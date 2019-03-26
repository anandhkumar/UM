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


public class PR1_PN_TR_WEB extends AbstractTask implements ApplicationContextAware{
	
	private String queueName; 
	private RedisDao redisDao;
	private MySqlDao mysqlDao;
	private Util util;
	private String customEventType = null;

	final static Logger logger = Logger.getLogger(PR1_PN_TR_WEB.class);
	private static ApplicationContext applicationContext = null;
	public PR1_PN_TR_WEB(String queue, RedisDao simpleDequeuer, MySqlDao mysqlDao,Util util){
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
				
				String[] tridArr= Util.getTrIDDetails(pnr.getMessage().getTrid());
				long ts = Long.valueOf(tridArr[4]);
				
				List<PushNotification> pnList = new ArrayList<PushNotification>();
				
				for(Map<String, String> device : pnr.getDevices()) {
				int bod = Integer.parseInt(device.get("bod"));
				PushNotification pn = new PushNotification(pnr.getUid(),device.get("pushid"), pnr.getCid(), pnr.getMsgid(),bod,false, device.get("siteid"));
				if(device.get("browser") != null){
					PushNotificationGateway gateway = (PushNotificationGateway) applicationContext.getBean(device.get("browser"));
					if(gateway==null)
						throw new RuntimeException("Gateway Bean not found for ostype:"+device.get("browser"));
					Content content = pnr.getMessage().getContent();
					int devHostFlag = device.get("developer") != null ? Integer.parseInt(device.get("developer")) : 1729;
					pnr = gateway.sendSingleMessage(pn, pnr.getMessage().getTrid(), content.getTitle(), 
							content.getMsgtext(), content.getIcon(), content.getImageurl(), content.getDeeplink(),
							true, null, String.valueOf(pnr.getMsgid()), pnr.getCid(), device.get("siteid"), ttl, Util.EVENT_TYPE_WEB, pnr.getIdentified(), pnr.getActions(), pnr.isAutohide(), null, null, null, this.requestId, customEventType, pnr, devHostFlag);
					
				pnList.add(pnr.getPushNotification());	
				}
				
				}
				pnr = publishResult(pnr.getCid(), pnr.getMsgid(), pnList, pnr.getMessage().getTrid(), ts, Util.EVENT_TYPE_WEB, pnr.getIdentified(), pnr.getFreqcapflag(), pnr);				
				
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
	
	private PushNotificationRequest publishResult(int cid, int msgid, List<PushNotification> pnList , String trid, long ts, String eventType, int identifiedFlag, int freqcapflag, PushNotificationRequest pnr) throws JsonProcessingException{
		
		String json = null;
		int sentCount = 0,failedCount = 0;		
		List<String> pushIds = new ArrayList<String>();
		List<String> tokens = new ArrayList<String>();
		List<Integer> bods = new ArrayList<Integer>();
		List<Integer> userIds = new ArrayList<Integer>();
		List<String> descriptions = new ArrayList<String>();
		List<String> status = new ArrayList<String>();
		List<String> siteids = new ArrayList<String>();
		List<String> newTokens = new ArrayList<String>();
		List<Boolean> invalidTokenList = new ArrayList<Boolean>();
		
		long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(); 
		
		for(PushNotification pn : pnList) {
			userIds.add(pn.userid);		
			pushIds.add(pn.pushid);		
			tokens.add(pn.token);		
			bods.add(pn.bod);							
			descriptions.add(pn.description);
			status.add(pn.status);
			siteids.add(pn.siteid);
			newTokens.add(pn.getNewtoken());
			invalidTokenList.add(pn.invalidToken);
			
			if (pn.status.equalsIgnoreCase("sent")) 
	    		sentCount += 1;
			else 
	    		failedCount += 1;
	    				
		}
			
		boolean skipFlag = false;
		//json = Util.createJsonList(status, cid, msgid, userIds, pushIds, trid, ts, descriptions, eventType, identifiedFlag, mysqlDao.skipFrequencyCapping(cid, msgid, Util.CHANNEL_WEBPUSH), siteids, tokens, bods, null, null, null, freqcapflag, newTokens, invalidTokenList);
		json = Util.createJsonList(status, cid, msgid, userIds, pushIds, trid, ts, descriptions, eventType, identifiedFlag, skipFlag, siteids, tokens, bods, null, null, null, freqcapflag, newTokens, invalidTokenList);
		
		long t66 = System.currentTimeMillis();
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
