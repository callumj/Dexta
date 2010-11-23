package com.dexta.coreservices.models.services;

import com.dexta.coreservices.models.base.DBAbstract;

public class ServiceProvider extends DBAbstract {
	
	public ServiceProvider(String name) {
		super();
		this.setName(name);
	}
	
	public void setName(String name) {
		this.put("name", name);
	}
}