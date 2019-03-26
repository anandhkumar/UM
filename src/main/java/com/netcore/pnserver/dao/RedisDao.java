package com.netcore.pnserver.dao;

import java.util.List;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisDao {
	private JedisPool jedispool;
	private JedisPool Tjedispool;
	private JedisPool summarizerJedisPool;
	private JedisPool pajedispool;
	final static Logger logger = Logger.getLogger(RedisDao.class);
	
	public RedisDao(JedisPool jedispool, JedisPool Tjedispool, JedisPool summarizerJedisPool, JedisPool pajedispool) {
		this.jedispool = jedispool;
		this.Tjedispool = Tjedispool;
		this.summarizerJedisPool = summarizerJedisPool;
		this.pajedispool = pajedispool;
	}

	public String dequeue(String queue, String requestId) {
		Jedis jedis =null;
		try {
			jedis = jedispool.getResource();
			List<String> str = jedis.blpop(10, queue);
			if(str!=null){
				logger.info(requestId+" - RPUSH "+queue+" '"+str.get(1)+"'");
				return str.get(1);
			}
			else 
				return null; 
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (jedis != null && jedispool !=null) {
				jedispool.returnBrokenResource(jedis);
				jedis =null;
			}
		} finally {
			if (jedis != null && jedispool !=null) {
				jedispool.returnResource(jedis);
				jedis=null;
			}
		}
		return null;
	}
		
	public void publishPNBC(String queue, String json){
		Jedis jedis =null;
		try {
			jedis = jedispool.getResource();
			jedis.rpush(queue,json);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (jedis != null && jedispool !=null) {
				jedispool.returnBrokenResource(jedis);
				jedis =null;
			}
		} finally {
			if (jedis != null && jedispool !=null) {
				jedispool.returnResource(jedis);
				jedis=null;
			}
		}
	}
	
	public void publishPNBCList(String queue, List<String> jsonList) {
		try (Jedis jedis = jedispool.getResource()) {
			Pipeline p = jedis.pipelined();
			for (String json : jsonList) {
				p.rpush(queue, json);
			}
			p.sync();
		}catch(Exception e){
			logger.info("Exception while publishing events  : " + e.getMessage());			
		}
	}
	
	public void enqueueTestRedis(String queueName, String redisString) {
		try (Jedis jedis = Tjedispool.getResource()) {
			jedis.rpush(queueName, redisString);
		} 
		
	}
	
	public void enqueueRedis(String queueName, String redisString) {
		try (Jedis jedis = pajedispool.getResource()) {
			jedis.rpush(queueName, redisString);
		} 
		
	}
	
	public String hGet(String mapName, String item) {
		try (Jedis jedis = jedispool.getResource()) {
			return jedis.hget(mapName, String.valueOf(item));
		}
	}
	
	public String get(String queueName) {
		try (Jedis jedis = jedispool.getResource()) {
			return jedis.get(queueName);
		}
	}
	
	public void publishToSummarizerRedis(String key, String data,String requestId){
		try (Jedis jedis = summarizerJedisPool.getResource()) {
			jedis.rpush(key, data);
		}
		catch(Exception e){
			logger.info(requestId+" - Retry publishing events to summarizer due to: " + e.getMessage());
			try (Jedis jedis = summarizerJedisPool.getResource()) {
				jedis.rpush(key, data);
			}
		}
	}
	
	public void publishToSummarizerRedisList(String key, List<String> jsonList,String requestId){
		try (Jedis jedis = summarizerJedisPool.getResource()) {			
			Pipeline p = jedis.pipelined();
			for (String json : jsonList) {
				p.rpush(key, json);
			}
			p.sync();
		}catch(Exception e){
			logger.info(requestId+" - Exception while publishing events to summarizer : " + e.getMessage());			
		}
	}
	
	public void destroy() {
		logger.info("====== Destroying jedis pool ====== ");
        if (jedispool != null) {
            try {
            	jedispool.destroy();
            } catch (Exception ex) {
                logger.warn("Cannot properly close Jedis pool", ex);
            }
            jedispool = null;
        }
        logger.info("====== Jedis pool destroyed =========");
    }
	
}
