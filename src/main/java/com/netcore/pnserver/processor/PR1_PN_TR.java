package com.netcore.pnserver.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netcore.pnserver.dao.MySqlDao;
import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.gateway.PushNotificationGateway;
import com.netcore.pnserver.pojo.Content;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.pojo.PushNotificationRequest;
import com.netcore.pnserver.pojo.PushNotificationRequestList;
import com.netcore.pnserver.util.Util;


public class PR1_PN_TR extends AbstractTask implements ApplicationContextAware{
	
	private String queueName; 
	private RedisDao redisDao;
	private MySqlDao mysqlDao;
	private Util util;
	private String customEventType = null;
	
	final static Logger logger = Logger.getLogger(PR1_PN_TR.class);
	private static ApplicationContext applicationContext = null;
	public PR1_PN_TR(String queue, RedisDao simpleDequeuer, MySqlDao mysqlDao,Util util){
		this.queueName = queue;
		this.redisDao = simpleDequeuer;
		this.mysqlDao = mysqlDao;
		this.util = util;
	}
	
	public void run() {
			
				long startTime = System.currentTimeMillis();				
				PushNotificationRequest pnr = null;
			try {
				pnr = Util.getObjectFromJson(redisString);
				if(util.IS_FEATURE_TOGGLE && util.testClientList.contains(pnr.getCid())){
					redisDao.enqueueTestRedis(queueName, redisString);
					logger.info(this.requestId + " - Passed event to test PNServer :"+(System.currentTimeMillis() - startTime));
					return;
				}
				int ttl =  Util.getMaxTtl(pnr.getTtl());
				pnr.setTtl(ttl);
	
				String[] tridArr= Util.getTrIDDetails(pnr.getMsgbody().getTrid());
				long ts = Long.valueOf(tridArr[4]);
				
				List<PushNotification> pnList = new ArrayList<PushNotification>();
				pnr.setUninstallJsonList(new ArrayList<String>());
				
				for(Map<String, String> device : pnr.getDevices()) {
				int bod = Integer.parseInt(device.get("bod"));
				PushNotification pn = new PushNotification(pnr.getUid(),device.get("pushid"), pnr.getCid(), pnr.getMsgid(),bod,false, device.get("appid"), device.get("os"),device.get("developer"));
				if(device.get("os") != null){
					PushNotificationGateway gateway = (PushNotificationGateway) applicationContext.getBean(device.get("os"));								
					if(gateway==null)
						throw new RuntimeException("Gateway Bean not found for ostype:"+device.get("os"));
					Content content = pnr.getMsgbody().getContent();
					int devHostFlag = device.get("developer") != null ? Integer.parseInt(device.get("developer")) : 1729;
					pnr = gateway.sendSingleMessage(pn, pnr.getMsgbody().getTrid(), content.getTitle(), 
							content.getMsgtext(), null, content.getImageurl(), content.getDeeplink(),
							true, null, String.valueOf(pnr.getMsgid()), pnr.getCid(), pn.getAppid(), ttl, Util.EVENT_TYPE_APP, pnr.getIdentified(), null, false, content.getActionButton(), content.getCarousel(), content.getcustomPayload(), this.requestId, customEventType, pnr, devHostFlag);					

					
					pnList.add(pnr.getPushNotification());	
				}
				
				}
				
				pnr = publishResult(pnr.getCid(), pnr.getMsgid(), pnList, pnr.getMsgbody().getTrid(), ts, Util.EVENT_TYPE_APP, pnr.getIdentified(), pnr.getFreqcapflag(),pnr);
				
				logger.info("Execution time "+(System.currentTimeMillis() - startTime));
			} catch (Exception e) {
				logger.error(e.getMessage(),e);;
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
	
	private PushNotificationRequest publishResult(int cid, int msgid, List<PushNotification> pnList, String trid, long ts, String eventType, int identifiedFlag, int freqcapflag, PushNotificationRequest pnr) throws JsonProcessingException{
		
		String json = null;
		int sentCount = 0,failedCount = 0;
		List<String> unistallList = pnr.getUninstallJsonList();
		List<Integer> userIds = new ArrayList<Integer>();
		List<String> pushIds = new ArrayList<String>();
		List<String> tokens = new ArrayList<String>();
		List<Integer> bods = new ArrayList<Integer>();
		List<String> descriptions = new ArrayList<String>();
		List<String> status = new ArrayList<String>();
		List<String> appids = new ArrayList<String>();
		List<String> os = new ArrayList<String>();
		List<String> newTokens = new ArrayList<String>();
		List<Boolean> invalidTokenList = new ArrayList<Boolean>();
		
		//Initializing below lists for status eq failed and uninstalled 
		List<String> uninstallDescriptions = new ArrayList<String>();
		List<Integer> uninstallUserIds = new ArrayList<Integer>();
		List<String> uninstallPushids = new ArrayList<String>();
		List<String> uninstallTokens = new ArrayList<String>();
		List<String> uninstallnewTokens = new ArrayList<String>();
		List<Integer> uninstallBods = new ArrayList<Integer>();
		List<Boolean> uninstallInvalidTokenList = new ArrayList<Boolean>();
		
		long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(); 
		
		for(PushNotification pn : pnList) {
			userIds.add(pn.userid);		
			pushIds.add(pn.pushid);		
			tokens.add(pn.token);		
			bods.add(pn.bod);							
			descriptions.add(pn.description);
			status.add(pn.status);
			appids.add(pn.appid);
			os.add(Util.getOsType(pn.os));
			newTokens.add(pn.getNewtoken());
			invalidTokenList.add(pn.invalidToken);
			
			if (pn.status.equalsIgnoreCase("sent"))
	    		sentCount += 1;
	    	else
	    		failedCount += 1; 
			
			if (pn.description !=null ){
				
				if(Util.pushFailureReasons.contains(pn.description.trim())){
					uninstallDescriptions.add(pn.description);
					uninstallUserIds.add(pn.userid);
					uninstallPushids.add(pn.pushid);
					uninstallTokens.add(pn.token);
					uninstallnewTokens.add(pn.pushid);
					uninstallBods.add(pn.bod);
					uninstallInvalidTokenList.add(pn.invalidToken);

					json = Util.createJson("uninstalled", cid, msgid, uninstallUserIds, uninstallPushids, trid, ts, uninstallDescriptions, Util.EVENT_TYPE_APP, identifiedFlag, false, pn.appid, uninstallTokens, uninstallBods, pn.os, null, uninstallnewTokens, uninstallInvalidTokenList);
		    		unistallList.add(json);
		    		
				}
			}
			uninstallDescriptions.clear();
			uninstallUserIds.clear();
			uninstallPushids.clear();
			uninstallTokens.clear();
			uninstallnewTokens.clear();
			uninstallBods.clear();
			uninstallInvalidTokenList.clear();
		}
		long t66 = System.currentTimeMillis();
		if(unistallList.size() > 0) {
			redisDao.publishPNBCList("PR1_APPUNINSTALL", unistallList);
		}
				
		boolean skipFlag = false;
		json = Util.createJsonList(status, cid, msgid, userIds, pushIds, trid, ts, descriptions, eventType, identifiedFlag, skipFlag, appids, tokens, bods, os, null, null, freqcapflag, newTokens, invalidTokenList);
		redisDao.publishPNBC(Util.PR1_PN_ATR, json);
		ret += System.currentTimeMillis() - t66;
 	
    	pnr.setMysqlTime(mst);
    	pnr.setRedisTime(ret);
    	pnr.setSentCount(sentCount);
		pnr.setFailedCount(failedCount);
    	
    	return pnr;
	}
	

	public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
	
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.applicationContext = applicationContext;
    }
}
