package com.dexta.coreservices.models.services;

import com.dexta.coreservices.models.base.DBAbstract;

import com.mongodb.DB;

public class Service extends DBAbstract {
	
	public Service() {
		super();
	}
	
	public Service(Service importFrom) {
		this();
		for (String key : importFrom.keySet())
			this.put(key, importFrom.get(key));
	}
	
	public String getType() {
		return (String) this.get("type");
	}
	
	public String createClassType() {
		return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.') + 1, this.getClass().getName().length()).toLowerCase();
	}
	
	public void commit(DB systemDB) {
		this.put("type", this.createClassType());
		systemDB.getCollection("service").save(this);
	}
}