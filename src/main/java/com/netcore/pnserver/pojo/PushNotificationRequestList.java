package com.netcore.pnserver.pojo;

import java.util.List;
import java.util.Map;

public class PushNotificationRequestList {

	private String url;
	private Message message;	
	private int cid;
	private int uid;	
	private int msgid;
	private int ttl;
	private Msgbody msgbody;
	private int identified;
	private List<Map<String, String>> actions;
	private List<Map<String, String>> actionButton;
	private List<Map<String, String>> carousel;
	private List<Map<String, String>> devices;
	private boolean autohide;	
	private Integer pa_enable;
	private int freqcapflag;

	public PushNotificationRequestList(String url, Message message, int cid, int uid,
			int msgid, int ttl, Msgbody msgbody, int identified, 
			List<Map<String, String>> actions, boolean autohide, List<Map<String, String>> actionButton,
			List<Map<String, String>> carousel, int pa_enable, int freqcapflag, List<Map<String, String>> devices) {
		super();
		this.url = url;
		this.message = message;		
		this.cid = cid;
		this.uid = uid;	
		this.msgid = msgid;
		this.ttl = ttl;
		this.msgbody = msgbody;
		this.identified = identified;
		this.actions = actions;
		this.autohide = autohide;
		this.actionButton = actionButton;
		this.carousel = carousel;
		this.pa_enable = pa_enable;
		this.freqcapflag = freqcapflag;
		this.devices = devices;
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

	

	public PushNotificationRequestList() {
		super();
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

	@Override
	public String toString() {
		return "PushNotificationRequestList [url=" + url + ", message=" + message +  ", cid=" + cid
				+ ", uid=" + uid +   ", msgid=" + msgid + ", ttl=" + ttl
				+ ", msgbody=" + msgbody + ", identified=" + identified 
				+ ", actions=" + actions + ", actionButton=" + actionButton + ", carousel=" + carousel + ", autohide="
				+ autohide  + ", devices=" + devices + ", freqcapflag=" + freqcapflag + "]";
	}

	public List<Map<String, String>> getDevices() {
		return devices;
	}

	public void setDevices(List<Map<String, String>> devices) {
		this.devices = devices;
	}

	public Integer getFreqcapflag() {
		return freqcapflag;
	}

	public void setFreqcapflag(Integer freqcapflag) {
		this.freqcapflag = freqcapflag;
	}
}
