package com.dexta.coreservices.models.services;

import com.dexta.coreservices.models.base.DBAbstract;

import com.mongodb.DB;

public class Service extends DBAbstract {
	
	public Service() {
		super();
	}	
	
	public void commit(DB systemDB) {
		this.put("type", this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.') + 1, this.getClass().getName().length()).toLowerCase());
		systemDB.getCollection("service").save(this);
	}
}