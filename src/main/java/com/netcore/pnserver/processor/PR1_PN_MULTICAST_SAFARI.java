package com.netcore.pnserver.processor;


import com.netcore.pnserver.dao.MongoDao;
import com.netcore.pnserver.dao.MySqlDao;
import com.netcore.pnserver.dao.RedisDao;

import com.netcore.pnserver.util.Util;

public class PR1_PN_MULTICAST_SAFARI extends PR1_PN_MULTICAST_WEB{

	public PR1_PN_MULTICAST_SAFARI(String queue, RedisDao simpleDequeuer, MySqlDao mysqlDao, MongoDao mongoDao,
			Util util) {
		super(queue, simpleDequeuer, mysqlDao, mongoDao, util);
		// TODO Auto-generated constructor stub
	}
}
