package com.netcore.pnserver.pojo;

import java.util.Map;

/**
 * @author Swapnil Srivastav
 *
 */
public class FrequencyConfig {
	
	private Map<String, Integer> email;
	private Map<String, Integer> sms;
	private Map<String, Integer> voice;
	private Map<String, Integer> apn;
	private Map<String, Integer> bpn;
	/**
	 * @return the email
	 */
	public Map<String, Integer> getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(Map<String, Integer> email) {
		this.email = email;
	}
	/**
	 * @return the sms
	 */
	public Map<String, Integer> getSms() {
		return sms;
	}
	/**
	 * @param sms the sms to set
	 */
	public void setSms(Map<String, Integer> sms) {
		this.sms = sms;
	}
	/**
	 * @return the voice
	 */
	public Map<String, Integer> getVoice() {
		return voice;
	}
	/**
	 * @param voice the voice to set
	 */
	public void setVoice(Map<String, Integer> voice) {
		this.voice = voice;
	}
	/**
	 * @return the apn
	 */
	public Map<String, Integer> getApn() {
		return apn;
	}
	/**
	 * @param apn the apn to set
	 */
	public void setApn(Map<String, Integer> apn) {
		this.apn = apn;
	}
	/**
	 * @return the bpn
	 */
	public Map<String, Integer> getBpn() {
		return bpn;
	}
	/**
	 * @param bpn the bpn to set
	 */
	public void setBpn(Map<String, Integer> bpn) {
		this.bpn = bpn;
	}
	@Override
	public String toString() {
		return "FrequencyConfig [email=" + email + ", sms=" + sms + ", voice=" + voice
				+ ", apn=" + apn + ", bpn=" + bpn + "]";
	}
	

}
