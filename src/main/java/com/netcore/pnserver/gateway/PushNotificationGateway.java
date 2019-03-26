package com.netcore.pnserver.gateway;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.pojo.PushNotificationRequest;

public interface PushNotificationGateway {
	
	final static Logger logger = Logger.getLogger(PushNotificationGateway.class);
	

	public PushNotificationRequest sendSingleMessage(PushNotification pn, String trid, String title, String message, String icon, String imageUrl, String deepLink, Boolean soundEnabled, String data, String collapse_key, int cid, String appid, int ttl, String eventType, int indentifiedFlag, List<Map<String, String>> actions, boolean autohide, List<Map<String, String>> actionButton, List<Map<String, String>> carousel, Map<String, String> customPayload, String requestId, String customEventType, PushNotificationRequest pnr, int devHostFlag) throws IOException, InterruptedException;
	
	public PushNotificationRequest sendBroadCast(List<PushNotification> pushNotifications, String trid, String title, String message, Map<String, String> customPayload, String icon, String imageUrl, String deepLink, Boolean soundEnabled, String data, String collapse_key, int cid, String appid, int ttl, String eventType, int indentifiedFlag, List<Map<String, String>> actions, boolean autohide, List<Map<String, String>> actionButton, List<Map<String, String>> carousel, String requestId, String customEventType, PushNotificationRequest pnr) throws IOException;
	
	public PushNotificationRequest sendMultiCast(List<PushNotification> pushNotifications, String icon, Boolean soundEnabled, String data, String collapse_key, int cid, String appid, int ttl, String eventType, int indentifiedFlag, List<Map<String, String>> actions, boolean autohide, List<Map<String, String>> actionButton, List<Map<String, String>> carousel, String requestId, String customEventType, PushNotificationRequest pnr) throws IOException;

}
