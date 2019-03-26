package com.netcore.pnserver.processor;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.mongodb.client.AggregateIterable;
import com.netcore.pnserver.dao.MongoDao;
import com.netcore.pnserver.dao.MySqlDao;
import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.gateway.PushNotificationGateway;
import com.netcore.pnserver.pojo.Content;
import com.netcore.pnserver.pojo.FrequencyConfig;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.pojo.PushNotificationRequest;
import com.netcore.pnserver.util.CSVHelper;
import com.netcore.pnserver.util.Util;



public class PR1_PN_MULTICAST extends AbstractTask implements ApplicationContextAware{
	
	private String queueName; 
	private RedisDao redisDao;
	private MySqlDao mysqlDao;
	private MongoDao mongoDao;
	private static ApplicationContext applicationContext = null;
	private int BATCH_SIZE = 1000;
	private Util util;
	private String customEventType;
	
	final static Logger logger = Logger.getLogger(PR1_PN_MULTICAST.class);
	
	public PR1_PN_MULTICAST(String queue, RedisDao simpleDequeuer, MySqlDao mysqlDao,MongoDao mongoDao,Util util, String customEventType){
		this.queueName = queue;
		this.redisDao = simpleDequeuer;
		this.mysqlDao = mysqlDao;
		this.util = util;
		this.mongoDao = mongoDao;
		this.customEventType = customEventType;
	}
	
	public void run() {
		long startTime = System.currentTimeMillis();
		
		PushNotificationRequest pnr = null;
			try {
				pnr = Util.getObjectFromJson(redisString);
				if(util.IS_FEATURE_TOGGLE && util.testClientList.contains(pnr.getCid())){
					redisDao.enqueueTestRedis(queueName, redisString);
					logger.info("<"+this.requestId +"> Passed event to test PNServer, TT="+(System.currentTimeMillis() - startTime));
					return;
				}
				
				int ttl =  Util.getMaxTtl(pnr.getTtl());
				pnr.setTtl(ttl);
				
				String[] tridArr= Util.getTrIDDetails(pnr.getMessage().getTrid());
				long ts = Long.valueOf(tridArr[4]);
				if(pnr.getOs() != null && pnr.getUrl()!=null && pnr.getUrl().trim().length() >0 ){
					PushNotificationGateway gateway = (PushNotificationGateway) applicationContext.getBean(pnr.getOs());
					if(gateway==null)
						throw new RuntimeException("Gateway Bean not found for ostype:"+pnr.getOs());
					Content content = pnr.getMessage().getContent();
					pnr = processFile(pnr.getUrl(),pnr.getCid(), pnr.getAppid(), pnr.getMessage().getTrid(), content.getTitle(), 
							content.getMsgtext(), null, content.getImageurl(), content.getDeeplink(), 
							ts, pnr.getMsgid(),gateway, ttl, pnr.getIdentified(), content.getActionButton(), content.getCarousel(), content.getcustomPayload(), pnr.getOs(), pnr);				
				}				
				
			} catch (Exception e) {
				logger.error(this.requestId + " - "+e.getMessage(),e);;
			}
			long mysqlTIme = pnr.getMysqlTime();
			long mongoTime = pnr.getMongoTime();
			long redisTime = pnr.getRedisTime();
			long sentTIme = pnr.getSentTime();
			long endTime = System.currentTimeMillis() - startTime;

			long PT = endTime - mysqlTIme - mongoTime - sentTIme - redisTime;
			logger.info("<" + this.requestId + "> [MST=" + mysqlTIme + ", MOT=" + mongoTime + ", RET=" + redisTime
					+", SC="+pnr.getSentCount()+", FC="+pnr.getFailedCount()+", TC="+ (pnr.getSentCount() +pnr.getFailedCount()) + ", ST=" + sentTIme + ", PT=" + PT + ", TT=" + endTime+"]");
	}
	
	public static <T> List<List<T>> chunk(List<T> input, int chunkSize) {

		int inputSize = input.size();
		int chunkCount = (int) Math.ceil(inputSize / (double) chunkSize);

		Map<Integer, List<T>> map = new HashMap<>(chunkCount);
		List<List<T>> chunks = new ArrayList<>(chunkCount);

		for (int i = 0; i < inputSize; i++) {

			map.computeIfAbsent(i / chunkSize, (ignore) -> {

				List<T> chunk = new ArrayList<>();
				chunks.add(chunk);
				return chunk;

			}).add(input.get(i));
		}

		return chunks;
	}
	
	private PushNotificationRequest processFile(String urlstr, int cid, String appid, String trid, String title, String msgBody, 
		String icon , String imageUrl, String deepLink, long ts, int msgid, PushNotificationGateway gateway, int ttl, int indentifiedFlag, List<Map<String, String>> actionButton, List<Map<String, String>> carousel, Map<String, String> customPayload, String ostype,  PushNotificationRequest pnr) throws Exception{
					
		try {
		long mst = 0, ret = 0, mot = 0, st = 0, sc = 0, fc = 0;
		pnr.setInvalidTokenList(new ArrayList<PushNotification>());		
		pnr.setUninstallJsonList(new ArrayList<String>());
		pnr.setPnbcJsonList(new ArrayList<String>());		
		pnr.setSent(0);
		pnr.setFailed(0);
		
		Reader fr = Util.getFileReader(urlstr);
		List<String> values = CSVHelper.parseLine(fr);
		
		List<PushNotification> pushNotificationsList = new ArrayList<PushNotification>();
		
		while (values!=null) {
	    	try{
	    		int bod = 0;
	    		if(values.size()>2){
	    			bod = values.get(2) != null ? Integer.parseInt(values.get(2)) : 0;
	    		}
	    		String token = null;
	    		if(values.size()>3 && values.get(3) != null && !values.get(3).isEmpty()){
	    			token = values.get(3);
	    		}
	    		PushNotification pn = new PushNotification(values.get(0)!=null?Integer.valueOf(values.get(0)):null, values.get(1), cid, msgid, bod, trid, ts, token);				
	    		pushNotificationsList.add(pn);
	    		
	    		
	    	}catch (Exception e){
				logger.error(e.getMessage(),e);
			}
	    	values = CSVHelper.parseLine(fr);
		}
	    logger.info("Request Size: " + pushNotificationsList.size());
		List<List<PushNotification>> chunks = chunk(pushNotificationsList, BATCH_SIZE);
		
		for(List<PushNotification> pushNotifications : chunks) {
			try{
    			pnr = gateway.sendBroadCast(pushNotifications, trid, title, msgBody, customPayload, icon, imageUrl, deepLink, null, null, String.valueOf(msgid), cid, appid, ttl, Util.EVENT_TYPE_APP, indentifiedFlag, null, false, actionButton, carousel, this.requestId, customEventType, pnr);
    	    	pnr = publishResult(cid, msgid, pnr.getPushNotificationsubList(), trid, ts, indentifiedFlag, title, appid, ostype, pnr.isSkipFlag(), pnr);
    			
			}catch (Exception e){
				logger.error(this.requestId + " - " + e.getMessage(),e);
			}	
			pnr.setPushNotificationsubList(null);
	    }
		long t3 = System.currentTimeMillis();
		
		if(pnr.getSent() >0 || pnr.getFailed() > 0) {
			sc += pnr.getSent();
			fc += pnr.getFailed();
			pnr.setSent(0);
			pnr.setFailed(0);
		}
		mst += System.currentTimeMillis() - t3;
		long t4 = System.currentTimeMillis();
	
		if(pnr.getUninstallJsonList().size() > 0){
			logger.info(this.requestId +" RPUSH PR1_APPUNINSTALL " + pnr.getUninstallJsonList() );
			redisDao.publishPNBCList("PR1_APPUNINSTALL", pnr.getUninstallJsonList());
			pnr.getUninstallJsonList().clear();
		}
		if(pnr.getPnbcJsonList().size() > 0) {
			logger.info(this.requestId +" RPUSH PR1_PN_BC_TR " + pnr.getPnbcJsonList() );
			redisDao.publishPNBCList(Util.PR1_PN_BC_TR, pnr.getPnbcJsonList());
			pnr.getPnbcJsonList().clear();
		}
		ret += System.currentTimeMillis() - t4;
		
		mst += pnr.getMysqlTime();
		mot += pnr.getMongoTime();
		ret += pnr.getRedisTime();
		st += pnr.getSentTime();
		
		pnr.setMysqlTime(mst);
		pnr.setMongoTime(mot);
		pnr.setRedisTime(ret);
		pnr.setSentTime(st);
		pnr.setSentCount(sc);
		pnr.setFailedCount(fc);
		
		} catch (Exception e) {
			logger.error(this.requestId + " - "+e.getMessage(),e);;
		}
		
		return pnr;
	}

	private PushNotificationRequest publishResult(int cid, int msgid, List<PushNotification> pushNotifications, String trid, long ts, int identifiedFlag,String title,String appid, String ostype, boolean skipFlag,  PushNotificationRequest pnr) throws JsonProcessingException{		
		Map<String,List<PushNotification>> statusMap = new HashMap<String,List<PushNotification>>();
		List<Integer> sentUids = new ArrayList<Integer>();
		List<Integer> sentBods = new ArrayList<Integer>();
		List<String> sentTokens = new ArrayList<String>();
		List<Integer> failedUids = new ArrayList<Integer>();
		List<Integer> failedBods = new ArrayList<Integer>();
		List<String> failedTokens = new ArrayList<String>();
		List<Integer> uninstallUserIds = new ArrayList<Integer>();
		List<String> sentPushids = new ArrayList<String>();
		List<String> failedPushids = new ArrayList<String>();
		List<String> uninstallPushids = new ArrayList<String>();
		List<Integer> uninstallBods = new ArrayList<Integer>();
		List<String> uninstallTokens = new ArrayList<String>();
		List<String> descriptions = new ArrayList<String>();
		List<String> uninstallDescriptions = new ArrayList<String>();
		List<String> failedDescription = new ArrayList<String>();
		List<String> sentDescription = new ArrayList<String>();
		List<String> sentnewTokenList = new ArrayList<String>();
		List<String> failednewTokenList = new ArrayList<String>();
		List<String> uninstallnewTokenList = new ArrayList<String>();
		List<Boolean> sentinvalidTokenList = new ArrayList<Boolean>();
		List<Boolean> failedinvalidTokenList = new ArrayList<Boolean>();
		List<Boolean> uninstallinvalidTokenList = new ArrayList<Boolean>();
		
		List<String> pnbcJsonList = pnr.getPnbcJsonList();
		List<String> uninstallJsonList = pnr.getUninstallJsonList();
		
		
		int sent = pnr.getSent();
		int failed = pnr.getFailed();
		
		
		for(PushNotification pn : pushNotifications){
			if(pn.status == null && pn.description == null) {
				pn.status = "sent";
			}
			if(pn.status == null){
				sentUids.add(pn.userid);
				sentPushids.add(pn.pushid);
				sentBods.add(pn.bod);
				sentTokens.add(pn.token);
				sentDescription.add(pn.description);
                sentnewTokenList.add(pn.getNewtoken());
                sentinvalidTokenList.add(pn.invalidToken);
			}
			else if(pn.status.equals("sent")){
				sentUids.add(pn.userid);
				sentPushids.add(pn.pushid);
				sentBods.add(pn.bod);
				sentTokens.add(pn.token);
				sentDescription.add(pn.description);
                sentnewTokenList.add(pn.getNewtoken());
                sentinvalidTokenList.add(pn.invalidToken);

			}else if (pn.status.equals("failed")){
				failedUids.add(pn.userid);
				failedPushids.add(pn.pushid);
				failedBods.add(pn.bod);
				failedTokens.add(pn.token);
				failedDescription.add(pn.description);
                failednewTokenList.add(pn.getNewtoken());
                failedinvalidTokenList.add(pn.invalidToken);
				
				if (pn.description != null) {
					if (Util.pushFailureReasons.contains(pn.description.trim())) {
						uninstallUserIds.add(pn.userid);
						uninstallPushids.add(pn.pushid);
						uninstallBods.add(pn.bod);
						uninstallTokens.add(pn.token);
						uninstallDescriptions.add(pn.description);
                        uninstallnewTokenList.add(pn.getNewtoken());
                        uninstallinvalidTokenList.add(pn.invalidToken);
					} 
				}
			}
		}
		
		
		String json;


		if(sentUids.size()>0 && !Util.NETCORE_UNINSTALL.equals(title)){
			json = Util.createJson("sent", cid, msgid, sentUids, sentPushids, trid, ts, sentDescription, Util.EVENT_TYPE_APP, identifiedFlag, skipFlag, appid, sentTokens, sentBods, ostype, null, sentnewTokenList, sentinvalidTokenList);				
			pnbcJsonList.add(json);
		}	
		if(failedUids.size()>0 && !Util.NETCORE_UNINSTALL.equals(title)){
			json = Util.createJson("failed", cid, msgid, failedUids, failedPushids, trid, ts, failedDescription, Util.EVENT_TYPE_APP, identifiedFlag,skipFlag,appid, failedTokens, failedBods, ostype, null, failednewTokenList, failedinvalidTokenList);				
			pnbcJsonList.add(json);
		}
		if(uninstallUserIds.size()>0){
			uninstallJsonList = publishUninstall(uninstallUserIds, uninstallPushids, uninstallTokens, uninstallBods, cid, msgid, trid, ts, uninstallDescriptions, identifiedFlag, appid, ostype, uninstallJsonList, uninstallnewTokenList, uninstallinvalidTokenList);
		}
		sent += sentUids.size();
		failed += failedUids.size();	
/*			
		}else{
			uninstallJsonList = publishUninstall(failedUids, failedPushids, failedTokens, failedBods, cid, msgid, trid, ts, descriptions, identifiedFlag,appid, ostype, uninstallJsonList, uninstallnewTokenList, uninstallinvalidTokenList);
		}
*/		
		pnr.setSent(sent);
		pnr.setFailed(failed);
		pnr.setPnbcJsonList(pnbcJsonList);
		pnr.setUninstallJsonList(uninstallJsonList);
		//pnr.setInvalidTokenList(invalidTokenList);
		
		return pnr;
	}

	private List<String> publishUninstall(List<Integer> uninstallUserIds, List<String> uninstallPushids, List<String> uninstallTokens, List<Integer> uninstallBods, int cid, int msgid, String trid,long ts, List<String> descriptions,int identifiedFlag,String appId, String ostype, List<String> uninstallJsonList, List<String> newTokenList, List<Boolean> invalidTokenList) throws JsonProcessingException {
		List<List<Integer>> sublistUserIds = Lists.partition(uninstallUserIds, 1000);
		List<List<String>> sublistPushIds = Lists.partition(uninstallPushids, 1000);
		String json;
		for(int i=0;i<sublistUserIds.size();i++){
			json = Util.createJson("uninstalled", cid, msgid, uninstallUserIds, uninstallPushids, trid, ts, descriptions, Util.EVENT_TYPE_APP, identifiedFlag, false, appId, uninstallTokens, uninstallBods, ostype, null, newTokenList, invalidTokenList);			
			uninstallJsonList.add(json);
		}
		return uninstallJsonList;
	}

	public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
	
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         PR1_PN_MULTICAST.applicationContext = applicationContext;
    }
	
	
}
