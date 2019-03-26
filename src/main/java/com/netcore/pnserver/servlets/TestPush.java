package com.netcore.pnserver.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.netcore.pnserver.gateway.APNSConnector;
import com.netcore.pnserver.gateway.GCMConnector;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.pojo.PushNotificationRequest;
import com.netcore.pnserver.util.ClientDBMap;
import com.netcore.pnserver.util.Util;

/**
 * Servlet implementation class TestPush
 */
@WebServlet("/smart_test_push")
public class TestPush extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private WebApplicationContext ctx;
	private final static Logger logger = Logger.getLogger(TestPush.class);
	private final static String REQUEST_PARAMS = "params";
	private final static String CLIENT_ID = "clientid";
	private final static String EVENT_TYPE = "eventtype";
	private final static String DATA = "data";
	private final static String PUSH = "push";
	private final static String APP_ID = "appid";
	private final static String OS_TYPE = "ostype";
	private final static String DEVELOPER = "developer";
	private final static String DEVICE_TOKEN = "token";
	private final static String PUSH_TITLE = "title";
	private final static String PUSH_MESSAGE = "content";
	private final static String PUSH_DEEP_LINK = "deeplink";
	private final static String PUSH_IMAGE_URL = "imageurl";
	private final static String PUSH_ICON = "icon";
	private final static String PUSH_AUTO_HIDE = "autohide";
	private final static String PUSH_ACTIONS = "actions";
	private final static String PUSH_CAROUSEL = "carousel";
	private final static String PUSH_CUSTOMPAYLOAD = "customPayload";
	private final static String STATUS = "status";
	private final static String MESSAGE = "message";
	private final static String PUSH_SENT = "sent";
	private final static String PUSH_FAILED = "failed";
	private final static String SUCCESS = "success";
	private final static String QUEUE_NAME_APP = "PR1_PN_BROADCAST";
	private final static String QUEUE_NAME_WEB = "PR1_PN_BROADCAST_WEB";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public void init() throws ServletException {
		ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
		logger.info("Context Loaded");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter pw = response.getWriter();

		JSONObject jsonResponseFinal = new JSONObject();
		JSONArray jsonResponseData = new JSONArray();
		JSONObject jsonRequest = new JSONObject(
				request.getParameter(REQUEST_PARAMS) != null && !request.getParameter(REQUEST_PARAMS).trim().isEmpty()
						? request.getParameter(REQUEST_PARAMS)
						: "{}");

		String requestId = UUID.randomUUID().toString();
		logger.info(requestId + " - " + "Received test push request: " + jsonRequest);

		try {

			int clientId = Integer.parseInt(jsonRequest.getString("clientid").trim());
			String eventType = jsonRequest.getString("eventtype").trim();
			int ttl = Util.getMaxTtl(jsonRequest.getInt("ttl"));
			logger.info("TTL ="+ttl);

			PushNotification pushNotification = new PushNotification(0, "", clientId, 0, 0, true);

			JSONArray jsonArrayData = jsonRequest.getJSONArray(DATA);

			for (int i = 0; i < jsonArrayData.length(); i++) {
				JSONObject jsonObjectData = jsonArrayData.getJSONObject(i);
				JSONArray jsonArrayPush = jsonObjectData.getJSONArray(PUSH);

				GCMConnector gcmConnector = null;
				APNSConnector apnsConnector = null;
				String appOrSiteId = jsonObjectData.getString(APP_ID).trim();
				String osType = jsonObjectData.getString(OS_TYPE).trim();
				int developer = jsonObjectData.getInt(DEVELOPER);
				JSONArray jsonResponsePush = new JSONArray();
				JSONObject jsonResponseApp = new JSONObject();
				jsonResponseApp.put(APP_ID, appOrSiteId);
				jsonResponseApp.put(OS_TYPE, osType);

				if (eventType.equals(Util.EVENT_TYPE_WEB) || osType.equals(Util.OS_TYPE_ANDROID)) {
					gcmConnector = (GCMConnector) ctx.getBean("01");
				} else {
					apnsConnector = (APNSConnector) ctx.getBean("10");
				}

				for (int j = 0; j < jsonArrayPush.length(); j++) {
					JSONObject jsonResponseToken = new JSONObject();
					JSONObject jsonObjectPush = jsonArrayPush.getJSONObject(j);

					String token = jsonObjectPush.getString(DEVICE_TOKEN).trim();
					String title = jsonObjectPush.optString(PUSH_TITLE).trim().replaceAll("\\\\", "");
					String message = jsonObjectPush.optString(PUSH_MESSAGE).trim().replaceAll("\\\\", "");
					String deeplink = jsonObjectPush.optString(PUSH_DEEP_LINK).trim();
					String imageUrl = jsonObjectPush.optString(PUSH_IMAGE_URL).trim();
					String icon = jsonObjectPush.optString(PUSH_ICON).trim();
					boolean autohide = Boolean.parseBoolean(jsonObjectPush.optString(PUSH_AUTO_HIDE).trim());
					List<Map<String, String>> actionButtons = Util
							.getListOfActions(jsonObjectPush.optString(PUSH_ACTIONS).trim(), eventType);
					List<Map<String, String>> carousel = Util
							.getListOfImages(jsonObjectPush.optString(PUSH_CAROUSEL).trim());
					Map<String, String> customPayload = Util
							.getMapOfCustomPayload(jsonObjectPush.optString(PUSH_CUSTOMPAYLOAD).trim());
					pushNotification.setToken(token);
					
					PushNotificationRequest pnr = new PushNotificationRequest();

					if (eventType.equals(Util.EVENT_TYPE_APP) && osType.equals(Util.OS_TYPE_ANDROID)) {
						pnr = gcmConnector.sendSingleMessage(pushNotification, Util.TEST_PUSH_TR_ID, title, message, null,
								imageUrl, deeplink, true, null, null, clientId, appOrSiteId, ttl, eventType, 0, null,
								false, actionButtons, carousel, customPayload, requestId, QUEUE_NAME_APP, pnr,developer);
					} else if (eventType.equals(Util.EVENT_TYPE_APP) && osType.equals(Util.OS_TYPE_IOS)) {
						pnr = apnsConnector.sendSingleMessage(pushNotification, Util.TEST_PUSH_TR_ID, title, message, null,
								imageUrl, deeplink, true, null, null, clientId, appOrSiteId, ttl, eventType, 0, null,
								false, actionButtons, carousel, customPayload, requestId, QUEUE_NAME_APP,pnr,developer);
					} else {
						pnr = gcmConnector.sendSingleMessage(pushNotification, Util.TEST_PUSH_TR_ID, title, message, icon,
								imageUrl, deeplink, true, null, null, clientId, appOrSiteId, ttl, eventType, 0,
								actionButtons, autohide, null, null, null, requestId, QUEUE_NAME_WEB,pnr,developer);
					}
					
					pushNotification =  pnr.getPushNotification();

					JSONObject jsonResponse = new JSONObject();
					jsonResponse.put(STATUS, pushNotification.getStatus().equalsIgnoreCase(PUSH_SENT) ? 1 : 0);
					jsonResponse.put(MESSAGE, pushNotification.getStatus().equalsIgnoreCase(PUSH_SENT) ? SUCCESS
							: pushNotification.description);
					jsonResponseToken.put(token, jsonResponse);
					jsonResponsePush.put(jsonResponseToken);
				}

				jsonResponseApp.put(PUSH, jsonResponsePush);
				jsonResponseData.put(jsonResponseApp);
			}

			jsonResponseFinal.put(STATUS, 1);
			jsonResponseFinal.put(MESSAGE, SUCCESS);
			jsonResponseFinal.put(DATA, jsonResponseData);
		} catch (JSONException e) {
			logger.info(requestId + " - Received Json Exception while parsing " + e.getMessage());
			jsonResponseFinal.put(STATUS, 0);
			jsonResponseFinal.put(MESSAGE, e.getMessage());
			jsonResponseFinal.put(DATA, jsonResponseData);
		} catch (InterruptedException e) {
			logger.info(requestId + " - Received Interruped Exception while parsing " + e.getMessage());
			jsonResponseFinal.put(STATUS, 0);
			jsonResponseFinal.put(MESSAGE, e.getMessage());
			jsonResponseFinal.put(DATA, jsonResponseData);
		}

		logger.info(requestId + " - " + jsonResponseFinal);
		pw.println(jsonResponseFinal);
	}
}
