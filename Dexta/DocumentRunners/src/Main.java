package com.dexta.runners.documentprocessors;

import com.dexta.coreservices.models.users.User;
import com.dexta.coreservices.models.users.UserService;
import com.dexta.coreservices.models.documents.Document;
import com.dexta.coreservices.models.documents.PendingDocument;
import com.dexta.coreservices.models.services.Service;
import com.dexta.tools.StorageWrapper;

import com.dexta.processors.DocProcessor;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.BasicDBObject;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.HashMap;
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
		
		PendingDocument doc;
		boolean quit = false;
		
		while (!quit) {
			doc = PendingDocument.getPendingDocumentInQueue(database, "doc");
			if (doc != null) {
				doc.setLocked(true);
				doc.commit(database);
				System.out.println("Processing " + doc.getfileName());
				try {
					Document newDocument = doc.transposeToDocument(common, new DocProcessor());
					
					//check to see if the document already exists
					HashMap<String, Object> removedContents = newDocument.removeNonImportants();
					boolean find = newDocument.find(database); //if found the id would of been set, allowing us to perform an update automatically
					//place the remove objects back in
					for (String key : removedContents.keySet())
						newDocument.put(key, removedContents.get(key));

					newDocument.commit(database);
					doc.delete(database);
				} catch (Exception err) {
					doc.put("error", err);
					doc.commit(database);
				}
			} else {
				System.out.println("No document in queue");
				Thread.sleep(5 * 1000);
			}
		}
		
		common.memcachedServer.shutdown();
	}
}