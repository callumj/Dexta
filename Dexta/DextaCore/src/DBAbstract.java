package com.dexta.coreservices.models.base;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import org.bson.types.ObjectId;

public class DBAbstract extends BasicDBObject {	
	public DBAbstract() {
		super(); //init upper
	}
	
	public static String classToCollectionName(Class theClass) {
		return theClass.getName().substring(theClass.getName().lastIndexOf('.') + 1, theClass.getName().length()).toLowerCase();
	}
	
	private DBCollection getMongoCollection(DB systemDB) {
		String thisCollectionName = classToCollectionName(this.getClass());
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