package com.netcore.pnserver.pojo;

import java.util.Map;

public class PushNotification {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pushid == null) ? 0 : pushid.hashCode());
		result = prime * result + ((userid == null) ? 0 : userid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PushNotification other = (PushNotification) obj;
		if (pushid == null) {
			if (other.pushid != null)
				return false;
		} else if (!pushid.equals(other.pushid))
			return false;
		if (userid == null) {
			if (other.userid != null)
				return false;
		} else if (!userid.equals(other.userid))
			return false;
		return true;
	}
	public int getBod() {
		return bod;
	}
	public void setBod(int bod) {
		this.bod = bod;
	}
	public Integer userid;
	public String pushid;
	public String siteid;
	public String appid;
	public String os;
	public String token;
	public String newtoken;
	// sent / failed - not making enum  
	public String status;
	public String description;
	public int cid;
	public int msgid;
	public int bod;
	public boolean testPush;

	private String trid;
	private long ts;
	private String title;
	private String message;
	private String deepLink;
	private String imageURL;
	public boolean invalidToken;
	private String developer;
	private Map<String, String> customPayload;
	
	public PushNotification(Integer userid, String pushid, int cid, int msgid, int bod, boolean testPush){
		this.userid = userid;
		this.pushid = pushid;
		this.cid = cid;
		this.msgid = msgid;
		this.bod = bod;
		this.testPush = testPush;
	}
	
	public PushNotification(Integer userid, String pushid, int cid, int msgid, int bod, boolean testPush, String siteid){
		this.userid = userid;
		this.pushid = pushid;
		this.cid = cid;
		this.msgid = msgid;
		this.bod = bod;
		this.testPush = testPush;
		this.siteid = siteid;
	}
	
	public PushNotification(Integer userid, String pushid, int cid, int msgid, int bod, boolean testPush, String appid, String os){
		this.userid = userid;
		this.pushid = pushid;
		this.cid = cid;
		this.msgid = msgid;
		this.bod = bod;
		this.testPush = testPush;
		this.appid = appid;
		this.os = os;
	}	
	
	public PushNotification(Integer userid, String pushid, int cid, int msgid, int bod, boolean testPush, String appid, String os, String developer){
		this.userid = userid;
		this.pushid = pushid;
		this.cid = cid;
		this.msgid = msgid;
		this.bod = bod;
		this.testPush = testPush;
		this.appid = appid;
		this.os = os;
		this.developer = developer;
	}
	public PushNotification(Integer userid, String pushid, int cid, int msgid,int bod, String trid, long ts, String title,String message,String deepLink,String imageURL , String token, Map <String, String> customPayload){
		this.userid = userid;
		this.pushid = pushid;
		this.cid = cid;
		this.msgid = msgid;
		this.bod = bod;
		this.trid = trid;
		this.ts = ts;		
		this.title = title;
		this.message = message;
		this.deepLink = deepLink;
		this.imageURL = imageURL;
		this.token = token;
		this.customPayload = customPayload;
	}
	
	public PushNotification(Integer userid, String pushid, int cid, int msgid,int bod, String trid, long ts){
		this.userid = userid;
		this.pushid = pushid;
		this.cid = cid;
		this.msgid = msgid;
		this.bod = bod;
		this.trid = trid;
		this.ts = ts;	
	}
	
	
	public PushNotification(Integer userid, String pushid, int cid, int msgid,int bod, String trid, long ts, String token){
		this.userid = userid;
		this.pushid = pushid;
		this.cid = cid;
		this.msgid = msgid;
		this.bod = bod;
		this.trid = trid;
		this.ts = ts;
		this.token = token;
	}

	public boolean isTestPush() {
		return testPush;
	}

	public void setTestPush(boolean testPush) {
		this.testPush = testPush;
	}

	public Integer getUserid() {
		return userid;
	}

	public void setUserid(Integer userid) {
		this.userid = userid;
	}

	public String getPushid() {
		return pushid;
	}

	public void setPushid(String pushid) {
		this.pushid = pushid;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int getMsgid() {
		return msgid;
	}

	public void setMsgid(int msgid) {
		this.msgid = msgid;
	}

	@Override
	public String toString() {
		return "PushNotification [userid=" + userid + ", pushid=" + pushid
				+ ", token=" + token + ", status=" + status + ", description="
				+ description + ", cid=" + cid + ", msgid=" + msgid + ", bod="
				+ bod + ", testPush=" + testPush + "]";
	}

	public String getNewtoken() {
		return newtoken;
	}

	public void setNewtoken(String newtoken) {
		this.newtoken = newtoken;
	}

	public String getTrid() {
		return trid;
	}

	public void setTrid(String trid) {
		this.trid = trid;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDeepLink() {
		return deepLink;
	}

	public void setDeepLink(String deepLink) {
		this.deepLink = deepLink;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getSiteid() {
		return siteid;
	}

	public void setSiteid(String siteid) {
		this.siteid = siteid;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}
	
	public Map<String, String> getCustomPayload() {
		return customPayload;
	}
	public void setCustomPayload(Map<String, String> customPayload) {
		this.customPayload = customPayload;
	}

}
