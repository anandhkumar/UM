package com.netcore.pnserver.gateway;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Notification;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;
import com.netcore.pnserver.dao.MongoDao;
import com.netcore.pnserver.dao.MySqlDao;
import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.pojo.PushNotificationRequest;
import com.netcore.pnserver.util.Util;

class FCMSender extends Sender {

	public FCMSender(String key) {
		super(key);
	}

	@Override
	protected HttpURLConnection getConnection(String url) throws IOException {
		String fcmUrl = "https://fcm.googleapis.com/fcm/send";
		return (HttpURLConnection) new URL(fcmUrl).openConnection();
	}
}

public class GCMConnector implements PushNotificationGateway {
	private MySqlDao mysqlDao;
	private MongoDao mongoDao;
	private RedisDao redisDao;
	private int RETRIES = 2;

	final static Logger logger = Logger.getLogger(GCMConnector.class);

	public GCMConnector(MySqlDao mysqlDao, MongoDao mongoDao, RedisDao redisDao) {
		this.mysqlDao = mysqlDao;
		this.mongoDao = mongoDao;
		this.redisDao = redisDao;
	}

	private Sender getSender(String apiKey, String eventType, String serverType) {
		switch (eventType) {
		case Util.EVENT_TYPE_APP:
			if (Util.FCM_SERVER.equalsIgnoreCase(serverType))
				return new FCMSender(apiKey);// AIzaSyBW1c4AQg_xXi8EyY_9-6-_aRgMWRKrkVc
			else
				return new Sender(apiKey);

		case Util.EVENT_TYPE_WEB:
			return new FCMSender(apiKey);
		default:
			return null;
		}

	}
	//TODO remove function 
	private List<PushNotification> getTokens(List<PushNotification> pushNotifications,String requestId, int cid, String eventType, int identifiedFlag) {
		pushNotifications = eventType.equals(Util.EVENT_TYPE_APP)
				? mysqlDao.getAppTokensForGuids(cid, pushNotifications, Util.OS_TYPE_ANDROID, identifiedFlag, requestId)
				: mysqlDao.getWebTokensForGuids(cid, pushNotifications, Util.BROWSER_TYPE_CHROME, identifiedFlag,
						requestId);
				
				return pushNotifications;		
	}
	
	private PushNotification validate(PushNotification pn) {
		if (pn.getTitle() == null)
			pn.setTitle("");
		if (pn.getMessage() == null)
			pn.setMessage("");
		return pn;
	}	
	
	
	@Override
	public PushNotificationRequest sendMultiCast(List<PushNotification> pushNotifications, String icon,
			Boolean soundEnabled, String data, String collapse_key, int cid, String appid, int ttl, String eventType,
			int identifiedFlag, List<Map<String, String>> actions, boolean autohide,
			List<Map<String, String>> actionButton, List<Map<String, String>> carousel, String requestId,
			String customEventTyp, PushNotificationRequest pnr) throws IOException {

		List<PushNotification> EmptyTokenList = new ArrayList<PushNotification>();
		List<PushNotification> invalidTokenList = new ArrayList<PushNotification>();
		List<PushNotification> newTokenList = new ArrayList<PushNotification>();
		
				
		long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(), mot = pnr.getMongoTime(), st = pnr.getSentTime();
		//long ret = pnr.getRedisTime(), st = pnr.getSentTime();
		//try catch 
		if (soundEnabled == null)
			soundEnabled = true;
		if (collapse_key == null)
			collapse_key = "do_not_collapse";
		if (data == null)
			data = "{}";

		long t1 = System.currentTimeMillis();
/* 		
		if(pushNotifications.get(0).getToken() == null || pushNotifications.get(0).getToken().trim().isEmpty())
		pushNotifications = getTokens(pushNotifications, requestId, cid, eventType, identifiedFlag);
*/		
//todo remove mysql call and get apikey from aredis
		//String serverAndApiKey = eventType.equals(Util.EVENT_TYPE_APP) ? mysqlDao.getAndroidApiKey(cid, appid)
		//		: mysqlDao.getChromeApiKey(cid, appid);
		 
		mst += System.currentTimeMillis() - t1;
		
		String serverAndApiKey = pnr.getApikey();
		String serverGCMorFCM = serverAndApiKey.substring(0, 3);
		String apiKey = serverAndApiKey.substring(3, serverAndApiKey.length());
		logger.info("serverGCMorFCM ="+serverGCMorFCM+" apiKey="+apiKey);

		Sender sender = getSender(apiKey, eventType, serverGCMorFCM);

		for (PushNotification pushobj : pushNotifications) {

			try {
				if (pushobj.token != null) {

					pushobj = validate(pushobj);

					Message msg = buildGCMMessage(pushobj.getTrid(), pushobj.getTitle(), pushobj.getMessage(), icon,
							pushobj.getImageURL(), pushobj.getDeepLink(), soundEnabled, data, collapse_key, cid, appid,
							ttl, eventType, serverGCMorFCM, actions, autohide, actionButton, carousel, pushobj.getCustomPayload(), customEventTyp, false);

					long t6 = System.currentTimeMillis();
					Result res = sender.send(msg, pushobj.getToken(), RETRIES);
					logger.info("RESULT : " + res);
					st += System.currentTimeMillis() - t6;
					pushobj.setNewtoken(pushobj.getToken());
					if (res.getMessageId() != null) {
						String canonicalRegId = res.getCanonicalRegistrationId();
						pushobj.status = "sent";
						if (canonicalRegId != null) {
							pushobj.setNewtoken(canonicalRegId);
							long t5 = System.currentTimeMillis();
/*						
							switch (eventType) {
							case Util.EVENT_TYPE_APP:
								newTokenList.add(pushobj);
								//todo remove below function call and create json for update token and push to redis 
								//mongoDao.updateWebToken(cid, pushobj.getToken(), canonicalRegId, Util.EVENT_TYPE_APP);
								//token_canonicalRegId_map.put(pushobj.getToken(),pushobj.getNewtoken());
								break;
							case Util.EVENT_TYPE_WEB:
								newTokenList.add(pushobj);
								//todo remove below function call and create json for update token and push to redis								
								//mongoDao.updateWebToken(cid, pushobj.getToken(), canonicalRegId, Util.EVENT_TYPE_WEB);
								//token_canonicalRegId_map.put(pushobj.getToken(),pushobj.getNewtoken());
								break;
							}
							//mot += System.currentTimeMillis() - t5;
*/
						}
						if (eventType.equals(Util.EVENT_TYPE_APP)
								&& !pushobj.getTitle().equals(Util.NETCORE_UNINSTALL)) {
							ret += Util.pushAmpData(msg.getData().get("data"), pushobj, appid);
						}
						logger.info(requestId + " - Send BroadcastMessage Success : " + pushobj.toString());
					} else {
						String error = res.getErrorCodeName();
						pushobj.status = "failed";
						pushobj.description = error;
						//if (error.equals("Unregistered Device") || error.equals("InvalidRegistration") || error.equals("NotRegistered")) {
						if (error.equals("Unregistered Device") || error.equals("InvalidRegistration") 
								|| error.equals("NotRegistered") ) {
							//invalidTokenList.add(pushobj);
							pushobj.invalidToken = true;
						}
						logger.error(
								requestId + " - Send BroadcastMessage Failed : " + error + " : " + pushobj.toString());
					}

				} else {
					pushobj.status = "failed";
					pushobj.description = "token not found for pushid";
					//EmptyTokenList.add(pushobj);
				}  
			} catch (Exception ex) {

				if (pushobj.status == null) {
					pushobj.status = "failed";
					pushobj.description = ex.getMessage();
					logger.error(requestId + " - Send BroadcastMessage Failed : " + ex.getMessage() + " : "
							+ pushobj.toString());
				}
				logger.error(requestId + "- " + ex.getMessage(), ex);
			}
		}

		long t4 = System.currentTimeMillis();
/*		
		switch (eventType) {
		case Util.EVENT_TYPE_APP:
//todo  remove below updateAppTokenList and push json into redis to update same. 				
			if (newTokenList.size() > 0) {
				//mysqlDao.updateAppTokenList(cid, newTokenList, identifiedFlag, requestId, Util.OS_TYPE_ANDROID);
				//mongoDao.updateWebToken(cid, pushobj.getToken(), canonicalRegId, Util.EVENT_TYPE_APP);
				 //hashmap = Util.createHashToken(cid,  newTokenList, identifiedFlag, Util.EVENT_TYPE_APP, Util.OS_TYPE_ANDROID, token_canonicalRegId_map);
				logger.info("HASH : " + hashmap );
			}
			if (invalidTokenList.size() > 0) {
//todo  remove below fuinction (delete invalid token in batch) and push json into redis for deletion 				
				//mysqlDao.deleteInvalidAppTokenBatch(cid, invalidTokenList, identifiedFlag, Util.OS_TYPE_ANDROID,requestId);
				//hashmap = Util.addListHash(invalidTokenList, hashmap);
			}
			 
			break;
		case Util.EVENT_TYPE_WEB:
			if (newTokenList.size() > 0) {
//todo  remove below updateWebTokenList and push json into redis to update same.				
				//mysqlDao.updateWebTokenList(cid, newTokenList, identifiedFlag, requestId, Util.BROWSER_TYPE_CHROME);
				//mongoDao.updateWebToken(cid, pushobj.getToken(), canonicalRegId, Util.EVENT_TYPE_WEB);
				hashmap = Util.createHashToken(cid,  newTokenList, identifiedFlag, Util.EVENT_TYPE_WEB, Util.BROWSER_TYPE_CHROME, token_canonicalRegId_map);
			}
			if (invalidTokenList.size() > 0) {
//todo  remove below fuinction (delete invalid token in batch) and push json into redis for deletion				
				//mysqlDao.deleteInvalidWebToken(cid, invalidTokenList, identifiedFlag, Util.BROWSER_TYPE_CHROME, requestId);
				hashmap = Util.addListHash(invalidTokenList, hashmap);
			}
			
			break;
		}
		//mst += System.currentTimeMillis() - t4;

		long t7 = System.currentTimeMillis();
		if (EmptyTokenList.size() > 0) {
//todo remove function and push json to redis 			
			//mysqlDao.disableGuidBatch(cid, EmptyTokenList, identifiedFlag, requestId);
			hashmap = Util.addListHash(EmptyTokenList, hashmap);
		}
		//mst += System.currentTimeMillis() - t7;
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(hashmap);
		redisDao.publishPNBC("PR1_PN_BC_TR",json);  //new Queuename to be decided
*/
		pnr.setPushNotificationsubList(pushNotifications);
		//pnr.setMysqlTime(mst);
		//pnr.setMongoTime(mot);
		pnr.setRedisTime(ret);
		pnr.setSentTime(st);
		return pnr;
	}
	

	@Override
	public PushNotificationRequest sendBroadCast(List<PushNotification> pushNotifications, String trid, String title,
			String message, Map<String,String> customPayload,String icon, String imageUrl, String deepLink, Boolean soundEnabled, String data,
			String collapse_key, int cid, String appid, int ttl, String eventType, int identifiedFlag,
			List<Map<String, String>> actions, boolean autohide, List<Map<String, String>> actionButton,
			List<Map<String, String>> carousel, String requestId, String customEventType, PushNotificationRequest pnr) throws IOException {
		if (title == null)
			title = "";
		if (message == null)
			message = "";
		if (soundEnabled == null)
			soundEnabled = true;
		if (collapse_key == null)
			collapse_key = "do_not_collapse";
		if (data == null)
			data = "{}";
		
		long ret;
		//long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(), mot = pnr.getMongoTime(), st = pnr.getSentTime(); 
		long mst = ret = pnr.getRedisTime(), st = pnr.getSentTime();
/*		
		long t1 = System.currentTimeMillis();
		if(pushNotifications.get(0).getToken() == null || pushNotifications.get(0).getToken().trim().isEmpty()) {
		pushNotifications = eventType.equals(Util.EVENT_TYPE_APP)
				? mysqlDao.getAppTokensForGuids(cid, pushNotifications, Util.OS_TYPE_ANDROID, identifiedFlag, requestId)
				: mysqlDao.getWebTokensForGuids(cid, pushNotifications, Util.BROWSER_TYPE_CHROME, identifiedFlag,
						requestId); 
		}
		mst += System.currentTimeMillis() - t1;
*/		

		List<String> regIds = new ArrayList<String>();
		Map<String,PushNotification> token_pushobj_map = new HashMap<String,PushNotification>();
		List<PushNotification> EmptyTokenList = new ArrayList<PushNotification>();
		List<PushNotification> invalidTokenList = new ArrayList<PushNotification>();
		List<PushNotification> newTokenList = new ArrayList<PushNotification>();
				
		for(PushNotification pn : pushNotifications){
			if(pn.token != null){
				regIds.add(pn.token);
				token_pushobj_map.put(pn.token, pn);
			} else {
				pn.status = "failed";
				pn.description = "token not found for pushid";
				//EmptyTokenList.add(pn);				
			}
		}
		long t2 = System.currentTimeMillis();
				
		
		//String serverAndApiKey = eventType.equals(Util.EVENT_TYPE_APP) ? mysqlDao.getAndroidApiKey(cid, appid)
		//		: mysqlDao.getChromeApiKey(cid, appid);
		mst += System.currentTimeMillis() - t2;
		
		String serverAndApiKey = pnr.getApikey();
		String serverGCMorFCM = serverAndApiKey.substring(0, 3);
		String apiKey = serverAndApiKey.substring(3, serverAndApiKey.length());

		Sender sender = getSender(apiKey, eventType,serverGCMorFCM);
		Message msg = buildGCMMessage(trid, title, message,icon, imageUrl, deepLink, soundEnabled, data, collapse_key,cid, appid, ttl, eventType, serverGCMorFCM, actions, autohide, actionButton, carousel, customPayload, customEventType, false);

		try {
			long t6 = System.currentTimeMillis();
			MulticastResult multiCastResults = sender.send(msg, regIds, RETRIES);
			st += System.currentTimeMillis() - t6;

			for (int i = 0; i < multiCastResults.getTotal(); i++) {
				Result res = multiCastResults.getResults().get(i);
				PushNotification pushobj = token_pushobj_map.get(regIds.get(i));
				pushobj.setNewtoken(pushobj.getToken());
				if (res.getMessageId() != null) {
					String canonicalRegId = res.getCanonicalRegistrationId();
					pushobj.status = "sent";
					if (canonicalRegId != null) {
						pushobj.setNewtoken(canonicalRegId);
/*						long t5 = System.currentTimeMillis();
						switch (eventType) {
						case Util.EVENT_TYPE_APP:
							newTokenList.add(pushobj);							
							//mongoDao.updateWebToken(cid, regIds.get(i), canonicalRegId, Util.EVENT_TYPE_APP);
							//token_canonicalRegId_map.put(pushobj.getToken(),pushobj.getNewtoken());
							break;
						case Util.EVENT_TYPE_WEB:
							newTokenList.add(pushobj);
							//mongoDao.updateWebToken(cid, regIds.get(i), canonicalRegId, Util.EVENT_TYPE_WEB);
							//token_canonicalRegId_map.put(pushobj.getToken(),pushobj.getNewtoken());
							break;
						}
						//mot += System.currentTimeMillis() - t5;
						 */
					}
					if (eventType.equals(Util.EVENT_TYPE_APP) && !title.equals(Util.NETCORE_UNINSTALL)) {
						ret += Util.pushAmpData(msg.getData().get("data"), pushobj, appid);
					}
					logger.info(requestId + " - Send MulticastMessage Success : " + pushobj.toString());
				} else {
					String error = res.getErrorCodeName();
					pushobj.status = "failed";
					pushobj.description = error;
					if (error.equals("Unregistered Device") || error.equals("InvalidRegistration")	|| error.equals("NotRegistered")) {
						//invalidTokenList.add(pushobj);
						pushobj.invalidToken = true;
					}
					logger.error(requestId + " - Send MulticastMessage Failed : " + error + " : " + pushobj.toString());
				}
			}
/*			
			long t4 = System.currentTimeMillis();
			switch (eventType) {
			case Util.EVENT_TYPE_APP:
				if (newTokenList.size() > 0) {
					//mysqlDao.updateAppTokenList(cid, newTokenList, identifiedFlag, requestId, Util.OS_TYPE_ANDROID);
			
				}
				if (invalidTokenList.size() > 0) {
					//mysqlDao.deleteInvalidAppTokenBatch(cid, invalidTokenList, identifiedFlag, Util.OS_TYPE_ANDROID,requestId);
				}
				break;
			case Util.EVENT_TYPE_WEB:
				if (newTokenList.size() > 0) {
					//mysqlDao.updateWebTokenList(cid, newTokenList, identifiedFlag, requestId, Util.BROWSER_TYPE_CHROME);
				}
				if (invalidTokenList.size() > 0) {
					//mysqlDao.deleteInvalidWebToken(cid, invalidTokenList, identifiedFlag, Util.BROWSER_TYPE_CHROME, requestId);
				}
				break;
			}
			mst += System.currentTimeMillis() - t4;
			
			if(EmptyTokenList.size() > 0) {		
				//mysqlDao.disableGuidBatch(cid, EmptyTokenList, identifiedFlag, requestId);
			}
*/			
		} catch (Exception ex) {
			for (PushNotification pn : pushNotifications) {
				if (pn.status == null) {
					pn.status = "failed";
					pn.description = ex.getMessage();
					logger.error(
							requestId + " - Send MulticastMessage Failed : " + ex.getMessage() + " : " + pn.toString());
				}
			}
			logger.error(requestId + "- " + ex.getMessage(), ex);
		}
		 
		pnr.setPushNotificationsubList(pushNotifications);
		//pnr.setMysqlTime(mst);
		//pnr.setMongoTime(mot);
		pnr.setRedisTime(ret);
		pnr.setSentTime(st);
		return pnr;
	}

	private Message buildGCMMessage(String trid, String title, String message,String icon, String imageUrl,String deepLink, Boolean soundEnabled, String data, String collapse_key, int cid, String appid, int ttl,String eventType, String serverType, List<Map<String, String>> actions,boolean autohide,List<Map<String, String>> actionButton, List<Map<String, String>> carousel, Map<String, String> customPayload, String customEventType,  boolean testPush){

		boolean dryrun = title.equals(Util.NETCORE_UNINSTALL) ? true : false;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		if(customEventType != null && customEventType.equals(Util.EVENT_TYPE_ICICI_APP)){
        	       	
        	//.addData("com.xtify.sdk.NOTIF_ACTION_TYPE", "com.xtify.sdk.OPEN_APP")        	
        	//.addData("com.xtify.sdk.NOTIF_ACTION_DATA", "")
        	//.addData("com.xtify.sdk.NOTIF_ID", "")
			
        	Message.Builder builder = new Message.Builder();
        	builder.addData("com.xtify.sdk.NOTIFICATION_TITLE", title);
        	builder.addData("com.xtify.sdk.NOTIFICATION_CONTENT", message);
        	
        	if(imageUrl !=null && imageUrl.length() > 0) {
        		builder.addData("data.com.xtify.notification.STYLE", "BIG_PICTURE");
        		builder.addData("data.com.xtify.notification.BIG_PICTURE", imageUrl);
        	}
        	if(deepLink != null && deepLink.length() > 0) {
        		builder.addData("com.xtify.sdk.NOTIF_ACTION_TYPE", "com.xtify.sdk.OPEN_APP");
        		builder.addData("com.xtify.sdk.NOTIF_ACTION_DATA", deepLink);
        	}
        	
        	Message msg = builder.build();        	      	
        		
        	logger.info(msg);
    		return msg;
        }      
        	
		Message msg = null;
                Message.Builder msgBuilder = null;
		if (eventType.equals(Util.EVENT_TYPE_APP)){
	        Notification notification = null;
	        /*if(Util.FCM_SERVER.equalsIgnoreCase(serverType))
	        	notification = new Notification.Builder(null).title(title).body(message).build();*/
	        if(title.equals(Util.NETCORE_UNINSTALL)) dryrun = true;
	        
	        Map<String, Object> notMap = new HashMap();
	        
	        notMap.put("sound", soundEnabled);
	        
	        if(deepLink!=null)
        		notMap.put("deeplink", deepLink);
        	
        	if(imageUrl !=null)
        		notMap.put("image", imageUrl);
        	
        	if(actionButton != null)
        		notMap.put("actionButton", actionButton);
        	
        	if(carousel != null)
        		notMap.put("carousel", carousel);
        	
        	if (ttl>0)
				notMap.put("expiry", timestamp.getTime() / 1000 +ttl);
        	
        	

	        
	        if(Util.FCM_SERVER.equalsIgnoreCase(serverType)){
	        	notMap.put("title", title);
	        	notMap.put("message", message);
	        	notMap.put("trid", trid);
	        	if (customPayload != null)
	        		notMap.put("customPayload", customPayload);
	        	
	        	Gson gson = new Gson(); 
	        	String payload = gson.toJson(notMap); 

	        	msgBuilder = new Message.Builder()
	        			.timeToLive(ttl)
	    	        	.addData("data",payload)
	    	           	.addData("trid", trid.isEmpty() ? null : trid)
	    	        	.dryRun(dryrun);
	        }
	        else{
	        	Gson gson = new Gson(); 
				String payload = gson.toJson(notMap); 
	        	
				msgBuilder = new Message.Builder()
						.addData("action", payload)
						.addData("trid", trid.isEmpty() ? null : trid)
						.addData("title",title)
						.addData("msgtext",message)
						.addData("data", data)
						.timeToLive(ttl)
						.notification(notification)
						.dryRun(dryrun);
	        }
	        
	        if(!testPush){
        		msgBuilder.collapseKey(Util.getTrIDDetails(trid)[1]);
        	}
        	
        	msg = msgBuilder.build();
		}
		else{        
			Map<String, Object> dataMap = new HashMap();
	    	dataMap.put("trid", trid);
	    	dataMap.put("click_action", deepLink);
	    	dataMap.put("autohide", autohide);
	    	
	    	Map<String, Object> notMap = new HashMap();
	    	notMap.put("title", title);
	    	notMap.put("body", message);
	    	notMap.put("icon", icon);
	    	notMap.put("image", imageUrl);
	    	notMap.put("data", dataMap);
	    	notMap.put("actions", actions);
	    	
	    	if(!testPush){
	    		notMap.put("tag", Util.getTrIDDetails(trid)[1]);
	    	}
	    	
	    	Gson gson = new Gson(); 
			String payload = gson.toJson(notMap); 
						
	    	msg = new Message.Builder()
	    		.priority(Message.Priority.HIGH)
	    	//	.contentAvailable(true)
	            .delayWhileIdle(true)
	            .timeToLive(ttl)
	    		.addData("notification",payload)
	    		.dryRun(dryrun)
	    		.build();
		}

		logger.info(msg);
		return msg;
	}

	/**
	 * Method implemented on top of GCM Sender.java implements backoff and retry
	 */
	@Override
	public PushNotificationRequest sendSingleMessage(PushNotification pn, String trid, String title, String message,
			String icon, String imageUrl, String deepLink, Boolean soundEnabled, String data, String collapse_key,
			int cid, String appid, int ttl, String eventType, int identifiedFlag, List<Map<String, String>> actions,
			boolean autohide, List<Map<String, String>> actionButton, List<Map<String, String>> carousel, Map<String, String> customPayload,
			String requestId, String customEventType, PushNotificationRequest pnr, int devHostFlag) throws IOException, InterruptedException {
		
		long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(), mot = pnr.getMongoTime(), st = pnr.getSentTime(); 
		
		if (title == null)
			title = "";
		if (message == null)
			message = "";
		if (soundEnabled == null)
			soundEnabled = true;
		if (collapse_key == null)
			collapse_key = "do_not_collapse";
		if (data == null)
			data = "{}";
		
		long t1 = System.currentTimeMillis();
		String serverAndApiKey = eventType.equals(Util.EVENT_TYPE_APP) ? mysqlDao.getAndroidApiKey(cid, appid)
				: mysqlDao.getChromeApiKey(cid, appid);
		mst += System.currentTimeMillis() - t1;
		//String serverAndApiKey = pnr.getApikey();
		String serverGCMorFCM = serverAndApiKey.substring(0, 3);
		String apiKey = serverAndApiKey.substring(3, serverAndApiKey.length());
		try {
			Sender sender = getSender(apiKey, eventType, serverGCMorFCM);
			
			long t2 = System.currentTimeMillis();
			if(pn.token == null || pn.token.isEmpty()){
			pn.token = eventType.equals(Util.EVENT_TYPE_APP) ? mysqlDao.getAppTokenForGuid(cid, pn.userid, pn.pushid, Util.OS_TYPE_ANDROID, identifiedFlag) : mysqlDao.getWebTokenForGuid(cid, pn.userid, pn.pushid, Util.BROWSER_TYPE_CHROME, identifiedFlag);
			}
			mst += System.currentTimeMillis() - t2;
			
			Message msg = buildGCMMessage(trid, title, message,icon, imageUrl, deepLink, soundEnabled, data, collapse_key, cid, appid, ttl, eventType, serverGCMorFCM, actions, autohide, actionButton, carousel, customPayload, customEventType, pn.testPush);
			long t10 = System.currentTimeMillis();
			Result result = sender.send(msg, pn.token, RETRIES);
			st += System.currentTimeMillis() - t10;
			pn.setNewtoken(pn.getToken());
			if (result.getMessageId() == null) {
				pn.status = "failed";
				String error = result.getErrorCodeName();
				pn.description = error;
				
				if(!pn.isTestPush()){
					if (error.equals("Unregistered Device") || error.equals("InvalidRegistration") || error.equals("NotRegistered")) {
					pn.invalidToken = true;
/*					
					long t3 = System.currentTimeMillis();
						switch (eventType) {
							case Util.EVENT_TYPE_APP:
								mysqlDao.deleteInvalidAppToken(cid, pn.userid, pn.pushid, pn.token, identifiedFlag, Util.OS_TYPE_ANDROID,requestId);
								break;
							case Util.EVENT_TYPE_WEB:
								mysqlDao.deleteInvalidWebToken(cid, pn.userid, pn.pushid, pn.token , identifiedFlag , Util.BROWSER_TYPE_CHROME ,requestId);
								break;
						}
					mst += System.currentTimeMillis() - t3;
*/						
				}
				}
				
				logger.error(requestId+ " - Send SingleMessage Failed : " + result.getErrorCodeName() + " : " + pn.toString());			} else {
				pn.status = "sent";
				if(!pn.isTestPush()){
				if (result.getCanonicalRegistrationId() != null) {
					pn.setNewtoken(result.getCanonicalRegistrationId());
/*					
					switch (eventType) {
					case Util.EVENT_TYPE_APP:
						
						long t5 = System.currentTimeMillis();
						mysqlDao.updateAppToken(cid, pn.token, result.getCanonicalRegistrationId(), identifiedFlag,requestId, Util.OS_TYPE_ANDROID);
						mst += System.currentTimeMillis() - t5;
						
						long t8 = System.currentTimeMillis();
						mongoDao.updateWebToken(cid, pn.token, result.getCanonicalRegistrationId(),
								Util.EVENT_TYPE_APP);
						mot += System.currentTimeMillis() - t8;
						
						break;
					case Util.EVENT_TYPE_WEB:
						
						long t6 = System.currentTimeMillis();
						mysqlDao.updateWebToken(cid, pn.token, result.getCanonicalRegistrationId(), identifiedFlag,requestId,Util.BROWSER_TYPE_CHROME);
						mst += System.currentTimeMillis() - t6;
						
						long t9 = System.currentTimeMillis();
						mongoDao.updateWebToken(cid, pn.token, result.getCanonicalRegistrationId(),
								Util.EVENT_TYPE_WEB);
						mot += System.currentTimeMillis() - t9;
							break;
						}
*/						
					}
					
					if (eventType.equals(Util.EVENT_TYPE_APP)) {
						Util.pushAmpData(msg.getData().get("data"), pn, appid);
					}
				}
				
				logger.info(requestId + " - Send SingleMessage Success : " + pn.toString());
			}
		} catch (EmptyResultDataAccessException edae) {
			pn.status = "failed";
			pn.description = "token not found for pushid";
			
			long t7 = System.currentTimeMillis();
			//mysqlDao.disableGuid(cid, pn.userid, pn.pushid, identifiedFlag, requestId);
			mst += System.currentTimeMillis() - t7;
			
			logger.error(requestId + " - Send SingleMessage Failed : " + pn.description + " : " + pn.toString());
		} catch (Exception e) {
			pn.status = "failed";
			pn.description = e.getMessage();
			logger.error(requestId + " - Send SingleMessage Failed : " + pn.description + " : " + pn.toString());
		}
		pnr.setPushNotification(pn);
		pnr.setMysqlTime(mst);
		pnr.setMongoTime(mot);
		pnr.setRedisTime(ret);
		pnr.setSentTime(st);
		
		return pnr;
	}
}
