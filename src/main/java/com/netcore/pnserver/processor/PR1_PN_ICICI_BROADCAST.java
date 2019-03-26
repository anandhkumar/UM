package com.netcore.pnserver.processor;

import com.netcore.pnserver.dao.MySqlDao;
import com.netcore.pnserver.dao.RedisDao;
import com.netcore.pnserver.util.Util;

public class PR1_PN_ICICI_BROADCAST extends PR1_PN_BROADCAST{

	public PR1_PN_ICICI_BROADCAST(String queue, RedisDao simpleDequeuer, MySqlDao mysqlDao, Util util) {
		super(queue, simpleDequeuer, mysqlDao, util, "icici_app");
		// TODO Auto-generated constructor stub
	}

}
