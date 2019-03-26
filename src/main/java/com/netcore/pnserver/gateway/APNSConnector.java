package com.netcore.pnserver.gateway;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.springframework.dao.EmptyResultDataAccessException;

import com.netcore.pnserver.dao.MySqlDao;
import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.pojo.PushNotificationRequest;
import com.netcore.pnserver.util.Util;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;

public class APNSConnector implements PushNotificationGateway {
	private MySqlDao mysqlDao;
	private RedisDao redisDao;
	private Util util;
	public static Map<String, ApnsClient> clientMap = new HashMap<String, ApnsClient>();
	public static Map<String, String> topicMap = new HashMap<String, String>();

	public APNSConnector(MySqlDao mysqlDao, Util util) {
		this.mysqlDao = mysqlDao;
		this.util = util;
	}

	private String getApnsHost(int devHostFlag) {
		String apns_host;
		if (devHostFlag == 1) {
			apns_host = Util.DEVELOPER_APNS_HOST;
		}else if(devHostFlag == 0) {
			apns_host = Util.PRODUCTION_APNS_HOST;
		}else {
			throw new RuntimeException("Unknown developer flag:"+ devHostFlag +" Please provide valid developer code " );
		}
		return apns_host;
	}
	
	
	private ApnsClient getClient(int cid, String appid, String eventType, int devHostFlag) throws IOException, InterruptedException {
		String clientKey = String.format("%s_%s_%s", cid, appid, devHostFlag);
		ApnsClient client = clientMap.get(clientKey);
		if (client == null) {
			Map<String, Object> p12 = util.getIOSCertFile(cid, appid, eventType);
			String p12Url = (String) p12.get("ios_p12");
			String p12pass = (String) p12.get("ios_pass");
			if (p12Url == null || p12Url.isEmpty() || p12pass == null || p12pass.isEmpty())
				throw new RuntimeException("p12Url:" + p12Url + " or p12pass:" + p12pass + " is null/empty");
			String p12Path = Util.downLoadp12(p12Url);
			final ApnsClient apnsClient = new ApnsClientBuilder().setApnsServer(getApnsHost(devHostFlag))
					.setClientCredentials(new File(p12Path), p12pass).build();
			clientMap.put(clientKey, apnsClient);
			logger.info("APNS client created : " + clientKey);
		}

		if (topicMap.get(appid) == null) {
			try {
				Map<String, Object> p12 = util.getIOSCertFile(cid, appid, eventType);
				String p12Url = (String) p12.get("ios_p12");
				String p12pass = (String) p12.get("ios_pass");
				if (p12Url == null || p12Url.isEmpty() || p12pass == null || p12pass.isEmpty())
					throw new RuntimeException("p12Url:" + p12Url + " or p12pass:" + p12pass + " is null/empty");
				p12Url = p12Url.replace("/var/www/html", "");
				String p12Path = Util.downLoadp12(p12Url);
				setTopicName(appid, p12Path, p12pass);
			} catch (Exception e) {
				logger.error("Could not set Topic name :" + e.getMessage());
			}

		}

		return clientMap.get(clientKey);
	}

	

	private Date getInvalidationDatetime(int ttl) {
		long ttlmillisecs = System.currentTimeMillis() + (ttl * 1000);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(ttlmillisecs);
		return calendar.getTime();
	}

	// TODO retrieve multiple topics from the certificate
	private void setTopicName(String appid, String p12Path, String p12Pass) throws Exception {
		KeyStore p12 = KeyStore.getInstance("pkcs12");
		p12.load(new FileInputStream(p12Path), p12Pass.toCharArray());
		Enumeration e = p12.aliases();
		Map<String, String> subjectMap = new HashMap<String, String>();
		while (e.hasMoreElements()) {
			String alias = (String) e.nextElement();
			X509Certificate c = (X509Certificate) p12.getCertificate(alias);
			Principal subject = c.getSubjectDN();
			String subjectArray[] = subject.toString().split(",");
			for (String s : subjectArray) {
				String[] str = s.trim().split("=");
				subjectMap.put(str[0], str[1]);
			}
		}
		topicMap.put(appid, subjectMap.get("UID"));
	}

	@Override
	public PushNotificationRequest sendSingleMessage(PushNotification pn, String trid, String title, String message,
			String icon, String imageUrl, String deepLink, Boolean soundEnabled, String data, String collapse_key,
			int cid, String appid, int ttl, String eventType, int identifiedFlag, List<Map<String, String>> actions,
			boolean autohide, List<Map<String, String>> actionButton, List<Map<String, String>> carousel,Map<String, String> customPayload,
			String requestId, String customEventType, PushNotificationRequest pnr, int devHostFlag) throws IOException, InterruptedException {
		
		long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(), mot = pnr.getMongoTime(), st = pnr.getSentTime(); 
		

		try {
			ApnsClient apnsClient = getClient(cid, appid, eventType,devHostFlag);
			final String payload = buildMessage(trid, title, message, deepLink, imageUrl, actionButton, carousel, customPayload, eventType, ttl);

			long t1 = System.currentTimeMillis();
			if(pn.token == null || pn.token.isEmpty()){
				pn.token = eventType.equals(Util.EVENT_TYPE_APP) ? mysqlDao.getAppTokenForGuid(cid, pn.userid, pn.pushid, Util.OS_TYPE_IOS, identifiedFlag) : mysqlDao.getWebTokenForGuid(cid, pn.userid, pn.pushid, Util.BROWSER_TYPE_SAFARI, identifiedFlag);					
			}
			mst += System.currentTimeMillis() - t1;
			
			
			pn.token = TokenUtil.sanitizeTokenString(pn.token);

			if (pn.token == null || pn.token.trim().isEmpty()) {
				pn.status = "failed";
				pn.description = "token not found for pushid";
				
				long t2 = System.currentTimeMillis();
				mst += System.currentTimeMillis() - t2;
				
				logger.error(
						requestId + " - APNS Send SingleMessage Failed : " + pn.description + " : " + pn.toString());
				pnr.setMysqlTime(mst);
				pnr.setMongoTime(mot);
				pnr.setRedisTime(ret);
				pnr.setSentTime(st);
				return pnr;
			}
			logger.info("Token is:" + pn.token);
			pn.setNewtoken(pn.getToken());	
			Date invalidationTime = getInvalidationDatetime(ttl);
			final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(pn.token,
					topicMap.get(appid), payload, invalidationTime, DeliveryPriority.IMMEDIATE, collapse_key);
			
			long t5 = System.currentTimeMillis();
			final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture = apnsClient
					.sendNotification(pushNotification);
			try {
				final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = sendNotificationFuture
						.get();
				st += System.currentTimeMillis() - t5;
				
				if (pushNotificationResponse.isAccepted()) {
					logger.info(requestId + " - APNS Send SingleMessage Success : " + pn.toString());
					pn.status = "sent";
					if (!pn.isTestPush() && eventType.equals(Util.EVENT_TYPE_APP) && !title.equals(Util.NETCORE_UNINSTALL)) {
						ret += Util.pushAmpData(payload, pn, appid);
					}
				} else {
					logger.error(requestId + " - APNS Send SingleMessage Failed : "
							+ pushNotificationResponse.getRejectionReason() + " : " + pn.toString());
					pn.status = "failed";
					pn.description = pushNotificationResponse.getRejectionReason();
					if (Util.APNS_RESPONSE_BAD_DEVICE_TOKEN.equals(pn.description.trim())) {
						//invalidTokenList.add(pn);
						pn.invalidToken = true;
					}
					if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
						logger.info(requestId + " - IOS Token is invalid as of "+ pushNotificationResponse.getTokenInvalidationTimestamp());
						
						long t3 = System.currentTimeMillis();
						if(!pn.isTestPush()){
							pn.invalidToken= true;
							
						}
						
						mst += System.currentTimeMillis() - t3;
					}
				}
			} catch (final ExecutionException e) {
				pn = logError(e, pn, requestId);
				System.err.println("Failed to send push notification.");
				logger.error(requestId + " - " + e.getMessage());

			}
		} catch (EmptyResultDataAccessException e) {
			pn.status = "failed";
			pn.description = "token not found for pushid";
			
			logger.error(requestId + " - APNS Send SingleMessage Failed : " + pn.description + " : " + pn.toString());
		} catch (Exception e) {
			pn = logError(e, pn, requestId);
		}
		pnr.setPushNotification(pn);
		pnr.setMysqlTime(mst);
		pnr.setMongoTime(mot);
		pnr.setRedisTime(ret);
		pnr.setSentTime(st);
		
		return pnr;
	}

	private PushNotification logError(Exception e, PushNotification pn, String requestId) {
		pn.status = "failed";
		pn.description = e.getMessage();
		logger.error(requestId + " - APNS Send SingleMessage Failed : " + pn.description + " : " + pn.toString());
		logger.error(requestId + " - " + e.getMessage(), e);
		return pn;
	}

	public void shutdown() {
		for (String clientKey : clientMap.keySet()) {
			try {
				ApnsClient apnsClient = clientMap.get(clientKey);
				final Future<Void> disconnectFuture = apnsClient.close();
				disconnectFuture.await();
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public PushNotificationRequest sendMultiCast(List<PushNotification> pns, String icon, Boolean soundEnabled,
			String data, String collapse_key, int cid, String appid, int ttl, String eventType, int identifiedFlag,
			List<Map<String, String>> actions, boolean autohide, List<Map<String, String>> actionButton,
			List<Map<String, String>> carousel, String requestId,String customEventType, PushNotificationRequest pnr)
			throws IOException {

		long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(), mot = pnr.getMongoTime(), st = pnr.getSentTime();
		List<Long> redisTimeList = new ArrayList<Long>();

		try {			
			ApnsClient apnsClient = getClient(cid, appid, eventType,pnr.getDeveloper());
			List<PushNotification> EmptyTokenList = new ArrayList<PushNotification>();
			List<PushNotification> invalidTokenList = new ArrayList<PushNotification>();

			long t1 = System.currentTimeMillis();

			Date invalidationTime = getInvalidationDatetime(ttl);

			final CountDownLatch countDownLatch = new CountDownLatch(pns.size());
			logger.info(requestId + " - Set countDownLatch count " + countDownLatch.getCount());
			for (PushNotification pn : pns) {
				try {

					if (pn.token != null) {

						pn.token = TokenUtil.sanitizeTokenString(pn.token);

						final String payload = buildMessage(pn.getTrid(), pn.getTitle(), pn.getMessage(),
								pn.getDeepLink(), pn.getImageURL(), actionButton, carousel, pn.getCustomPayload(), eventType, ttl);

						SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(pn.token,
								topicMap.get(appid), payload, invalidationTime, DeliveryPriority.IMMEDIATE,
								collapse_key);

						long t4 = System.currentTimeMillis();
						pn.setNewtoken(pn.getToken());
						final Future<PushNotificationResponse<SimpleApnsPushNotification>> future = apnsClient
								.sendNotification(pushNotification);
						st += System.currentTimeMillis() - t4;

						future.addListener(
								new GenericFutureListener<Future<PushNotificationResponse<SimpleApnsPushNotification>>>() {
									@Override
									public void operationComplete(
											final Future<PushNotificationResponse<SimpleApnsPushNotification>> future)
											throws Exception {
										if (future.isSuccess()) {
											final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = future
													.get();
											if (pushNotificationResponse.isAccepted()) {
												logger.info(requestId + " - APNS Send MulticastMessage Success : "
														+ pn.toString());
												pn.status = "sent";
												if (eventType.equals(Util.EVENT_TYPE_APP)) {
													redisTimeList.add(Util.pushAmpData(payload, pn, appid));
												}
											} else {
												logger.error(requestId + " - APNS Send MulticastMessage Failed : "
														+ pushNotificationResponse.getRejectionReason() + " : "
														+ pn.toString());
												pn.status = "failed";
												pn.description = pushNotificationResponse.getRejectionReason();
												if (Util.APNS_RESPONSE_BAD_DEVICE_TOKEN.equals(pn.description.trim())) {
													pn.invalidToken = true;
												}
												if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
													logger.info("IOS Token is invalid as of "
															+ pushNotificationResponse.getTokenInvalidationTimestamp());
													pn.invalidToken = true;
												}
											}

										} else {
											logger.error(requestId + " - Future fails because of: " + future.cause());
											future.cause().printStackTrace();
											pn.status = "failed";
										}
										countDownLatch.countDown();
									}
								});

					} else {
						pn.status = "failed";
						pn.description = "token not found for pushid";
					}

				} catch (Exception ex) {
					if (pn.status == null) {
						pn.status = "failed";
						pn.description = ex.getMessage();
					}
					logger.error(requestId + " - " + ex.getMessage(), ex);
				}

			}

			long t2 = System.currentTimeMillis();

			logger.info(requestId + " - Waiting on countDownLatch.await()");
			countDownLatch.await(5, TimeUnit.SECONDS);
			logger.info(requestId + " - After processing countDownLatch count " + countDownLatch.getCount()
					+ " before :" + pns.size());
		} catch (Exception e) {
			for (PushNotification pn : pns) {
				if (pn.status == null) {
					pn.status = "failed";
					pn.description = e.getMessage();
				}
			}
			logger.error(requestId + " - " + e.getMessage(), e);
		}

		for (Long tm : redisTimeList) {
			ret += tm;
		}

		pnr.setPushNotificationsubList(pns);
		pnr.setMysqlTime(mst);
		pnr.setMongoTime(mot);
		pnr.setRedisTime(ret);
		pnr.setSentTime(st);
		return pnr;
	}

	@Override
	public PushNotificationRequest sendBroadCast(List<PushNotification> pns, String trid, String title, String message,
			Map<String, String> customPayload, String icon, String imageUrl, String deepLink, Boolean soundEnabled, String data, String collapse_key,
			int cid, String appid, int ttl, String eventType, int identifiedFlag, List<Map<String, String>> actions,
			boolean autohide, List<Map<String, String>> actionButton, List<Map<String, String>> carousel,
			String requestId, String customEventType, PushNotificationRequest pnr) throws IOException {
	
		long mst = pnr.getMysqlTime(), ret = pnr.getRedisTime(),  mot = pnr.getMongoTime(), st = pnr.getSentTime(); 
		List<Long> redisTimeList = new ArrayList<Long>();
		try {
			final List<SimpleApnsPushNotification> pushNotifications = new ArrayList<>();
			ApnsClient apnsClient = getClient(cid, appid, eventType,pnr.getDeveloper());
			Map<String, PushNotification> pnMap = new HashMap<String, PushNotification>();
			List<PushNotification> EmptyTokenList = new ArrayList<PushNotification>();
			List<PushNotification> invalidTokenList = new ArrayList<PushNotification>();

			long t1 = System.currentTimeMillis();
			mst += System.currentTimeMillis() - t1;

			final String payload = buildMessage(trid, title, message, deepLink, imageUrl, actionButton, carousel, customPayload, eventType, ttl);

			Date invalidationTime = getInvalidationDatetime(ttl);

			for (PushNotification pn : pns) {
				if (pn.token != null) {
					pn.token = TokenUtil.sanitizeTokenString(pn.token);
					// TODO package name should be taken from DB
					pushNotifications.add(new SimpleApnsPushNotification(pn.token, topicMap.get(appid), payload,
							invalidationTime, DeliveryPriority.IMMEDIATE, collapse_key));
					pnMap.put(pn.token, pn);
					pn.setNewtoken(pn.getToken());
				} else {
					pn.status = "failed";
					pn.description = "token not found for pushid";
					//EmptyTokenList.add(pn);					
				}
			}

			final CountDownLatch countDownLatch = new CountDownLatch(pushNotifications.size());
			logger.info(requestId + " - Set countDownLatch count " + countDownLatch.getCount());
			for (final SimpleApnsPushNotification pushNotification : pushNotifications) {
				try {
					long t4 = System.currentTimeMillis();
					final Future<PushNotificationResponse<SimpleApnsPushNotification>> future = apnsClient
							.sendNotification(pushNotification);
					st += System.currentTimeMillis() - t4;
					
					future.addListener(
							new GenericFutureListener<Future<PushNotificationResponse<SimpleApnsPushNotification>>>() {
								@Override
								public void operationComplete(
										final Future<PushNotificationResponse<SimpleApnsPushNotification>> future)
										throws Exception {
									PushNotification pn = pnMap.get(pushNotification.getToken());
									if (future.isSuccess()) {
										final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = future
												.get();
										if (pushNotificationResponse.isAccepted()) {
											logger.info(requestId + " - APNS Send MulticastMessage Success : "
													+ pn.toString());
											pn.status = "sent";
											if (eventType.equals(Util.EVENT_TYPE_APP) && !title.equals(Util.NETCORE_UNINSTALL)) {
												redisTimeList.add(Util.pushAmpData(payload, pn, appid));
											}
										} else {
											logger.error(requestId + " - APNS Send MulticastMessage Failed : "
													+ pushNotificationResponse.getRejectionReason() + " : "
													+ pn.toString());
											pn.status = "failed";
											pn.description = pushNotificationResponse.getRejectionReason();
											if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
												logger.info("IOS Token is invalid as of "
														+ pushNotificationResponse.getTokenInvalidationTimestamp());
												pn.invalidToken = true;
											}
											if (Util.APNS_RESPONSE_BAD_DEVICE_TOKEN.equals(pn.description.trim())) {
												pn.invalidToken = true;
											}
										}

									} else {
										logger.error(requestId + " - Future fails because of: " + future.cause());
										future.cause().printStackTrace();
										pn.status = "failed";
									}
									countDownLatch.countDown();
								}
							});
				} catch (Exception ex) {
					PushNotification pn = pnMap.get(pushNotification.getToken());
					if (pn.status == null) {
						pn.status = "failed";
						pn.description = ex.getMessage();
					}
					logger.error(requestId + " - " + ex.getMessage(), ex);
				}

			}
			
			logger.info(requestId + " - Waiting on countDownLatch.await()");
			countDownLatch.await(5, TimeUnit.SECONDS);
			logger.info(requestId + " - After processing countDownLatch count " + countDownLatch.getCount()
					+ " before :" + pushNotifications.size());
		} catch (Exception e) {
			for (PushNotification pn : pns) {
				if (pn.status == null) {
					pn.status = "failed";
					pn.description = e.getMessage();
				}
			}
			logger.error(requestId + " - " + e.getMessage(), e);
		}
		
		for (Long tm : redisTimeList) {
			ret += tm;
		}
		
		pnr.setPushNotificationsubList(pns);
		pnr.setMysqlTime(mst);
		pnr.setMongoTime(mot);
		pnr.setRedisTime(ret);
		pnr.setSentTime(st);
		return pnr;
	}

	private String buildMessage(String trid, String title, String message, String deepLink, String imageUrl,
			List<Map<String, String>> actionButton, List<Map<String, String>> carousel, Map<String, String> customPayload, String eventType, int ttl) {
		final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		if (!title.trim().equals(Util.NETCORE_UNINSTALL)) {
			payloadBuilder.setAlertBody(message);
			payloadBuilder.setAlertTitle(title);
			payloadBuilder.setMutableContent(true);
			payloadBuilder.setContentAvailable(true);
			payloadBuilder.setCategoryName("smartechPush");
		}

		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("trid", trid);
		if (eventType.equals(Util.EVENT_TYPE_APP)) {
			dataMap.put("deeplink", deepLink == null ? "" : deepLink);
			dataMap.put("mediaurl", imageUrl == null ? "" : imageUrl);
			dataMap.put("actionButton", actionButton == null ? "[]" : actionButton);
			dataMap.put("carousel", carousel == null ? "[]" : carousel);
			dataMap.put("expiry", carousel == null ? "" : timestamp.getTime() /1000 + ttl);
		}
		payloadBuilder.addCustomProperty("payload", dataMap);
		if(customPayload != null )
		payloadBuilder.addCustomProperty("customPayload", customPayload);

		return payloadBuilder.buildWithDefaultMaximumLength();
	}
}	
	
