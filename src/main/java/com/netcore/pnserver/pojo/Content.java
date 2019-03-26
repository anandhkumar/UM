package com.netcore.pnserver.pojo;

import java.util.List;
import java.util.Map;

public class Content {
	private String msgtext;
	private String title;
	private String deeplink;
	private String imageurl;
	private String icon;
	private List<Map<String, String>> actionButton;
	private List<Map<String, String>> carousel;
	private Map<String, String> customPayload;
	
	public Content() {
		super();
	}

	public Content(String msgtext, String title, String deeplink,String imageurl, String icon, List<Map<String, String>> actionButtons, List<Map<String, String>> carousel, Map<String, String> customPayload ) {
		super();
		this.msgtext = msgtext;
		this.title = title;
		this.deeplink = deeplink;
		this.imageurl = imageurl;
		this.icon = icon;
		this.actionButton = actionButton;
		this.carousel = carousel;
		this.customPayload = customPayload;
	}

	public String getMsgtext() {
		return msgtext;
	}
	public void setMsgtext(String msgtext) {
		this.msgtext = msgtext;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDeeplink() {
		return deeplink;
	}
	public void setDeeplink(String deeplink) {
		this.deeplink = deeplink;
	}	
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getImageurl() {
		return imageurl;
	}
	public void setImageurl(String imageurl) {
		this.imageurl = imageurl;
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

	@Override
	public String toString() {
		return "Content [msgtext=" + msgtext + ", title=" + title
				+ ", deeplink=" + deeplink + ", imageurl=" + imageurl
				+ ", icon=" + icon + ", actionButton=" + actionButton
				+ ", carousel=" + carousel + "]";
	}

	public Map<String, String> getcustomPayload() {
		return customPayload;
	}
}
