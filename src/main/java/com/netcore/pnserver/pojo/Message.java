package com.netcore.pnserver.pojo;

public class Message {
	@Override
	public String toString() {
		return "Message [trid=" + trid + ", identity="
				+ identity + ", content=" + content + "]";
	}
	
	public Message() {
		super();
	}

	public Message(String trid, String identity, Content content) {
		super();
		this.trid = trid;
		this.identity = identity;
		this.content = content;
	}

	public String getTrid() {
		return trid;
	}
	public void setTrid(String trid) {
		this.trid = trid;
	}
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	public Content getContent() {
		return content;
	}
	public void setContent(Content content) {
		this.content = content;
	}
	private String trid;
	private String identity;
	private Content content;
}
