package com.netcore.pnserver.gateway;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;

import io.netty.util.concurrent.Future;

public class TestApns {

	static String getTopicName(String p12Path, String p12Pass) throws Exception {
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
		return subjectMap.get("UID");
	}

	static ApnsClient getClient(String p12Path, String p12Pass) throws IOException, InterruptedException {
		return new ApnsClientBuilder().setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
				.setClientCredentials(new File(p12Path), p12Pass).build();
	}

	// static String buildMessage(String title, String message, String deeplink) {
	// final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
	// payloadBuilder.setAlertBody(message);
	// payloadBuilder.setAlertTitle(title);
	// payloadBuilder.setActionButtonLabel("View");
	// String urlargs[] = {deeplink};
	// payloadBuilder.setUrlArguments(urlargs);
	// return payloadBuilder.buildWithDefaultMaximumLength();
	// }

	static String buildMessage(String title, String message, String deeplink) {

		List<Map<String, String>> actionButton = new ArrayList<Map<String, String>>();
		List<Map<String, String>> carousel = new ArrayList<Map<String, String>>();

		final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
		payloadBuilder.setAlertBody(message);
		payloadBuilder.setAlertTitle(title);
		payloadBuilder.setActionButtonLabel("View");
		String urlargs[] = { deeplink };
		payloadBuilder.setUrlArguments(urlargs);

		payloadBuilder.setMutableContent(true);
		payloadBuilder.setContentAvailable(true);
		payloadBuilder.setCategoryName("smartechPush");

		Map<String, Object> customPayload = new HashMap<String, Object>();
		customPayload.put("trid", "12345");
		customPayload.put("deeplink", deeplink == null ? "" : deeplink);
		customPayload.put("mediaurl", deeplink == null ? "" : deeplink);
		customPayload.put("actionButton", actionButton == null ? "[]" : actionButton);
		customPayload.put("carousel", carousel == null ? "[]" : carousel);
		payloadBuilder.addCustomProperty("payload", customPayload);

		return payloadBuilder.buildWithDefaultMaximumLength();
	}

	static Date getInvalidationDatetime(int ttl) {
		long ttlmillisecs = System.currentTimeMillis() + (ttl * 1000);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(ttlmillisecs);
		return calendar.getTime();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			String token = "00955ACB3497AB3DD41FE57660AB5CDECB070B15AA49DD991ACA19CAD58C3117";
			String p12Path = "/home/hemant/Downloads/web.koovs.webapp.p12";
			String p12Pass = "koovs@123";
			String deeplink = "women/tags/vip-half-price-store?utm_source=netcore&utm_medium=fixed2&utm_campaign=220618_w_halfpricestore&__sta=vhg.uosvmhkxIBsvbskqsfomllp%7CBBU&__stm_medium=bpn&__stm_source=smartech";
			System.out.println("Creating client");
			ApnsClient apnsClient = getClient(p12Path, p12Pass);
			String topic = getTopicName(p12Path, p12Pass);

			System.out.println("topic: " + topic);			

			final String payload = buildMessage("Hello Hemant", "Welcome to APNS", deeplink);
			token = TokenUtil.sanitizeTokenString(token);
			Date invalidationTime = getInvalidationDatetime(1500);

			System.out.println("payload: " + payload);

			final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, topic, payload,
					invalidationTime, DeliveryPriority.IMMEDIATE, "123");
			final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture = apnsClient
					.sendNotification(pushNotification);

			final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = sendNotificationFuture
					.get();
			if (pushNotificationResponse.isAccepted()) {
				System.out.println("Accepted ");				
				System.out.println("reason =" + pushNotificationResponse.getRejectionReason() + ", Expiry Time ="
						+ pushNotificationResponse.getTokenInvalidationTimestamp());
			} else {
				System.out.println("Rejected Reason: " + pushNotificationResponse.getRejectionReason());
				System.out.println("reason =" + pushNotificationResponse.getRejectionReason() + ", Expiry Time ="
						+ pushNotificationResponse.getTokenInvalidationTimestamp());
				if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
					System.out
							.println("Token Invalid from :" + pushNotificationResponse.getTokenInvalidationTimestamp());
				}
			}

			apnsClient.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
