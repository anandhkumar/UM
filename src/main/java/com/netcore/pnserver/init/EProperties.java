package com.netcore.pnserver.init;

public class EProperties extends java.util.Properties {

	private static final long serialVersionUID = 1L;

	public int getPropAsInt(String key) {
		return getPropAsInt(key, 0);
	}

	public int getPropAsInt(String key, int defaultValue) {
		try {
			return Integer.parseInt(getProperty(key));
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public long getPropAsLong(String key) {
		return getPropAsLong(key, 0);
	}

	public long getPropAsLong(String key, long defaultValue) {
		try {
			return Long.parseLong(getProperty(key));
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public String getPropAsString(String key) {
		return getPropAsString(key, "");
	}

	public String getPropAsString(String key, String defaultValue) {
		try {
			return getProperty(key);
		} catch (Exception ex) {
			return defaultValue;
		}
	}

}
