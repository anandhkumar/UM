package com.netcore.pnserver.processor;

/**
 * @author Swapnil Srivastav
 *
 */
public abstract class AbstractTask implements Runnable {
	public String redisString;
	public String requestId;
}
