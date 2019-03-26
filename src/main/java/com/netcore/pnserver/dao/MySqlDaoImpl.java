package com.netcore.pnserver.dao;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netcore.pnserver.pojo.PushNotification;
import com.netcore.pnserver.util.ClientDBMap;
import com.netcore.pnserver.util.Util;

public class MySqlDaoImpl implements MySqlDao, ApplicationContextAware{
	private JdbcTemplate papiJdbcTemplate;
	private ClientDBMap clientdbmap;
	private Util util;
	public static HashMap<Integer, LoadingCache<String, Integer>> clientIdSiteIdSourceIdCache;

	final static Logger logger = Logger.getLogger(MySqlDaoImpl.class);
	private static ApplicationContext applicationContext = null;

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public MySqlDaoImpl(JdbcTemplate papiJdbcTemplate, ClientDBMap clientdbmap,Util util){
		this.papiJdbcTemplate = papiJdbcTemplate;
		this.clientdbmap = clientdbmap;
		this.util = util;
		this.clientIdSiteIdSourceIdCache = new HashMap<Integer, LoadingCache<String, Integer>>();
	}


	private String[] getDBname(int clientId){
		String dbstr = this.clientdbmap.getDB(clientId);
		try {
			String dbname[] = dbstr.split("@");
			return dbname;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new RuntimeException("Client id "+clientId+" doesn't have an DBname in expected format :dnbame@hostname :"+dbstr);
		}
	}

	public JdbcTemplate getJdbcTemplate(String dbName){
		return (JdbcTemplate) applicationContext.getBean(dbName);
	}

	@Override
	public String getAppTokenForGuid(int clientId, int userId, String pushid, String osType, int identifiedFlag) {
		if(osType.equals(Util.OS_TYPE_ANDROID)) {	
			return getTokenForGuid(clientId, userId, pushid, osType, identifiedFlag, "app_token_map_android", "anon_app_token_map_android");
		}
		else if (osType.equals(Util.OS_TYPE_IOS)) {			
			return getTokenForGuid(clientId, userId, pushid, osType, identifiedFlag, "app_token_map_ios", "anon_app_token_map_ios");
		}
		return null;
	}

	
	@Override
	public String getWebTokenForGuid(int clientId, int userId, String pushid, String browserType, int identifiedFlag) {
		if(browserType.equals(Util.BROWSER_TYPE_CHROME)) {
			return getTokenForGuid(clientId, userId, pushid, browserType, identifiedFlag, "web_token_map_chrome", "anon_web_token_map_chrome");
		}
		else if(browserType.equals(Util.BROWSER_TYPE_SAFARI)) {
			return getTokenForGuid(clientId, userId, pushid, browserType, identifiedFlag, "web_token_map_apns", "anon_web_token_map_apns");
		}
		return null;
	}
	
	private String getTokenForGuid(int clientId, int userId, String pushid, String osType, int identifiedFlag,String identifiedTable,String anonTable){
		String[] dbName = getDBname(clientId);
		String query = null;
		String tableName = null;
		tableName = (identifiedFlag == 1) ? identifiedTable : anonTable;
		query = "select token from " + dbName[0] + "." + tableName + " where userId=? and guid=?";
		Object[] args = new Object[] {userId, pushid};
		JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName[1]);
		return jdbcTemplate.queryForObject(query, args,String.class);
	}


	@Override
	public String getAndroidApiKey(int cid, String appid) {
		return getApiKey(cid, appid, "android_apikey", "appid", "push_app_master");
	}

	@Override
	public String getChromeApiKey(int cid, String siteid) {
		return getApiKey(cid, siteid, "auth_key", "siteid", "push_web_master");
	}
	
	private String getApiKey(int cid, String appIdOrSiteId,String authkeyColumn, String appIdOrSiteIdColumn,String table){
		String query = "Select server_type,"+authkeyColumn+" from papi_automation."+table+" where cid= ? and "+appIdOrSiteIdColumn+"= ? ";
		Object[] args = new Object[]{cid,appIdOrSiteId};
		Map<String,Object> serverTypeAndKey = papiJdbcTemplate.queryForMap(query, args);
		String apikey = (String) serverTypeAndKey.get(authkeyColumn);
		String servertType = (String) serverTypeAndKey.get("server_type");
		return servertType+apikey;
	}

	@Override
	public int updateAppToken(int clientId, String oldToken, String newToken, int identifiedFlag,String requestId, String osType) {
		
		if(osType.equals(Util.OS_TYPE_ANDROID)) {	
			return updateToken(clientId, oldToken, newToken, identifiedFlag, "app_token_map_android", "anon_app_token_map_android",requestId);
		}
		else if (osType.equals(Util.OS_TYPE_IOS)) {			
			return updateToken(clientId, oldToken, newToken, identifiedFlag, "app_token_map_ios", "anon_app_token_map_ios",requestId);
		}
		return 0;
		
	}

	@Override
	public int updateWebToken(int clientId, String oldToken, String newToken, int identifiedFlag,String requestId, String browserType) {
		
		if(browserType.equals(Util.BROWSER_TYPE_CHROME)) {
			return updateToken(clientId, oldToken, newToken, identifiedFlag, "web_token_map_chrome", "anon_web_token_map_chrome",requestId);
		}
		else if(browserType.equals(Util.BROWSER_TYPE_SAFARI)) {
			return updateToken(clientId, oldToken, newToken, identifiedFlag, "web_token_map_apns", "anon_web_token_map_apns",requestId);
		}
		return 0;		
	}
	
	@Override
	public void updateAppTokenList(int clientId, List<PushNotification> tokenList, int identifiedFlag,String requestId, String osType) {
		if(osType.equals(Util.OS_TYPE_ANDROID)) {	
			updateToken(clientId, tokenList, identifiedFlag, "app_token_map_android", "anon_app_token_map_android",requestId);
		}
		else if (osType.equals(Util.OS_TYPE_IOS)) {			
			updateToken(clientId, tokenList, identifiedFlag, "app_token_map_ios", "anon_app_token_map_ios",requestId);
		}		
	}

	@Override
	public void updateWebTokenList(int clientId, List<PushNotification> tokenList, int identifiedFlag,String requestId , String browserType) {
		if(browserType.equals(Util.BROWSER_TYPE_CHROME)) {
			updateToken(clientId, tokenList, identifiedFlag, "web_token_map_chrome", "anon_web_token_map_chrome",requestId);
		}
		else if(browserType.equals(Util.BROWSER_TYPE_SAFARI)) {
			updateToken(clientId, tokenList, identifiedFlag, "web_token_map_apns", "anon_web_token_map_apns",requestId);
		}	
	}
	

	private int updateToken(int clientId, String oldToken, String newToken, int identifiedFlag,String identifiedTable,String anonTable,String requestId){
		String[] dbName = getDBname(clientId);
		logger.info(requestId+" - Update token "+oldToken+" ==> "+newToken);
		String tableName = (identifiedFlag == 1) ? identifiedTable : anonTable;
		String query = "update " + dbName[0] + "." + tableName +  " set token = ? where token = ?";
		Object[] args = new Object[] {newToken,oldToken};
		JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName[1]);
		return jdbcTemplate.update(query, args);

	}
	
	
	private void updateToken(int clientId, List<PushNotification> pns, int identifiedFlag, String identifiedTable,
			String anonTable, String requestId) {
		String[] dbName = getDBname(clientId);

		String tableName = (identifiedFlag == 1) ? identifiedTable : anonTable;
		String query = "update " + dbName[0] + "." + tableName + " set token = ? where token = ?";
		List<Object[]> batchArgs = new ArrayList<Object[]>();

		for (PushNotification pn : pns) {
			logger.info(requestId + " - Update token " + pn.getToken() + " ==> " + pn.getNewtoken());
			Object[] args = new Object[] { pn.getNewtoken(), pn.getToken() };
			batchArgs.add(args);
		}

		JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName[1]);
		jdbcTemplate.batchUpdate(query, batchArgs);
		batchArgs.clear();

	}

	@Override
	public List<PushNotification> getAppTokensForGuids(int clientId,List<PushNotification> pns, String osType, int identifiedFlag,String requestId) {
		if(osType.equals(Util.OS_TYPE_ANDROID)){
			return getTokensForGuids(clientId, pns, osType, identifiedFlag, "app_token_map_android", "anon_app_token_map_android",requestId);
		}else if (osType.equals(Util.OS_TYPE_IOS)){
			return getTokensForGuids(clientId, pns, osType, identifiedFlag, "app_token_map_ios", "anon_app_token_map_ios",requestId);
		}
		return null;
	}

	@Override
	public List<PushNotification> getWebTokensForGuids(int clientId,List<PushNotification> pns, String browserType, int identifiedFlag,String requestId) {
		if(browserType.equals(Util.BROWSER_TYPE_CHROME)){
			return getTokensForGuids(clientId, pns, browserType, identifiedFlag, "web_token_map_chrome", "anon_web_token_map_chrome",requestId);
		}else if (browserType.equals(Util.BROWSER_TYPE_SAFARI)){
			return getTokensForGuids(clientId, pns, browserType, identifiedFlag, "web_token_map_apns", "anon_web_token_map_apns",requestId);			
		}
		return null;
	}
	
	private List<PushNotification> getTokensForGuids(int clientId,List<PushNotification> pns, String browserType, int identifiedFlag,String identifiedTable,String anonTable,String requestId){
		Map<String, PushNotification> map = new HashMap<String, PushNotification>();
		List<String> pushids = new ArrayList<String>();
		String[] dbName = getDBname(clientId);
		JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName[1]);
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
		for(PushNotification pn : pns){
			pushids.add(pn.pushid);
			map.put(pn.pushid, pn);
		}
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", pushids);
		SqlRowSet srs = null;
		String tableName = (identifiedFlag == 1) ? identifiedTable : anonTable;
		srs = namedJdbcTemplate.queryForRowSet("select guid,token from "+dbName[0] + "." + tableName+" where guid IN (:ids)",parameters);
		int i=0;
		while (srs.next()){
			i++;(map.get(srs.getString("guid"))).token = srs.getString("token");
		}
		logger.info(requestId+" - Tokens searched "+pns.size()+"; Tokens retrieved from DB "+i);		return pns;
	}


	@Override
	public int deleteInvalidAppToken(int clientId, int userId, String guid, String token, int identifiedFlag, String ostype,String requestId) {
		String tokenTable = null;
		if (ostype.equals(Util.OS_TYPE_ANDROID)){
			tokenTable = (identifiedFlag == 1) ? "app_token_map_android" : "anon_app_token_map_android";
		}
		else if(ostype.equals(Util.OS_TYPE_IOS)) {
			tokenTable = (identifiedFlag == 1) ? "app_token_map_ios" : "anon_app_token_map_ios";
		}
		
		String guidMasterTable = (identifiedFlag == 1) ? "user_guid_master" : "anon_user_guid_master";
		
		return deleteInvalidToken(clientId, userId, guid, token, tokenTable, guidMasterTable,requestId);
	}	
	
	
	@Override
	public void deleteInvalidAppTokenBatch(int clientId, List<PushNotification> InvalidAppToken, int identifiedFlag, String ostype,String requestId) {
		String tokenTable = null;
		if (ostype.equals(Util.OS_TYPE_ANDROID)){
			tokenTable = (identifiedFlag == 1) ? "app_token_map_android" : "anon_app_token_map_android";
		}
		else if(ostype.equals(Util.OS_TYPE_IOS)) {
			tokenTable = (identifiedFlag == 1) ? "app_token_map_ios" : "anon_app_token_map_ios";
		}
		
		String guidMasterTable = (identifiedFlag == 1) ? "user_guid_master" : "anon_user_guid_master";
		
		deleteInvalidTokenBatch(clientId, InvalidAppToken, tokenTable, guidMasterTable,requestId);
	}

	@Override
	public int deleteInvalidWebToken(int clientId, int userId, String guid, String token, int identifiedFlag, String ostype,String requestId) {
		String tokenTable = null;
		if (ostype.equals(Util.BROWSER_TYPE_CHROME)){			
			tokenTable = (identifiedFlag == 1) ? "web_token_map_chrome" : "anon_web_token_map_chrome";
		}
		else if(ostype.equals(Util.BROWSER_TYPE_SAFARI)) {			
			tokenTable = (identifiedFlag == 1) ? "web_token_map_safari" : "anon_web_token_map_safari";
		}
		
		String guidMasterTable = (identifiedFlag == 1) ? "user_guid_master" : "anon_user_guid_master";
		return deleteInvalidToken(clientId, userId, guid, token, tokenTable, guidMasterTable,requestId);
	}
	
	@Override
	public void deleteInvalidWebToken(int clientId, List<PushNotification> InvalidAppToken, int identifiedFlag,String ostype, String requestId) {
		
		String tokenTable = null;
		if (ostype.equals(Util.BROWSER_TYPE_CHROME)){			
			tokenTable = (identifiedFlag == 1) ? "web_token_map_chrome" : "anon_web_token_map_chrome";
		}
		else if(ostype.equals(Util.BROWSER_TYPE_SAFARI)) {			
			tokenTable = (identifiedFlag == 1) ? "web_token_map_safari" : "anon_web_token_map_safari";
		}
				
		String guidMasterTable = (identifiedFlag == 1) ? "user_guid_master" : "anon_user_guid_master";
		deleteInvalidTokenBatch(clientId, InvalidAppToken, tokenTable, guidMasterTable,requestId);
	}

	private int deleteInvalidToken(int clientId, int userId, String guid, String token,  String tokenTable, String guidMasterTable,String requestId) {
		String[] dbName = getDBname(clientId);
		
		String query = "update "+dbName[0] + "." + guidMasterTable+" set enabled=0 where userid = ? and guid= ?";
		Object[] args = new Object[] {userId, guid};
		JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName[1]);
		jdbcTemplate.update(query, args);
		
		logger.info(requestId+" - Delete token ==> "+token);
		query = "delete from "+dbName[0] + "." + tokenTable+" where token = ?";
		args = new Object[] {token};
		return jdbcTemplate.update(query, args);
	}
	
	private void deleteInvalidTokenBatch(int clientId, List<PushNotification> InvalidAppToken,  String tokenTable, String guidMasterTable,String requestId) {
		String[] dbName = getDBname(clientId);
		
		String updateQuery = "update "+dbName[0] + "." + guidMasterTable+" set enabled=0 where userid = ? and guid= ?";
		String deleteQuery = "delete from "+dbName[0] + "." + tokenTable+" where token = ?";
		
		List<Object[]> updateBatchArgs = new ArrayList<Object[]>();
		List<Object[]> deleteBatchArgs = new ArrayList<Object[]>();
		
		for(PushNotification pn : InvalidAppToken) {
		Object[] updateArgs = new Object[] {pn.getUserid(), pn.getPushid()};
		updateBatchArgs.add(updateArgs);
		
		logger.info(requestId+" - Delete token ==> "+pn.getToken());
		Object[] deleteArgs = new Object[] {pn.getToken()};
		deleteBatchArgs.add(deleteArgs);
		}
		JdbcTemplate updateJdbcTemplate = getJdbcTemplate(dbName[1]);
		updateJdbcTemplate.batchUpdate(updateQuery, updateBatchArgs);
		updateBatchArgs.clear();
		
		JdbcTemplate deleteJdbcTemplate = getJdbcTemplate(dbName[1]);
		deleteJdbcTemplate.batchUpdate(deleteQuery, deleteBatchArgs);
		deleteBatchArgs.clear();
		
	}
	

	@Override
	public Map<String, Object> getIOSCertFile(int cid, String appid, String eventType) {
		String query = null;
		if(eventType.equals(Util.EVENT_TYPE_APP)){
			query = "select ios_p12,ios_pass from papi_automation.push_app_master where cid = ? and appid = ? ";
		}else {
			query = "select ios_p12,ios_pass from papi_automation.push_web_master where cid = ? and siteid = ? ";
		}
		Object[] args = new Object[] {cid,appid};
		return papiJdbcTemplate.queryForMap(query, args);
	}

	@Override
	public int publishSummary(int clientId, int msgId, int sentCount, int failedCount, int frequencyCapping,String eventType,String requestId) {
		try{
			String[] dbName = getDBname(clientId);
			String tableName = null;
			if(eventType.equals(Util.EVENT_TYPE_APP)){
				tableName = "push_app_summary";
			}
			else {
				tableName = "push_web_summary";
			}
			String query = "insert into " + dbName[0] + "." + tableName + "(msgid,sent,failed,frequency_capping) values (?,?,?,?) on duplicate key update sent=sent+?,failed=failed+?,frequency_capping=frequency_capping+?";
			Object[] args = new Object[] {msgId,sentCount,failedCount,frequencyCapping,sentCount, failedCount,frequencyCapping};
			JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName[1]);
			return jdbcTemplate.update(query, args);
		}
		catch (Exception e){
			logger.error(requestId+" - "+e.getMessage());
			return 0;
		}
	}

	@Override
	public boolean skipFrequencyCapping(int cid, int msgid,int channelId) {
		String[] dbName = getDBname(cid);
		JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName[1]);
		String query = "Select count(1) from "+dbName[0]+".skip_frequency_capping where messageid=? AND channelid=?";
		Object[] args = new Object[] {msgid,channelId};
		try{
			int c = jdbcTemplate.queryForObject(query,args,Integer.class);
			if(c>0){
				return true;
			}
		}catch(EmptyResultDataAccessException e){
			return false;
		}
		return false;
	}

	@Override
	public void disableGuid(int cid, int userid, String guid, int identifiedFlag,String requestId) {
		try{
			String guidMasterTable = (identifiedFlag == 1) ? "user_guid_master" : "anon_user_guid_master";
			String[] dbName = getDBname(cid);
			String query = "update "+dbName[0] + "." + guidMasterTable+" set enabled=0 where userid = ? and guid= ?";
			Object[] args = new Object[] {userid, guid};
			JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName[1]);
			jdbcTemplate.update(query, args);
		}
		catch(Exception e){
			logger.error(requestId+" - "+e.getMessage(),e);
		}
	}
	
	@Override
	public void disableGuidBatch(int cid, List<PushNotification> InvalidTokenList, int identifiedFlag, String requestId) {
		try {
			String guidMasterTable = (identifiedFlag == 1) ? "user_guid_master" : "anon_user_guid_master";
			String[] dbName = getDBname(cid);
			List<Object[]> batchArgs = new ArrayList<Object[]>();
			String query = "update " + dbName[0] + "." + guidMasterTable
					+ " set enabled=0 where userid = ? and guid= ?";
			for (PushNotification pn : InvalidTokenList) {
				Object[] args = new Object[] { pn.getUserid(), pn.getPushid() };
				batchArgs.add(args);
			}
			JdbcTemplate jdbcTemplate = getJdbcTemplate(dbName[1]);
			jdbcTemplate.batchUpdate(query, batchArgs);
			batchArgs.clear();
		} catch (Exception e) {
			logger.error(requestId + " - " + e.getMessage(), e);
		}
	}
	

	@Override
	public int getWebSourceId(int clientId, String siteId) throws ExecutionException {
		return getWebOrAppSourceId(clientId, siteId, "id", "siteid", "push_web_master");
    }

	@Override
    public int getWebOrAppSourceId(int cid, String appIdOrSiteId, String idColumn, String appIdOrSiteIdColumn, String table) {
        String query = "Select " + idColumn + " from papi_automation." + table + " where cid= ? and " + appIdOrSiteIdColumn + "= ? ";
        Object[] args = new Object[] { cid, appIdOrSiteId};
        try {
        	return papiJdbcTemplate.queryForObject(query, args, Integer.class);
        } catch (EmptyResultDataAccessException dae) {
            logger.error("site id does not exists for client id" + cid, dae);
            return 0;
        }
	}
}
