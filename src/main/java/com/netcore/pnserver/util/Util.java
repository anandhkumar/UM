package com.netcore.pnserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.init.ConfigManager;
import com.netcore.pnserver.pojo.FrequencyConfig;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.pojo.PushNotificationRequest;
import com.netcore.pnserver.pojo.PushNotificationRequestList;

public class Util {

	private static final String TMP_FILE_DIR = "/tmp/";
	final static Logger logger = Logger.getLogger(Util.class);

	public static final String OS_TYPE_ANDROID = "android";
	public static final String BROWSER_TYPE_CHROME = "chrome";
	public static final String EVENT_TYPE_APP = "app";
	public static final String EVENT_TYPE_ICICI_APP = "icici_app";
	public static final String EVENT_TYPE_WEB = "web";
	public static final String FCM_SERVER = "fcm";
	public static final String GCM_SERVER = "gcm";
	public static final String OS_TYPE_IOS = "ios";
	public static final String BROWSER_TYPE_SAFARI = "safari";
	public static final int TTL_DEFAULT_4WEEKS = 2419200;
	public static final String NETCORE_UNINSTALL = "NC_UNINSTALL";
	public static final int APP_UNINSTALLED = 24;
	public static final String FREQUENCY_COLLECTION = "frequencyDetails";
	public static final String ANON_FREQUENCY_COLLECTION = "anonFrequencyDetails";
	public static final String BPN_TOKEN = "bpnTokens";
	public static final String PR1_PN_BC_TR = "PR1_PN_BC_TR";
	public static final String PR1_PN_ATR = "PR1_PN_ATR";
	public static final String APN_TOKEN = "apnTokens";
	public static final String TEST_PUSH_TR_ID = "smartech_test_pn";
	public static final int DAY = 1;
	public static final int WEEK = 2;
	public static final int MONTH = 3;
	public static final int CHANNEL_APPPUSH = 4;
	public static final int CHANNEL_WEBPUSH = 5;

	public List<Integer> testClientList = new ArrayList<Integer>();
	public final boolean IS_FEATURE_TOGGLE;
	private ConfigManager cons = ConfigManager.getInstance();
	private static RedisDao redisDao;
	public static final String FREQ_CONFIG_FLAG = "FREQUENCY_CAPPING";
	public static final String FREQ_CONFIG = "FREQUENCY_DATA";
	public static final String APNS_RESPONSE_BAD_DEVICE_TOKEN = "BadDeviceToken";
	public static final String PRODUCTION_APNS_HOST = "api.push.apple.com";
	public static final String DEVELOPER_APNS_HOST = "api.sandbox.push.apple.com";
	
	public static final String appActivitySchema = 
						"(id int(11) NOT NULL AUTO_INCREMENT, userid INT(11) NOT NULL, anonid INT(11), eventid INT(10) NOT NULL,"
					+ " sessionid VARCHAR(255) NOT NULL, guid VARCHAR(255) NOT NULL, " 
					+ "payload TEXT, ts timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
					+ "channelid TINYINT NOT NULL, tx BIGINT, msgid INT(11), latitude DOUBLE, longitude DOUBLE, pts MEDIUMINT, sts MEDIUMINT, deep_link TEXT,"
					+ "PRIMARY KEY (id), KEY idxuserid (userid), KEY idxeventid (eventid) ) ENGINE=MyISAM DEFAULT CHARSET=latin1" ;
	
	public static final String anonAppActivitySchema = 
			"(id int(11) NOT NULL AUTO_INCREMENT, userid INT(11) NOT NULL, eventid INT(10) NOT NULL,"
		+ " sessionid VARCHAR(255) NOT NULL, guid VARCHAR(255) NOT NULL, " 
		+ "payload TEXT, ts timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
		+ "channelid TINYINT NOT NULL, tx BIGINT, msgid INT(11), latitude DOUBLE, longitude DOUBLE, pts MEDIUMINT, sts MEDIUMINT, deep_link TEXT,"
		+ "PRIMARY KEY (id), KEY idxuserid (userid), KEY idxeventid (eventid) ) ENGINE=MyISAM DEFAULT CHARSET=latin1" ;
	
	public static final List<String> pushFailureReasons = new ArrayList<String>(){{
		add("NotRegistered"); 
		add("Unregistered");
		add("Unregistered Device");
	}};
	
	public Util(RedisDao redisDao){	

		this.redisDao = redisDao;
		this.IS_FEATURE_TOGGLE = Boolean.parseBoolean(cons.getProperty("IS_TEST").trim());
		for (String s : cons.getProperty("TEST_CLIENTS").split(",")) {
			testClientList.add(Integer.parseInt(s));
		}
	}

	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JsonNode getRootNode(String jsonStr) throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(jsonStr);
	}

	public static PushNotificationRequest getObjectFromJson(String jsonStr)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		PushNotificationRequest pnr = mapper.readValue(jsonStr, PushNotificationRequest.class);
		return pnr;
	}
	
	public static PushNotificationRequestList getObjectFromJsonList(String jsonStr)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		PushNotificationRequestList pnr = mapper.readValue(jsonStr, PushNotificationRequestList.class);
		return pnr;
	}

	public static Object getObjectFromJson(String json, Class clas) {
		Gson gson = new Gson();
		return gson.fromJson(json, clas);
	}

	public static String downLoadp12(String urlstr) throws IOException {
		String FileName = urlstr.substring(urlstr.lastIndexOf("/"));
		URL url = new URL(urlstr);
		String path = TMP_FILE_DIR + FileName;
		File file = new File(path);
		file.deleteOnExit();
		FileUtils.copyURLToFile(url, file);
		return path;
	}

	public static Reader getFileReader(String urlstr) throws IOException {
		String FileName = urlstr.substring(urlstr.lastIndexOf("/"));
		URL url = new URL(urlstr);
		String path = TMP_FILE_DIR + FileName;
		File file = new File(path);
		file.deleteOnExit();
		FileUtils.copyURLToFile(url, file);
		FileInputStream fis = new FileInputStream(file);
		return new InputStreamReader(fis, "UTF-8");
	}

	public static String createJson(String status, int cid, int msgid, List<Integer> userids, List<String> pushids,
			String trid, long ts, List<String> description, String eventType, int indentifiedFlag, boolean skipFlag,
			String appIdorSiteId, List<String> token, List<Integer> bods, String ostype, Integer automationId,
			List<String> newtoken, List<Boolean> invalidToken) throws JsonProcessingException {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("clientid", cid);
		map.put("msgid", msgid);
		map.put("ts", ts);
		map.put("userids", userids.toArray());
		map.put("pushids", pushids.toArray());
		if (bods != null)
			map.put("bod", bods.toArray());
		if (token != null)
			map.put("tokens", token.toArray());
		map.put("status", status);
		map.put("trid", trid);
		map.put("description", description);
		map.put("eventtype", eventType);
		if (eventType.equals(Util.EVENT_TYPE_WEB)) {
			map.put("siteid", appIdorSiteId);
		} else {
			map.put("appid", appIdorSiteId);
		}
		map.put("identified", indentifiedFlag);
		map.put("skip", skipFlag);
		/*
		if (sourceId != null) {
			map.put("sourceId", sourceId);
		}*/
		if (automationId != null) {
			map.put("automationid", automationId);
		}
		if (ostype != null) {
			map.put("os", getOsType(ostype));
		}
		map.put("invalidtoken", invalidToken.toArray());
		if (newtoken != null)
			map.put("newtoken", newtoken.toArray());
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(map);
		return json;
	}
	
	public static String createJsonList(List<String> status, int cid, int msgid, List<Integer> userids, List<String> pushids,
			String trid, long ts, List<String> description, String eventType, int indentifiedFlag, boolean skipFlag,
			List<String> appIdorSiteId, List<String> token, List<Integer> bods, List<String> ostype, Integer automationId,
			Integer sourceId, int freqcapflag, List<String> newtoken, List<Boolean> invalidToken) throws JsonProcessingException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("clientid", cid);
		map.put("msgid", msgid);
		map.put("ts", ts);
		map.put("userids", userids.toArray());
		map.put("pushids", pushids.toArray());
		if (bods != null)
			map.put("bod", bods.toArray());
		if (token != null)
			map.put("tokens", token.toArray());
		map.put("status", status.toArray());
		map.put("trid", trid);
		map.put("description", description.toArray());
		map.put("eventtype", eventType);
		if (eventType.equals(Util.EVENT_TYPE_WEB)) {
			map.put("siteid", appIdorSiteId.toArray());
		} else {
			map.put("appid", appIdorSiteId.toArray());
			map.put("os", ostype.toArray());
		}
		map.put("identified", indentifiedFlag);
		map.put("skip", skipFlag);
		if (sourceId != null) {
			map.put("sourceId", sourceId);
		}
		if (automationId != null) {
			map.put("automationid", automationId);
		}
		map.put("freqcapflag", freqcapflag);
		if (newtoken != null)
			map.put("newtoken", newtoken.toArray());
		map.put("invalidtoken", invalidToken.toArray());
		
		

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(map);
		return json;
	}


	public static String getOsType(String ostype) {
		if (ostype.equals("01")) {
			return OS_TYPE_ANDROID;
		} else if (ostype.equals("10")) {
			return OS_TYPE_IOS;
		}
		return null;
	}

	public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
					URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}

	public static String[] getTrIDDetails(String trid) {
		// trid: $clientid-$msgid-$userid-$automationid-$ts
		if (trid == null)
			throw new RuntimeException("trid null");
		StringTokenizer st = new StringTokenizer(trid, "-");
		if (st.countTokens() < 5)
			throw new RuntimeException(
					"Incorrect trid Format, exptected: trid: $clientid-$msgid-$userid-$automationid-$ts");
		String ret[] = new String[6];
		int i = 0;
		while (st.hasMoreTokens()) {
			ret[i] = st.nextToken();
			i++;
		}
		return ret;
	}

	public static int getMaxTtl(int ttl) {
		return (ttl == 0 || ttl > TTL_DEFAULT_4WEEKS) ? TTL_DEFAULT_4WEEKS : ttl;
	}

	public static Map<String, Object> convertMap(Map<String, String> map) {
		if (map == null)
			return null;
		Map<String, Object> retMap = new HashMap<String, Object>();
		for (String s : map.keySet()) {
			retMap.put(s, map.get(s));
		}
		return retMap;
	}

	public String getMonthlyShardName() {

		Date date = new Date();
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int year = localDate.getYear();
		int month = localDate.getMonthValue();
		String tablename = String.valueOf(year).substring(2);
		if (month < 10)
			tablename = "0" + month + "" + tablename;
		else
			tablename = String.valueOf(month) + tablename;

		return tablename;
	}

	public Map<Integer, Integer> getPeriod(FrequencyConfig fq, String type, int clientId)
			throws IOException, InterruptedException {
		Integer dayThreshold = null;
		Integer weekThreshold = null;
		Integer monthThreshold = null;
		if (type.equals(EVENT_TYPE_APP)) {
			dayThreshold = fq.getApn().get("day");
			weekThreshold = fq.getApn().get("week");
			monthThreshold = fq.getApn().get("month");
		} else {
			dayThreshold = fq.getBpn().get("day");
			weekThreshold = fq.getBpn().get("week");
			monthThreshold = fq.getBpn().get("month");
		}
		Map<Integer, Integer> periodThresholdMap = new HashMap<Integer, Integer>();
		if (dayThreshold > 0) {
			periodThresholdMap.put(getPeriod(DAY, clientId), dayThreshold);
		}
		if (weekThreshold != 0) {
			periodThresholdMap.put(getPeriod(WEEK, clientId), weekThreshold);
		}
		if (monthThreshold != 0) {
			periodThresholdMap.put(getPeriod(MONTH, clientId), monthThreshold);
		}
		return periodThresholdMap;
	}

	public int getPeriod(int weekOrDayOrMonthFlag, int clientId) throws IOException, InterruptedException {
		if (weekOrDayOrMonthFlag == DAY) {
			Date today = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
			return Integer.parseInt(df.format(today));
		} else if (weekOrDayOrMonthFlag == WEEK) {
			return getWeekNumber(clientId);
		} else if (weekOrDayOrMonthFlag == MONTH) {
			Date today = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyMM");
			return Integer.parseInt(df.format(today));
		}
		return 0;
	}

	@SuppressWarnings("unused")
	private int getWeekNumber(int clientId) throws IOException, InterruptedException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYMMdd");
		String key = "WEEK_NUMBER_" + LocalDate.now().format(formatter);
		String weekNumber = redisDao.get(key);
		if (weekNumber != null) {
			return Integer.parseInt(weekNumber);
		} else {
			return getFromAPI(clientId, LocalDate.now().format(formatter));
		}
	}

	private int getFromAPI(int clientId, String date) throws IOException, InterruptedException {
		String url = cons.getProperty("GET_WEEK_NUMBER");
		StringBuilder query = new StringBuilder();
		query.append("client_id=").append(clientId).append("&date=").append(date);
		String jsonStr = readUrl(url, query.toString());
		Map<String, Object> clientIdUserIdMap = getDictFromJsonString(jsonStr);
		return ((Double) clientIdUserIdMap.get("result")).intValue();
	}

	private static String readUrl(String url, String queryString)
			throws MalformedURLException, IOException, InterruptedException {
		URL urlObj = new URL(url + "?" + queryString);
		try {
			HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			return in.readLine();
		} catch (SocketTimeoutException se) {
			int randomNum = ThreadLocalRandom.current().nextInt(5, 2000 + 1);
			Thread.sleep(randomNum);
			HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			return in.readLine();
		}
	}

	public Map<String, Object> getDictFromJsonString(String str) {
		Map<String, Object> retMap = new Gson().fromJson(str, new TypeToken<HashMap<String, Object>>() {
		}.getType());
		return retMap;
	}

	public static List<Map<String, String>> getListOfActions(String convertString, String eventType){
		List<Map<String, String>> listActions = new ArrayList<>();

		try {
			JSONArray jsonArray = new JSONArray(convertString);
			JSONObject jsonObject;
			Map<String, String> mapActions;

			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObject = jsonArray.getJSONObject(i);

				mapActions = new HashMap<String, String>();

				if(eventType.equals(EVENT_TYPE_APP)){
					mapActions.put("actionName", jsonObject.optString("actionName").replaceAll("\\\\", ""));
					mapActions.put("actionDeeplink", jsonObject.optString("actionDeeplink"));
				} else {
					mapActions.put("action", jsonObject.optString("action"));
					mapActions.put("title", jsonObject.optString("title"));
					mapActions.put("icon", jsonObject.optString("icon"));
					mapActions.put("id", jsonObject.optString("id"));
				}

				listActions.add(mapActions);
			}
		} catch (JSONException e) {
			logger.info("Failure due to: " + e.getMessage());
		}

		return listActions;
	}

	public static List<Map<String, String>> getListOfImages(String convertString){
		List<Map<String, String>> listActions = new ArrayList<>();

		try {
			JSONArray jsonArray = new JSONArray(convertString);
			JSONObject jsonObject;
			Map<String, String> mapImages;

			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObject = jsonArray.getJSONObject(i);

				mapImages = new HashMap<String, String>();

				mapImages.put("imgUrl", jsonObject.optString("imgUrl"));
				mapImages.put("imgTitle", jsonObject.optString("imgTitle").replaceAll("\\\\", ""));
				mapImages.put("imgMsg", jsonObject.optString("imgMsg").replaceAll("\\\\", ""));
				mapImages.put("imgDeeplink", jsonObject.optString("imgDeeplink"));

				listActions.add(mapImages);
			}
		} catch (JSONException e) {
			logger.info("Failure due to: " + e.getMessage());
		}

		return listActions;
	}

	public int getAutomationId(String trId) {
		return Integer.parseInt(trId.split("-")[3]);
	}

	public static long pushAmpData(String payload, PushNotification pn, String appid) {
		// TODO Auto-generated method stub
		long tm = 0;
		
		long t1 = System.currentTimeMillis();
		String status = redisDao.hGet("pamp_status", appid);
		tm += System.currentTimeMillis() - t1;
		
		if (status == null || status.equals("0")) {
			logger.info("Push Amp status is false or not set for app: " + appid);
			return tm;
		}else {
			logger.info("Push Amp status is true for app: " + appid);
		}
		
		long t2 = System.currentTimeMillis();
		String freq = redisDao.hGet("pamp_freq", appid);
		tm += System.currentTimeMillis() - t2;
		
		Map<String, Object> notificationdata = new HashMap();
		String pampqueueName = "PR1_PN_PUSHAMP";
		notificationdata.put("pushid", pn.pushid);
		notificationdata.put("appid", appid);
		notificationdata.put("message", payload);
		notificationdata.put("pa_enable", status);
		notificationdata.put("frequency", freq);
		Gson gson = new Gson();
		String payloaddata = gson.toJson(notificationdata);
		logger.info("Push Amp msg in redis queue " + pampqueueName + " : " + payloaddata);
		
		long t3 = System.currentTimeMillis();
		redisDao.enqueueRedis(pampqueueName, payloaddata);
		tm += System.currentTimeMillis() - t3;
		
		return tm;

	}

	public static  HashMap<String, Object> createHashToken(int cid, List<PushNotification> newTokenList, int identifiedFlag,
			String eventType, String osType, Map<String, String> token_canonicalRegId_map) throws JsonProcessingException {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("clientid", cid);
		map.put("newtokenlist", newTokenList.toArray());
		map.put("identified", identifiedFlag);
		map.put("eventtype", eventType);
		map.put("ostype", osType);
		map.put("update_token", token_canonicalRegId_map);
		//ObjectMapper mapper = new ObjectMapper();
		//String json = mapper.writeValueAsString(map);
		return map;
				
	}

	public static HashMap<String, Object> addListHash(List<PushNotification> invalidTokenList, HashMap<String, Object> hashmap) {
		hashmap.put("deletetokenBatch", invalidTokenList);
		return hashmap; 
		
	}

	public  Map<String, Object> getIOSCertFile(int cid, String appid, String eventType) throws IOException, InterruptedException {
			String url = cons.getProperty("GET_IOSCER_API");
			StringBuilder query = new StringBuilder();
			query.append("clientid=").append(cid)
			.append("&appid=").append(appid)
			.append("&sourcetype=").append(eventType);
					
			String jsonStr = readUrl(url, query.toString());
			Map<String, Object> clientIdUserIdMap= getDictFromJsonString(jsonStr);
			return clientIdUserIdMap;
		
	}

	public static Map<String, String> getMapOfCustomPayload(String convertString) {
		Map<String, String> customPayloadMap = new HashMap<String, String>();
		try {
			JSONObject jsonObj = new JSONObject(convertString);
			
				for (String key : jsonObj.keySet()) {
			        //based on you key types
			        String keyStr = key;
			        String keyvalue = jsonObj.getString(keyStr);
			        customPayloadMap.put(keyStr, keyvalue);
			        //Print key and value
			        System.out.println("key: "+ keyStr + " value: " + keyvalue);
			    }
				
		} catch (JSONException e) {
			logger.info("Failure due to: " + e.getMessage());
		}
		return customPayloadMap;
	}
	
	
}
