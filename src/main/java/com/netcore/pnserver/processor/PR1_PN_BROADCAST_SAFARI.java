package com.netcore.pnserver.processor;

import com.netcore.pnserver.dao.MySqlDao;
import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.util.Util;

public class PR1_PN_BROADCAST_SAFARI extends PR1_PN_BROADCAST_WEB {

	public PR1_PN_BROADCAST_SAFARI(String queue, RedisDao simpleDequeuer, MySqlDao mysqlDao, Util util) {
		super(queue, simpleDequeuer, mysqlDao, util);
		// TODO Auto-generated constructor stub
	}
}
