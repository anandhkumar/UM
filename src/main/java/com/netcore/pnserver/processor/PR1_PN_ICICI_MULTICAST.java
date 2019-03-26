package com.netcore.pnserver.processor;

import com.netcore.pnserver.dao.MongoDao;
import com.netcore.pnserver.dao.MySqlDao;
import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.util.Util;

public class PR1_PN_ICICI_MULTICAST extends PR1_PN_MULTICAST{

	public PR1_PN_ICICI_MULTICAST(String queue, RedisDao simpleDequeuer, MySqlDao mysqlDao, MongoDao mongoDao,
			Util util) {
		super(queue, simpleDequeuer, mysqlDao, mongoDao, util, "icici_app");
		// TODO Auto-generated constructor stub
	}

}
