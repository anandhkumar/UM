package com.netcore.pnserver.dao;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.netcore.pnserver.pojo.PushNotification;

public interface MySqlDao {

	public String getAppTokenForGuid(int clientId, int userId, String pushid, String osType, int identifiedFlag);
	
	public String getWebTokenForGuid(int clientId, int userId, String pushid, String browserType, int identifiedFlag);
	
	public List<PushNotification> getAppTokensForGuids(int clientId, List<PushNotification> pns, String osType, int identifiedFlag,String requestId);
	
	public List<PushNotification> getWebTokensForGuids(int clientId, List<PushNotification> pns, String browserType, int identifiedFlag,String requestId);
		
	public String getAndroidApiKey(int cid, String appid);
	
	public String getChromeApiKey(int cid, String siteid);

	public int updateAppToken(int clientId, String oldToken, String newToken, int identifiedFlag,String requestId, String osType);
	
	public void updateAppTokenList(int clientId, List<PushNotification> tokenList, int identifiedFlag,String requestId, String osType);

	public int updateWebToken(int clientId, String oldToken, String newToken, int identifiedFlag,String requestId, String browserType);

	public void updateWebTokenList(int clientId, List<PushNotification> tokenList, int identifiedFlag,String requestId, String browserType);
	
	public int deleteInvalidAppToken(int clientId, int userId, String guid, String token, int identifiedFlag, String ostype,String requestId);
	

	public void deleteInvalidAppTokenBatch(int clientId, List<PushNotification> InvalidAppToken, int identifiedFlag, String ostype,String requestId);	
	
	public void deleteInvalidWebToken(int clientId, List<PushNotification> InvalidAppToken, int identifiedFlag,String ostype,String requestId);

	public int deleteInvalidWebToken(int clientId, int userId, String guid, String token, int identifiedFlag,String ostype, String requestId);
	
	public Map<String, Object> getIOSCertFile(int cid, String appid,String eventType);
	
	public int publishSummary(int clientId, int msgId, int sentCount, int failedCount,int frequencyCapping, String eventType,String requestId);

	public boolean skipFrequencyCapping(int cid, int msgid,int channelId);

	public void disableGuid(int cid, int userid, String guid, int identifiedFlag,String requestId);
	
	public void disableGuidBatch(int cid, List<PushNotification> InvalidTokenList, int identifiedFlag,String requestId);
	
	public int getWebSourceId(int cid, String appIdOrSourceId) throws ExecutionException;
	
	public int getWebOrAppSourceId(int cid, String appIdOrSiteId, String idColumn, String appIdOrSiteIdColumn, String table);
}
