package com.netcore.pnserver.pojo;

import java.util.List;
import java.util.Map;
import com.netcore.pnserver.pojo.Message;

public class PushNotificationRequest {
	
	private String url;
	private Message message;
	private String os ;
	private int cid ;
	private int uid ;
	private String pushid ;
	private String appid ;
	private int msgid ;
	private int ttl;
	private Msgbody msgbody;
	private int identified;
	private String siteid;
	private String browser;
	private List<Map<String, String>> actions;
	private List<Map<String, String>> actionButton;
	private List<Map<String, String>> carousel;
	private Map<String, String> customPayload;
	private List<Map<String, String>> devices;
	private int freqcapflag;
	private boolean autohide;
	private int bod;
	private Integer pa_enable;
	private Integer frequency;
	private long mysqlTime;
	private long mongoTime;
	private long redisTime;
	private long sentTime;
	private long sentCount;
	private long failedCount;	
	private int sent;
	private int failed;
	private List<String> pnbcJsonList;
	private List<String> summaryJsonList;
	private PushNotification pushNotification;
	private List<PushNotification> pushNotifications;
	private List<PushNotification> pushNotificationsubList;
	private List<PushNotification> invalidTokenList;
	private List<PushNotification> newTokenList;
	private List<String> uninstallJsonList;
	private String apikey;
	private boolean skipFlag;
	private int developer;
	
	
	
	public PushNotificationRequest(String url, Message message, String os,int cid, int uid, String pushid, String appid, int msgid, int ttl,
			Msgbody msgbody,int identified,String siteid, String browser,List<Map<String, String>> actions, boolean autohide,List<Map<String, String>> actionButton, List<Map<String, String>> carousel,  int pa_enable, int frequency, String apikey, boolean skipFlag) {
		super();
		this.url = url;
		this.message = message;
		this.os = os;
		this.cid = cid;
		this.uid = uid;
		this.pushid = pushid;
		this.appid = appid;
		this.msgid = msgid;
		this.ttl = ttl;
		this.msgbody = msgbody;
		this.identified = identified;
		this.siteid = siteid;
		this.browser = browser;
		this.actions = actions;
		this.autohide = autohide;
		this.actionButton = actionButton;
		this.carousel = carousel;
		this.pa_enable = pa_enable;
		this.frequency = frequency;
		this.apikey = apikey;
		this.skipFlag = skipFlag;
	}
	public boolean isSkipFlag() {
		return skipFlag;
	}
	public void setSkipFlag(boolean skipFlag) {
		this.skipFlag = skipFlag;
	}
	public boolean isAutohide() {
		return autohide;
	}
	public void setAutohide(boolean autohide) {
		this.autohide = autohide;
	}
	public List<Map<String, String>> getActions() {
		return actions;
	}
	public void setActions(List<Map<String, String>> actions) {
		this.actions = actions;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}
	
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public int getCid() {
		return cid;
	}
	public void setCid(int cid) {
		this.cid = cid;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getPushid() {
		return pushid;
	}
	public void setPushid(String pushid) {
		this.pushid = pushid;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public int getMsgid() {
		return msgid;
	}
	public void setMsgid(int msgid) {
		this.msgid = msgid;
	}
	public int getTtl() {
		return ttl;
	}
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	public Msgbody getMsgbody() {
		return msgbody;
	}
	public void setMsgbody(Msgbody msgbody) {
		this.msgbody = msgbody;
	}
	public int getIdentified() {
		return identified;
	}
	public void setIdentified(int identified) {
		this.identified = identified;
	}
	
	public String getSiteid() {
		return siteid;
	}
	public void setSiteid(String siteid) {
		this.siteid = siteid;
	}
	public String getBrowser() {
		return browser;
	}
	public void setBrowser(String browser) {
		this.browser = browser;
	}
	
	public PushNotificationRequest() {
		super();
	}
	public int getBod() {
		return bod;
	}
	public void setBod(int bod) {
		this.bod = bod;
	}
	public List<Map<String, String>> getActionButton() {
		return actionButton;
	}
	public void setActionButton(List<Map<String, String>> actionButton) {
		this.actionButton = actionButton;
	}	
	public List<Map<String, String>> getCarousel() {
		return carousel;
	}
	public void setCarousel(List<Map<String, String>> carousel) {
		this.carousel = carousel;
	}
	public Integer getPa_enable() {
		return pa_enable;
	}
	public void setPa_enable(Integer pa_enable) {
		this.pa_enable = pa_enable;
	}
	public Integer getFrequency() {
		return frequency;
	}
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}
	
	@Override
	public String toString() {
		return "PushNotificationRequest [url=" + url + ", message=" + message
				+ ", os=" + os + ", cid=" + cid + ", uid=" + uid + ", pushid="
				+ pushid + ", appid=" + appid + ", msgid=" + msgid + ", ttl="
				+ ttl + ", msgbody=" + msgbody + ", identified=" + identified
				+ ", siteid=" + siteid + ", browser=" + browser + ", actions="
				+ actions + ", actionButton=" + actionButton + ", carousel="
				+ carousel + ", autohide=" + autohide + ", bod=" + bod + "]";	
	}
	public long getMysqlTime() {
		return mysqlTime;
	}
	public void setMysqlTime(long mysqlTime) {
		this.mysqlTime = mysqlTime;
	}
	public long getMongoTime() {
		return mongoTime;
	}
	public void setMongoTime(long mongoTime) {
		this.mongoTime = mongoTime;
	}
	public long getRedisTime() {
		return redisTime;
	}
	public void setRedisTime(long redisTime) {
		this.redisTime = redisTime;
	}
	public long getSentTime() {
		return sentTime;
	}
	public void setSentTime(long sentTime) {
		this.sentTime = sentTime;
	}
	public int getSent() {
		return sent;
	}
	public void setSent(int sent) {
		this.sent = sent;
	}
	public int getFailed() {
		return failed;
	}
	public void setFailed(int failed) {
		this.failed = failed;
	}
	public List<String> getPnbcJsonList() {
		return pnbcJsonList;
	}
	public void setPnbcJsonList(List<String> pnbcJsonList) {
		this.pnbcJsonList = pnbcJsonList;
	}
	public List<String> getSummaryJsonList() {
		return summaryJsonList;
	}
	public void setSummaryJsonList(List<String> summaryJsonList) {
		this.summaryJsonList = summaryJsonList;
	}
	public List<PushNotification> getPushNotifications() {
		return pushNotifications;
	}
	public void setPushNotifications(List<PushNotification> pushNotifications) {
		this.pushNotifications = pushNotifications;
	}
	public List<PushNotification> getPushNotificationsubList() {
		return pushNotificationsubList;
	}
	public void setPushNotificationsubList(List<PushNotification> pushNotificationsubList) {
		this.pushNotificationsubList = pushNotificationsubList;
	}
	public List<String> getUninstallJsonList() {
		return uninstallJsonList;
	}
	public void setUninstallJsonList(List<String> uninstallJsonList) {
		this.uninstallJsonList = uninstallJsonList;
	}
	public String getApikey() {
		return apikey;
	}
	public void setApikey(String apikey) {
		this.apikey = apikey;
	}
	public List<PushNotification> getInvalidTokenList() {
		return invalidTokenList;
	}
	public void setInvalidTokenList(List<PushNotification> invalidTokenList) {
		this.invalidTokenList = invalidTokenList;
	}
	public long getSentCount() {
		return sentCount;
	}
	public void setSentCount(long sentCount) {
		this.sentCount = sentCount;
	}
	public long getFailedCount() {
		return failedCount;
	}
	public void setFailedCount(long failedCount) {
		this.failedCount = failedCount;
	}
	public PushNotification getPushNotification() {
		return pushNotification;
	}
	public void setPushNotification(PushNotification pushNotification) {
		this.pushNotification = pushNotification;
	}
	public List<Map<String, String>> getDevices() {
		return devices;
	}
	public void setDevices(List<Map<String, String>> devices) {
		this.devices = devices;
	}
	public int getFreqcapflag() {
		return freqcapflag;
	}
	public void setFreqcapflag(int freqcapflag) {
		this.freqcapflag = freqcapflag;
	}
	public List<PushNotification> getNewTokenList() {
		return newTokenList;
	}
	public void setNewTokenList(List<PushNotification> newTokenList) {
		this.newTokenList = newTokenList;
	}
	public int getDeveloper() {
		return developer;
	}
	public void setDeveloper(int developer) {
		this.developer = developer;
	}
	public Map<String, String> getCustomPayload() {
		return customPayload;
	}
	public void setCustomPayload(Map<String, String> customPayload) {
		this.customPayload = customPayload;
	}
	
}


