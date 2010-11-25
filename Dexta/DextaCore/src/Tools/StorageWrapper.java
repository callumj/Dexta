package com.dexta.tools;

import com.mongodb.DB;
import net.spy.memcached.MemcachedClient;

public class StorageWrapper {
	public DB mongoDatabase;
	public MemcachedClient memcachedServer;
	
	public static int MAX_MEMCACHED_STORE_TIME = 8 * 60 * 60; //8 hours
}