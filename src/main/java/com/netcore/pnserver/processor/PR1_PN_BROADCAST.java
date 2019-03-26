package com.netcore.pnserver.processor;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netcore.pnserver.dao.MySqlDao;
import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.gateway.PushNotificationGateway;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.pojo.PushNotificationRequest;
import com.netcore.pnserver.util.CSVHelper;
import com.netcore.pnserver.util.Util;


public class PR1_PN_BROADCAST extends AbstractTask implements ApplicationContextAware{
	
	private String queueName; 
	private RedisDao redisDao;
	private MySqlDao mysqlDao;
	private Util util;
	private String customEventType;
	
	private static ApplicationContext applicationContext = null;
	final static Logger logger = Logger.getLogger(PR1_PN_BROADCAST.class);
	
	public PR1_PN_BROADCAST(String queue, RedisDao simpleDequeuer, MySqlDao mysqlDao, Util util, String customEventType){
		this.queueName = queue;
		this.redisDao = simpleDequeuer;
		this.mysqlDao = mysqlDao;
		this.util = util;
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
				
				if(pnr.getOs() != null && pnr.getUrl()!=null && pnr.getUrl().trim().length() >0 ){
					PushNotificationGateway gateway = (PushNotificationGateway) applicationContext.getBean(pnr.getOs());
					if(gateway==null)
						throw new RuntimeException("Gateway Bean not found for ostype:"+pnr.getOs());

					pnr = processFile(pnr, ttl, gateway);
				}				
				
			} catch (Exception e) {
				logger.error(this.requestId+ " - "+ e.getMessage(),e);
			}
			long mysqlTIme = pnr.getMysqlTime();
			long mongoTime = pnr.getMongoTime();
			long redisTime = pnr.getRedisTime();
			long sentTIme = pnr.getSentTime();
			long endTime = System.currentTimeMillis() - startTime;

			long PT = endTime - mysqlTIme - mongoTime - sentTIme - redisTime;
			logger.info("<" + this.requestId + "> [MST=" + mysqlTIme + ", MOT=" + mongoTime + ", RET=" + redisTime
					+", SC="+pnr.getSentCount()+", FC="+pnr.getFailedCount()+", TC="+(pnr.getSentCount()+pnr.getFailedCount())+", ST=" + sentTIme + ", PT=" + PT + ", TT=" + endTime+"]");
	}
	
	private PushNotificationRequest processFile(PushNotificationRequest pnr, int ttl, PushNotificationGateway gateway) throws Exception{
		try {
		Reader fr = Util.getFileReader(pnr.getUrl());
		List<String> values = CSVHelper.parseLine(fr);	
		long mst = pnr.getMysqlTime();
						
		List<PushNotification> pnList = new ArrayList<PushNotification>();
		pnr.setUninstallJsonList(new ArrayList<String>());
		pnr.setPnbcJsonList(new ArrayList<String>());
		
		while (values!=null) {
	    	try{
	    		int uid = Integer.valueOf(values.get(0));
	    		String pushId = values.get(1);
	    		String identity = values.get(2);
	    		String title = values.get(3);
	    		String message = values.get(4);
	    		String deepLink = values.get(5);
	    		String trid = values.get(6);
	    		String imageURL = values.get(7);
	    		String[] tridArr= Util.getTrIDDetails(trid);
				long ts = Long.valueOf(tridArr[4]);
				
	    		int bod = values.get(8) != null && !values.get(8).isEmpty() ? Integer.parseInt(values.get(8)) : 0;
	    		if(values.size() > 9){
		    		pnr.setActionButton(values.get(9).trim() != null && !values.get(9).trim().isEmpty() ? util.getListOfActions(values.get(9).trim(), Util.EVENT_TYPE_APP) : pnr.getActionButton());
	    		}
	    		if(values.size() > 10){
	    			pnr.setCarousel(values.get(10).trim() != null && !values.get(10).trim().isEmpty() ? util.getListOfImages(values.get(10).trim()) : pnr.getCarousel());
	    		}
	    		
	    		String token = null;
	    		if(values.size()> 11 &&  values.get(11) != null && !values.get(11).isEmpty()){
	    			token = values.get(11);
	    		}
	    		if(values.size() > 12){
		    		pnr.setCustomPayload(values.get(12).trim() != null && !values.get(12).trim().isEmpty() ? util.getMapOfCustomPayload(values.get(12).trim()) : pnr.getCustomPayload());
	    		}
	    		
	    		PushNotification pn = new PushNotification(uid, pushId, pnr.getCid(), pnr.getMsgid(), bod, trid, ts, title, message, deepLink, imageURL, token, pnr.getCustomPayload());	    		
	    		pnList.add(pn);

			}catch (Exception e){
				logger.error(this.requestId + " - " + e.getMessage(),e);
			}
			values = CSVHelper.parseLine(fr);
	    }
		pnr = gateway.sendMultiCast(pnList, null, true, null, String.valueOf(pnr.getMsgid()), pnr.getCid(), pnr.getAppid(), ttl, Util.EVENT_TYPE_APP, pnr.getIdentified(), null, false, pnr.getActionButton(), pnr.getCarousel(), this.requestId, customEventType, pnr);
		pnr = publishResult(pnr.getCid(), pnr.getMsgid(), pnr.getPushNotificationsubList(), pnr.getIdentified(), pnr.getAppid(), pnr.getOs(), pnr, pnr.isSkipFlag());
		
	} catch (Exception e) {
		logger.error(this.requestId + " - "+e.getMessage(),e);
	}
		return pnr;
	}
	
	private PushNotificationRequest publishResult(int cid, int msgid,  List<PushNotification> pnList, int identifiedFlag,String appid, String ostype, PushNotificationRequest pnr, boolean skipFlag) throws JsonProcessingException{
		
		int sentCount=0, failedCount=0;
		long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(),  sc = pnr.getSentCount(), fc = pnr.getFailedCount(); 
		List<String> pnbcJsonList = pnr.getPnbcJsonList();
		List<String> unistallList = pnr.getUninstallJsonList();
		List<PushNotification> invalidAppTokenList = new ArrayList<PushNotification>();
		
		List<Integer> userIds = new ArrayList<Integer>();
		List<Integer> bods = new ArrayList<Integer>();
		List<String> pushIds = new ArrayList<String>();
		List<String> tokens = new ArrayList<String>();
		List<String> newTokens = new ArrayList<String>();
		List<Boolean> invalidTokenList = new ArrayList<Boolean>();
		List<String> descriptions = new ArrayList<String>();
		
		for(PushNotification pn : pnList) {
			userIds.add(pn.userid);
			bods.add(pn.bod);
			pushIds.add(pn.pushid);
			tokens.add(pn.token);
			newTokens.add(pn.getNewtoken());
			invalidTokenList.add(pn.invalidToken);
			
			if(pn.status == null && pn.description == null) {
				pn.status = "sent";
			}
			String json =null;
						
			if (pn.description !=null ){
				descriptions.add(pn.description);
				json = Util.createJson(pn.status, cid, msgid, userIds, pushIds, pn.getTrid(), pn.getTs(),descriptions, Util.EVENT_TYPE_APP, identifiedFlag,skipFlag, appid, tokens, bods, ostype, null, newTokens, invalidTokenList);
				
				if(Util.pushFailureReasons.contains(pn.description.trim())){
/*					List<Integer> uninstallUserIds = new ArrayList<Integer>();
					List<String> uninstallPushids = new ArrayList<String>();
					List<String> uninstallDescriptions = new ArrayList<String>();
					uninstallUserIds.add(pn.userid);
					uninstallPushids.add(pn.pushid);
					uninstallDescriptions.add(pn.description);
*/					
					unistallList.add(json);
				}
			}else
				json = Util.createJson(pn.status, cid, msgid, userIds, pushIds, pn.getTrid(), pn.getTs(),descriptions, Util.EVENT_TYPE_APP, identifiedFlag,skipFlag, appid, tokens, bods, ostype, null, newTokens, invalidTokenList);
				
			pnbcJsonList.add(json);
			
			if (pn.status.equalsIgnoreCase("sent")) {
				sentCount += userIds.size();
				sc += userIds.size();
			}
	    	else {
	    		failedCount += userIds.size();
	    		fc += userIds.size();
	    	}
			userIds.clear();
			bods.clear();
			pushIds.clear();
			tokens.clear();
			newTokens.clear();
			invalidTokenList.clear();
			descriptions.clear();
		}
		
		long t5 = System.currentTimeMillis();
		if(pnbcJsonList.size() > 0) {
			logger.info(this.requestId +" RPUSH PR1_PN_BC_TR " + pnbcJsonList );
			redisDao.publishPNBCList(Util.PR1_PN_BC_TR, pnbcJsonList);
		}
		if(unistallList.size() > 0) {
			logger.info(this.requestId +" RPUSH PR1_APPUNINSTALL " +  unistallList);
			redisDao.publishPNBCList("PR1_APPUNINSTALL", unistallList);
		}
		
		ret += System.currentTimeMillis() - t5;

		pnr.setRedisTime(ret);
    	pnr.setSentCount(sc);
		pnr.setFailedCount(fc);
    	return pnr;
	}
	

	public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
	
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         PR1_PN_BROADCAST.applicationContext = applicationContext;
    }
	
}
