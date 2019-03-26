package com.netcore.pnserver.dao;

import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.result.UpdateResult;

public interface MongoDao {
	
	AggregateIterable<Document>  checkFrequencyCappingAndReturnCappedUsers(Set<Integer> userIds, int clientId, String eventType,int flag,Map<Integer,Integer> periodThresholdMap,String requestId);

	UpdateResult updateWebToken(int cid, String token, String canonicalRegistrationId,String eventType);


}
