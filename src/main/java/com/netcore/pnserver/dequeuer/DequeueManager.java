package com.netcore.pnserver.dequeuer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.processor.TaskManager;


/**
 * <h1>Dequeue Manager!</h1>
 * Dequeue Manager manages all the dequeuers threads as configured 
 * in Spring application context.
 * 
 * @author  Kedar Parikh
 * @version 1.0
 * @since   2014-03-31
 */
public class DequeueManager implements ApplicationContextAware{

	final static Logger logger = Logger.getLogger(DequeueManager.class);
	private static ApplicationContext applicationContext = null;
	private TaskManager taskManager;
	private List<String> dqConf;
	private List<GenericDequeuer> gdList = new ArrayList<GenericDequeuer>();
	private RedisDao redisDao;

	
	public DequeueManager(List <String> dqConf,RedisDao redisDao){
		this.dqConf = dqConf;
		this.redisDao = redisDao;
	}


	/**
	 * Starts all dequeuers based on their Spring configuration.
	 */
	public void startDequeuers(){
		this.taskManager = (TaskManager) applicationContext.getBean("taskManager");
		for(String queueName : this.dqConf){
			try{
				GenericDequeuer genericDequeuer = (GenericDequeuer) applicationContext.getBean("gd_"+queueName);
				genericDequeuer.setName(queueName);
				genericDequeuer.start();
				gdList.add(genericDequeuer);
			}catch(BeansException e){
				logger.error(e.getMessage(),e);
			}

		}	
	}

	/**
	 * Stops all the Dequeuers for all the queues
	 * @return void
	 * @throws InterruptedException 
	 */
	public void stopAll() throws InterruptedException{
		for(GenericDequeuer gd : gdList){
			gd.stop=true;
		}
		
		for(GenericDequeuer gd : gdList){
			gd.join();
		}
		
		logger.info("======== Stopped all Dequeue threads ==========");
		ExecutorService es = this.taskManager.getExecutorService();
		es.shutdown();
		//Wait for all running tasks 
		while (!es.isTerminated()) {
			Thread.sleep(1000);
			logger.info("Waiting for tasks to complete..");
		}
		logger.info("======== All tasks complete ==========");

		redisDao.destroy();

	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public List getDqrs(){
		return gdList;
	}
	
	public void startSingle(String queueName){
		try {
			logger.info("Start Single Dqr: "+ queueName);
			GenericDequeuer gd = (GenericDequeuer) applicationContext.getBean("gd_"+queueName);
			gd.setName(gd.getQueueName());
			gd.start();
			
			Iterator<GenericDequeuer> iterator = gdList.iterator();
			//find first occurrence and remove
			while(iterator.hasNext()){
				GenericDequeuer gdeq = iterator.next();
				if (gdeq.getQueueName().equals(queueName)){
					gdList.remove(gdeq);
					break;
				}
			}
			gdList.add(gd);
		} catch (BeansException e) {
			logger.error(e.getMessage(),e);
		}
	}
	
	public void stopSingle(String queueName){
		logger.info("Stop Single Dqr: "+ queueName);
		for(GenericDequeuer gd: gdList){
			if (gd.getQueueName().equals(queueName)){
				gd.stop = true;
			}
		}
	}

	public List<String> getDqConf() {
		return dqConf;
	}

	public void setDqConf(List<String> dqConf) {
		this.dqConf = dqConf;
	}
	
}
