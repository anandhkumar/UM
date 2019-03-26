package com.netcore.pnserver.dequeuer;

import java.util.UUID;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.processor.AbstractTask;
import com.netcore.pnserver.processor.TaskManager;

/**
 * @author Swapnil Srivastav
 *
 */
public class GenericDequeuer extends Thread implements ApplicationContextAware {
	
	public boolean stop;
	private static ApplicationContext applicationContext = null;
	private String queueName;
	private RedisDao redisDao;
	private TaskManager taskManager;
	final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GenericDequeuer.class); 
	
	public GenericDequeuer(RedisDao redisDao,String queueName,TaskManager taskManager){
		this.redisDao = redisDao;
		this.queueName = queueName;
		this.taskManager = taskManager;
	}
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		GenericDequeuer.applicationContext = applicationContext;
	}


	/**
	 * @return the queueName
	 */
	public String getQueueName() {
		return queueName;
	}


	/**
	 * @param queueName the queueName to set
	 */
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	@Override
	public void run(){
		logger.info("Started dequeuer for "+this.queueName);
		while(!stop){
			try{
				String requestId = UUID.randomUUID().toString();
				String str = redisDao.dequeue(queueName,requestId);
				if(str==null)
					continue;
				AbstractTask task = (AbstractTask)applicationContext.getBean(queueName);
				task.redisString = str;
				task.requestId = requestId;
				taskManager.getExecutorService().submit(task);					
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
			
		}
		logger.info("Stoped dequeuer for "+this.queueName);
	}
	
	
}
