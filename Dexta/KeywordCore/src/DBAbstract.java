package com.dexta.coreservices.models;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import org.bson.types.ObjectId;

public class DBAbstract extends BasicDBObject {
	
	public static String DB_NAME = "keywords";
	
	public DBAbstract() {
		super(); //init upper
	}
	
	private DBCollection getMongoCollection(DB systemDB) {
		String thisCollectionName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.') + 1, this.getClass().getName().length()).toLowerCase();
		return systemDB.getCollection(thisCollectionName);
	}
	
	public ObjectId getID() {
		return (ObjectId) this.get("_id");
	}
	
	public void commit(DB systemDB) {
		this.getMongoCollection(systemDB).save(this);
	}
	
	public boolean find(DB systemDB) {
		DBObject result = this.getMongoCollection(systemDB).findOne(this);
		if (result != null) {
			for (String key : result.keySet())
				this.put(key, result.get(key));
				
			return true;
		}
		
		return false;
	}
}