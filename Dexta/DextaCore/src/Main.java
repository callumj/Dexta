package com.dexta.coreservices.models;

import com.dexta.coreservices.models.users.User;
import com.dexta.coreservices.models.users.UserService;
import com.dexta.coreservices.models.documents.Document;

import com.dexta.coreservices.models.services.AWSS3;
import com.dexta.coreservices.models.services.GoogleDocs;
import com.dexta.coreservices.models.services.Service;

import com.dexta.tools.StorageWrapper;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.BasicDBObject;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.net.InetSocketAddress;

import net.spy.memcached.MemcachedClient;

public class Main
{ 
	public static void main(String [] args) throws Exception
	{
		Mongo m = new Mongo("localhost", 27019);
		DB database = m.getDB("dexta");
		
		User myself = new User();
		myself.setEmail("callum@callumj.com");
		myself.find(database);
		
		StorageWrapper common = new StorageWrapper();
		common.mongoDatabase = database;
		common.memcachedServer = new MemcachedClient(new InetSocketAddress("localhost", 11211));
		
		List<Service> collection = UserService.getServicesForUser(database, myself, new GoogleDocs());
		System.out.println(collection.size());
		
		for (Service svc : collection) {
			GoogleDocs dSvc = new GoogleDocs(svc);
			System.out.println(dSvc.getName());
			dSvc.addNewFiles(common);
		}
		
		common.memcachedServer.shutdown();
	}
	
	public static void doIndex(DB database) throws Exception {
		//setup indexes
		database.getCollection("keyword").createIndex(new BasicDBObject("word", 1));
		database.getCollection("keywordpreview").createIndex(new BasicDBObject("text", 1));
		database.getCollection("keyworddocument").createIndex(new BasicDBObject("user", 1));
		database.getCollection("keyworddocument").createIndex(new BasicDBObject("document", 1));
		database.getCollection("keyworddocument").createIndex(new BasicDBObject("keyword", 1));
		database.getCollection("user").createIndex(new BasicDBObject("email", 1));
	}

}