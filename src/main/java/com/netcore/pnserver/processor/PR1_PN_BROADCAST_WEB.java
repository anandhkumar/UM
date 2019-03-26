package com.netcore.pnserver.processor;

import java.io.Reader;
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
import com.netcore.pnserver.util.CSVHelper;
import com.netcore.pnserver.util.Util;


public class PR1_PN_BROADCAST_WEB extends AbstractTask implements ApplicationContextAware{
	
	private String queueName; 
	private RedisDao redisDao;
	private MySqlDao mysqlDao;
	private Util util;
	private String customEventType = null;
	
	private static ApplicationContext applicationContext = null;
	final static Logger logger = Logger.getLogger(PR1_PN_BROADCAST_WEB.class);
	
	public PR1_PN_BROADCAST_WEB(String queue, RedisDao simpleDequeuer, MySqlDao mysqlDao, Util util){
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
					logger.info("<"+this.requestId +"> Passed event to test PNServer, TT="+(System.currentTimeMillis() - startTime));
					return;
				}
				int ttl =  Util.getMaxTtl(pnr.getTtl());
				pnr.setTtl(ttl);
				
				
				if(pnr.getBrowser() != null && pnr.getUrl()!=null && pnr.getUrl().trim().length() >0 ){
					PushNotificationGateway gateway = (PushNotificationGateway) applicationContext.getBean(pnr.getBrowser());
					if(gateway==null)
						throw new RuntimeException("Gateway Bean not found for browser:"+pnr.getBrowser());
					
					Content content = pnr.getMessage().getContent();
					
					pnr = processFile(pnr.getUrl(),pnr.getCid(), pnr.getSiteid(), ttl, pnr.getMsgid(), content.getIcon(), content.getImageurl(), gateway, pnr.getIdentified(), pnr.getActions(), pnr.isAutohide(), pnr);
				}				
				
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
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
	
	private PushNotificationRequest processFile(String urlstr, int cid, String siteid, int ttl, int msgid, String icon, String imageUrl, PushNotificationGateway gateway, int identifiedFlag, List<Map<String, String>> actions, boolean autohide, PushNotificationRequest pnr) throws Exception{
		
		try {
		Reader fr = Util.getFileReader(urlstr);
		List<String> values = CSVHelper.parseLine(fr);		
		long mst = pnr.getMysqlTime();
		
		long t1 = System.currentTimeMillis();
		mst += System.currentTimeMillis() - t1;
		
		List<PushNotification> pnList = new ArrayList<PushNotification>();		
		pnr.setPnbcJsonList(new ArrayList<String>());
		
		while (values!=null) {
			try{
				
	    		int uid = Integer.valueOf(values.get(0));
	    		String pushId = values.get(1);
	    		int bod = 0;
	    		if(values.get(7)!=null && !values.get(7).isEmpty())
	    			bod = Integer.valueOf(values.get(7));	    		
	    		
//	    		String identity = values.get(2);
	    		String title = values.get(3);
	    		String message = values.get(4);
	    		String deepLink = values.get(5);
	    		String trid = values.get(6);
//	    		String icon = values.get(7);
	    		String[] tridArr= Util.getTrIDDetails(trid);
				long ts = Long.valueOf(tridArr[4]);
	    		
	    		if(values.get(8) != null && !values.get(8).isEmpty()){
	    			actions = util.getListOfActions(values.get(8), Util.EVENT_TYPE_WEB);
	    		}
	    		
	    		String token = null;
	    		if(values.size()> 9 && values.get(9) != null && !values.get(9).isEmpty()){
	    			token = values.get(9);
	    		}
	    		
	    		PushNotification pn = new PushNotification(uid,pushId,cid,msgid,bod, trid, ts, title, message, deepLink, imageUrl, token, null);
	    		pnList.add(pn);
				
			}catch (Exception e){
				logger.error(this.requestId+" - "+e.getMessage(),e);
			}
			values = CSVHelper.parseLine(fr);
			
	    }
		pnr = gateway.sendMultiCast(pnList, icon, true, null, String.valueOf(msgid), cid, siteid, ttl, Util.EVENT_TYPE_WEB, identifiedFlag, actions, autohide, null, null, this.requestId, customEventType, pnr);
		pnr = publishResult(cid, msgid, pnr.getPushNotificationsubList(), identifiedFlag,siteid, pnr, pnr.isSkipFlag());
		pnr.setMysqlTime(mst);
		} catch (Exception e) {
			logger.error(this.requestId + " - "+e.getMessage(),e);;
		}
		return pnr;
	}
	
	private PushNotificationRequest publishResult(int cid, int msgid, List<PushNotification> pnList, int identifiedFlag,String siteId, PushNotificationRequest pnr, boolean skipFlag) throws JsonProcessingException{
		
		int sentCount=0, failedCount=0;
		long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(),  sc = pnr.getSentCount(), fc = pnr.getFailedCount(); 
		List<String> pnbcJsonList = pnr.getPnbcJsonList();
		
		for(PushNotification pn : pnList) {
			List<Integer> userIds = new ArrayList<Integer>();
			userIds.add(pn.userid);
			List<Integer> bods = new ArrayList<Integer>();
			bods.add(pn.bod);
			List<String> pushIds = new ArrayList<String>();
			pushIds.add(pn.pushid);
			List<String> tokens = new ArrayList<String>();
			tokens.add(pn.token);
			List<String> newTokens = new ArrayList<String>();
			newTokens.add(pn.getNewtoken());
			List<Boolean> invalidTokenList = new ArrayList<Boolean>();
			invalidTokenList.add(pn.invalidToken);
			
			if(pn.status == null && pn.description == null) {
				pn.status = "sent";
			}
			String json =null;
			if (pn.description != null){
				List<String> descriptions = new ArrayList<String>();
				descriptions.add(pn.description);
				json = Util.createJson(pn.status, cid, msgid, userIds, pushIds, pn.getTrid(), pn.getTs(),descriptions, Util.EVENT_TYPE_WEB, identifiedFlag,skipFlag,siteId,tokens,bods, null, null, newTokens, invalidTokenList);
			}
			else
				json = Util.createJson(pn.status, cid, msgid, userIds, pushIds, pn.getTrid(), pn.getTs(),null, Util.EVENT_TYPE_WEB, identifiedFlag,skipFlag,siteId,tokens,bods, null, null, newTokens, invalidTokenList);
			
			pnbcJsonList.add(json);
		
			if (pn.status.equalsIgnoreCase("sent")) {
				sentCount += userIds.size();
				sc += userIds.size();
			}
	    	else {
	    		failedCount += userIds.size();
	    		fc += userIds.size();
	    	}
	    	
		}
		
		long t3 = System.currentTimeMillis();
		logger.info(this.requestId +" RPUSH PR1_PN_BC_TR " + pnbcJsonList );
		redisDao.publishPNBCList(Util.PR1_PN_BC_TR, pnbcJsonList);
		ret += System.currentTimeMillis() - t3;		
		
		pnr.setMysqlTime(mst);
    	pnr.setRedisTime(ret);
    	pnr.setSentCount(sc);
		pnr.setFailedCount(fc);
    	return pnr;
	}
	

	public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
	
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         PR1_PN_BROADCAST_WEB.applicationContext = applicationContext;
    }
	
}
