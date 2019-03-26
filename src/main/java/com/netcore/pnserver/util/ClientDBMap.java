package com.netcore.pnserver.util;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import com.netcore.pnserver.servlets.ResetDB;
import org.json.simple.parser.JSONParser;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import org.json.simple.JSONObject;

public class ClientDBMap {
	final static Logger logger = Logger.getLogger(ClientDBMap.class);
	private Map<Integer,String> map;
	private JedisPool redis;
	private Map<Integer,String> mongoMap;
	
	public ClientDBMap(Map map, JedisPool redis,Map mongoMap){
		this.map = map;
		this.redis = redis;
		this.mongoMap = mongoMap;
	}

	public String getDB(Integer clientId) {
		String dbname = map.get(clientId);
		if (dbname == null){
			synchronized (this) {
				dbname = map.get(clientId);
				if (dbname == null){
					Jedis jedis =null;
					try{
						jedis = redis.getResource();
						dbname= jedis.hget("p1_cmap", String.valueOf(clientId));
					}catch (Exception e) {
						logger.error(e.getMessage(), e);
						if (jedis != null && redis !=null) {
							redis.returnBrokenResource(jedis);
							jedis =null;
						}
					} finally {
						if (jedis != null && redis !=null) {
							redis.returnResource(jedis);
							jedis=null;
						}
					}
					if (dbname == null)
						throw new RuntimeException("DBname not found in REDIS for clientId "+ clientId);
					else{
						map.put(clientId, dbname);
					}
				}
				return map.get(clientId);
			}
		} return map.get(clientId);
	}

	public String updateMap(String clientId){
		JSONObject jsonObject = new JSONObject();
		
		if(clientId != null && !clientId.isEmpty()){
			synchronized (this){
				try{
					int varClientId = Integer.parseInt(clientId);
					
					if(map.get(varClientId) != null){
						map.remove(varClientId);
						mongoMap.remove(varClientId);
						jsonObject.put("status", "1");
						jsonObject.put("message", "Success");
					}
					else{
						jsonObject.put("status", "2");
						jsonObject.put("message", "Client id " + varClientId + " does not exist in cache");
					}
				}
				catch(NumberFormatException e){
					jsonObject.put("status", "0");
					jsonObject.put("message", "Invalid number format");
				}
			}
		}else{
			jsonObject.put("status", "0");
			jsonObject.put("message", "Client id is null");
		}
		
		logger.info(jsonObject.toString());
		return jsonObject.toString();
	}
	
	public String getDBMongoDB(Integer clientId) {
		String dbname = mongoMap.get(clientId);
		if (dbname == null){
			initClientMongoDbMap(clientId);
		} return mongoMap.get(clientId);		
	}
	
	public synchronized void initClientMongoDbMap(Integer clientId){
		String dbname=null;
		synchronized (this) {
			dbname= getMongoDB(clientId);
			if (dbname == null){
				throw new RuntimeException("DBname not found in REDIS for clientId "+ clientId);
			}
			else{
				mongoMap.put(clientId, dbname);
			}
			logger.info("ClientDB Map Updated: "+clientId + " "+dbname);
		}
	}
	
	private String getMongoDB(int clientId){
		try (Jedis jedis = redis.getResource()) {
			String dbConfig = jedis.hget("p1_mgmap", String.valueOf(clientId)); //dbName@mysqlDBServer@mongogroup@IDC
			String[] dbConfigArr = dbConfig.split("@");
			String mongoGroup = dbConfigArr[2];
			String JSONConfig = jedis.hget("p1_mgaccess", mongoGroup); // {\"host\":\"192.168.50.167,192.168.50.169\",\"readuser\":{\"user\":\"read\",\"pass\":\"PzErDw\\u003d\\u003d\"},\"writeuser\":{\"user\":\"write\",\"pass\":\"OiYjHz8\\u003d\"},\"mgtype\":\"0\",\"port\":\"27017\"}
			JSONParser parser = new JSONParser(); 
			JSONObject json = (JSONObject) parser.parse(JSONConfig);
			String msgType = json.get("mgtype").toString();
			return dbConfigArr[0]+"@"+msgType; //dbName@1 or dbName@2 1 is normal and 2 is encrypted
		}catch (Exception e) {
			logger.error(e);
		}
	return null;
	}
}
