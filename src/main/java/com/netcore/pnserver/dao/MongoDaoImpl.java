package com.netcore.pnserver.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.netcore.pnserver.util.ClientDBMap;
import com.netcore.pnserver.util.Util;


public class MongoDaoImpl implements MongoDao {
	
	final static Logger logger = Logger.getLogger(MongoDaoImpl.class);
	private MongoClient mongoClient;
	private MongoClient mongoClientCluster2;
	private MongoClient mongoClientEncrypt;	
	private Util pnUtil;
	private ClientDBMap cleintdbmap;
	public MongoDaoImpl(MongoClient mongoClient,MongoClient mongoClientCluster2,ClientDBMap cleintdbmap, Util pnUtil,MongoClient mongoClientEncrypt){
		this.mongoClient = mongoClient;
		this.mongoClientCluster2 = mongoClientCluster2;
		this.cleintdbmap = cleintdbmap;		
		this.pnUtil = pnUtil;
		this.mongoClientEncrypt = mongoClientEncrypt;
	}

	@Override
	public AggregateIterable<Document> checkFrequencyCappingAndReturnCappedUsers(Set<Integer> userIds, int clientId,String eventType,int flag,Map<Integer,Integer> periodThresholdMap,String requestId){
		MongoDatabase mongodb = getMongoDB(clientId);
		List<Integer> periods = new ArrayList<Integer>();
		periods.addAll(periodThresholdMap.keySet());
		List<Document> pipeLine = new ArrayList<Document>();
		Document match = new Document("$match", new Document("uid", new Document("$in",userIds))
										.append("period", new Document("$in",periods)));
		String counterKey;
		if(eventType==Util.EVENT_TYPE_APP) counterKey="$a_count"; else counterKey="$b_count";
		Document group = new Document("_id", "$uid");
		Document project1 = new Document("uid","$_id");
		Document project2 = new Document("uid", 1);
		StringBuffer sb = new StringBuffer();
		List<String> $period = new ArrayList<String>();
		for(Integer p : periods){
			$period.add("$"+p);
		}
		project2.append("exceeded", new Document("$or", $period));   
		Document match2 = new Document("$match",new Document("exceeded",true));
		for (Integer period : periodThresholdMap.keySet()) {
			group.append(period.toString(), new Document("$sum", new Document("$cond", new ArrayList<>(Arrays.asList(new Document("$eq", new ArrayList<>(Arrays.asList("$period", period))), counterKey, 0)))));
			
			project1.append(period.toString(), new Document("$cond",new ArrayList<>(Arrays.asList(new Document("$lt",new ArrayList<>(Arrays.asList("$"+period,periodThresholdMap.get(period)))),0,1))));
			
		}
		Document groupDocument = new Document("$group", group);
		pipeLine.add(match);
		pipeLine.add(groupDocument);
		pipeLine.add(new Document("$project",project1));
		pipeLine.add(new Document("$project",project2));
		pipeLine.add(match2);
		Document d = new Document("Query",pipeLine);
		logger.info(requestId+" - "+d.toJson());
		String collectionName = flag==1? Util.FREQUENCY_COLLECTION : Util.ANON_FREQUENCY_COLLECTION;
		return mongodb.getCollection(collectionName).aggregate(pipeLine);
	}
	
	private MongoDatabase getMongoDB(int clientid){
		String dbConfig = this.cleintdbmap.getDBMongoDB(clientid);
		String[] dbConfigArr = dbConfig.split("@");
		if(dbConfigArr[1].equals("1"))
			return mongoClient.getDatabase(dbConfigArr[0]);
		else if(dbConfigArr[1].equals("2"))
			return mongoClientEncrypt.getDatabase(dbConfigArr[0]);
		else if(dbConfigArr[1].equals("3"))
			   return mongoClientCluster2.getDatabase(dbConfigArr[0]);
		
		return null;
	}

	@Override
	public UpdateResult updateWebToken(int cid, String token,String canonicalRegistrationId,String eventType) {
		String collectionName = eventType.equals(Util.EVENT_TYPE_APP) ? Util.APN_TOKEN : Util.BPN_TOKEN;
		Document filter = new Document("_id", token);
		Document newToken = new Document("_id", canonicalRegistrationId);
		Document updateOperationDocument = new Document("$set", newToken);
		MongoDatabase mongodb = getMongoDB(cid);
		return mongodb.getCollection(collectionName).updateOne(filter, updateOperationDocument);
	}

}
