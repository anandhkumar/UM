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


public class PR1_PN_MULTICAST_WEB extends AbstractTask implements ApplicationContextAware{
	
	private String queueName; 
	private RedisDao redisDao;
	private MySqlDao mysqlDao;
	private MongoDao mongoDao;
	private Util util;
	private static ApplicationContext applicationContext = null;
	private int BATCH_SIZE = 1000;
	private String customEventType = null;
	final static Logger logger = Logger.getLogger(PR1_PN_MULTICAST_WEB.class);
	
	public PR1_PN_MULTICAST_WEB(String queue, RedisDao simpleDequeuer, MySqlDao mysqlDao,MongoDao mongoDao,Util util){
		this.queueName = queue;
		this.redisDao = simpleDequeuer;
		this.mysqlDao = mysqlDao;
		this.mongoDao = mongoDao;
		this.util = util;
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
				if(pnr.getBrowser() != null && pnr.getUrl()!=null && pnr.getUrl().trim().length() >0 ){
					PushNotificationGateway gateway = (PushNotificationGateway) applicationContext.getBean(pnr.getBrowser());
					if(gateway==null)
						throw new RuntimeException("Gateway Bean not found for browsertype:"+pnr.getBrowser());
					if(pnr.getSiteid()==null||pnr.getSiteid().isEmpty())
						throw new RuntimeException("Site id is null for client : "+pnr.getCid());
					Content content = pnr.getMessage().getContent();
					pnr =  processFile(pnr.getUrl(),pnr.getCid(), pnr.getSiteid(), pnr.getMessage().getTrid(), content.getTitle(), 
							content.getMsgtext(), content.getIcon(), content.getImageurl(), content.getDeeplink(), 
							ts, pnr.getMsgid(),gateway, ttl, pnr.getIdentified(), pnr.getActions(), pnr.isAutohide(), pnr);
				}			
				
			} catch (Exception e) {
				logger.error(this.requestId + " - "+ e.getMessage(),e);
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
	
	private PushNotificationRequest processFile(String urlstr, int cid, String siteid, String trid, String title, String msgBody, 
			String icon , String imageUrl, String deepLink, long ts, int msgid, PushNotificationGateway gateway, int ttl, int indentifiedFlag, List<Map<String, String>> actions, boolean autohide, PushNotificationRequest pnr) throws Exception{
		
		try {
		long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(), mot = pnr.getMongoTime(), st = pnr.getSentTime(), sc = pnr.getSentCount(), fc = pnr.getFailedCount();
		pnr.setPnbcJsonList(new ArrayList<String>());
		pnr.setSummaryJsonList(new ArrayList<String>());
		pnr.setSent(0);
		pnr.setFailed(0);
		
		long t1 = System.currentTimeMillis();
		mst += System.currentTimeMillis() - t1;
		
		Integer sourceId = 0;
		String fconfig = null;
		String frequencyData = null;
		
		if(title.equals(Util.NETCORE_UNINSTALL)) {
			long t2 = System.currentTimeMillis();
//TODO remove below function call should be checked at EL can get sourceID from mysql incase of uninstall event 			
			//sourceId = mysqlDao.getWebSourceId(cid, siteid);
			mst += System.currentTimeMillis() - t2;
		}else {	
			long t3 = System.currentTimeMillis();
			fconfig = redisDao.hGet(Util.FREQ_CONFIG_FLAG, String.valueOf(cid));
			if(fconfig!=null && Integer.parseInt(fconfig)==1)
				frequencyData = redisDao.hGet(Util.FREQ_CONFIG, String.valueOf(cid));
			
			ret += System.currentTimeMillis() - t3;
		}
			
		
		Reader fr = Util.getFileReader(urlstr);
		List<String> values = CSVHelper.parseLine(fr);		
		List<PushNotification> pushNotificationsList = new ArrayList<PushNotification>();
		while (values!=null) {
	    	try{
	    		String token = null;
	    		if(values.size()>3  && values.get(3) != null && !values.get(3).isEmpty()){
	    			token = values.get(3);
	    		}
	    		PushNotification pn = new PushNotification(values.get(0)!=null?Integer.valueOf(values.get(0)):null, values.get(1), cid, msgid,values.get(2)!=null?Integer.valueOf(values.get(2)):0, trid, ts, token);				
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
			pnr = gateway.sendBroadCast(pushNotifications, trid, title, msgBody, null, icon, imageUrl, deepLink, null, null, String.valueOf(msgid), cid, siteid, ttl, Util.EVENT_TYPE_WEB, indentifiedFlag, actions, autohide, null, null, this.requestId,customEventType, pnr);
			pnr = publishResult(cid, msgid, pnr.getPushNotificationsubList(), trid, ts, Util.EVENT_TYPE_WEB, indentifiedFlag, siteid, title,pnr.isSkipFlag(), sourceId,  pnr);
			}catch (Exception e){
				logger.error(e.getMessage(),e);
			}
			pnr.setPushNotificationsubList(null);
		}
		
		long t4 = System.currentTimeMillis();
		if(title.equals(Util.NETCORE_UNINSTALL) && pnr.getSummaryJsonList().size() > 0){
			logger.info(this.requestId +" RPUSH Summarizer PR1_PN_BC_TR " + pnr.getSummaryJsonList() );
			redisDao.publishToSummarizerRedisList(Util.PR1_PN_BC_TR, pnr.getSummaryJsonList(),requestId);
			pnr.getSummaryJsonList().clear();
		}
		if(pnr.getPnbcJsonList().size() > 0) {
			logger.info(this.requestId +" RPUSH PR1_PN_BC_TR " + pnr.getPnbcJsonList());
			redisDao.publishPNBCList(Util.PR1_PN_BC_TR, pnr.getPnbcJsonList());
			pnr.getPnbcJsonList().clear();
		}
		ret += System.currentTimeMillis() - t4;
		
		long t5 = System.currentTimeMillis();
		if(pnr.getSent() >0 || pnr.getFailed() > 0) {
			sc += pnr.getSent();
			fc += pnr.getFailed();
			pnr.setSent(0);
			pnr.setFailed(0);
		}
		mst += System.currentTimeMillis() - t5;
		
		
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
			logger.error(this.requestId + " - "+ e.getMessage(),e);
		}
		return pnr;
		
	}

	private PushNotificationRequest publishResult(int cid, int msgid, List<PushNotification> pushNotifications, String trid, long ts, String eventType, int identifiedFlag, String siteid, String title, boolean skipFlag, Integer sourceId,  PushNotificationRequest pnr) throws JsonProcessingException{
		Map<String,List<PushNotification>> statusMap = new HashMap<String,List<PushNotification>>();
		List<Integer> sentUids = new ArrayList<Integer>();
		List<Integer> sentBods = new ArrayList<Integer>();
		List<String> sentTokens = new ArrayList<String>();
		List<Integer> failedUids = new ArrayList<Integer>();
		List<Integer> failedBods = new ArrayList<Integer>();
		List<String> sentPushids = new ArrayList<String>();
		List<String> failedPushids = new ArrayList<String>();
		List<String> failedTokens = new ArrayList<String>();
		List<String> descriptions = new ArrayList<String>();
		List<String> sentnewTokenList = new ArrayList<String>();
		List<String> failednewTokenList = new ArrayList<String>();
		List<Boolean> sentinvalidTokenList = new ArrayList<Boolean>();
		List<Boolean> failedinvalidTokenList = new ArrayList<Boolean>();
		
		List<String> pnbcJsonList = pnr.getPnbcJsonList();
		List<String> summaryJsonList = pnr.getSummaryJsonList();
		int sent = pnr.getSent();
		int failed = pnr.getFailed();
		
		for(PushNotification pn : pushNotifications){
			if(pn.status == null && pn.description == null ) {
				pn.status = "sent";
			}
			if(pn.status == null && !title.equals(Util.NETCORE_UNINSTALL)){
				sentUids.add(pn.userid);
				sentPushids.add(pn.pushid);
				sentBods.add(pn.bod);
				sentTokens.add(pn.token);
                sentnewTokenList.add(pn.getNewtoken());
                sentinvalidTokenList.add(pn.invalidToken);

			}
			else if(pn.status.equals("sent") && !title.equals(Util.NETCORE_UNINSTALL)){
				sentUids.add(pn.userid);
				sentPushids.add(pn.pushid);
				sentBods.add(pn.bod);
				sentTokens.add(pn.token);
                sentnewTokenList.add(pn.getNewtoken());
                sentinvalidTokenList.add(pn.invalidToken);
			}else if (pn.status.equals("failed")){
				failedUids.add(pn.userid);
				failedPushids.add(pn.pushid);
				descriptions.add(pn.description);
				failedBods.add(pn.bod);
				failedTokens.add(pn.token);
                failednewTokenList.add(pn.getNewtoken());
                failedinvalidTokenList.add(pn.invalidToken);
			}
		}
		if(title.equals(Util.NETCORE_UNINSTALL)){
			String json = Util.createJson("failed", cid, msgid, failedUids, failedPushids, trid, ts, descriptions, eventType, identifiedFlag, false, siteid, failedTokens, failedBods, null, util.getAutomationId(trid), failednewTokenList, failedinvalidTokenList);
			summaryJsonList.add(json);			
		}
		else{
			if(sentUids.size()>0){
		    	String json =Util.createJson("sent", cid, msgid, sentUids, sentPushids, trid, ts, null, eventType, identifiedFlag,skipFlag,siteid,sentTokens,sentBods, null, null, sentnewTokenList, sentinvalidTokenList);		    	
		    	pnbcJsonList.add(json);
		    }
		    if(failedUids.size()>0){
		    	String json =Util.createJson("failed", cid, msgid, failedUids, failedPushids, trid, ts, descriptions, eventType, identifiedFlag,skipFlag,siteid,failedTokens,failedBods, null, null, failednewTokenList, failedinvalidTokenList);
		    	pnbcJsonList.add(json);
		    	
		    }
		    sent += sentUids.size();
		    failed += failedUids.size();		    	
		}
		pnr.setSent(sent);
		pnr.setFailed(failed);
		pnr.setPnbcJsonList(pnbcJsonList);
		pnr.setSummaryJsonList(summaryJsonList);
		return pnr;
	}

	
	public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
	
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         PR1_PN_MULTICAST_WEB.applicationContext = applicationContext;
    }
}
