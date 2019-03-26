package com.netcore.pnserver.processor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Swapnil Srivastav
 *
 */
public class TaskManager {
	private BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(20); // Maximum Size of Thread pool
	private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
	private ExecutorService executorService = new ThreadPoolExecutor(
			10, 20, 120L, TimeUnit.SECONDS, blockingQueue,
			rejectedExecutionHandler);
	
	public ExecutorService getExecutorService() {
		return executorService;
	}


}
