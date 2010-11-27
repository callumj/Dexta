package com.dexta.coreservices.models.services;

import com.dexta.coreservices.models.base.DBAbstract;
import com.dexta.tools.StorageWrapper;

import com.mongodb.DB;

import java.io.IOException;
public class StorageService extends DBAbstract {
	
	public StorageService() {
		super();
	}	
	
	public byte[] getData(StorageWrapper storageWrapper) {
		try {
			if (((String) this.get("type")).equals("awss3")) {
				AWSS3 file = new AWSS3(this);
				return file.getObjectContents(storageWrapper);
			}
		} catch (IOException ioErr) {}
			
		return null;
	}
	
	public void commit(DB systemDB) {
		this.put("type", this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.') + 1, this.getClass().getName().length()).toLowerCase());
		systemDB.getCollection("storageservice").save(this);
	}
	
	public void superCommit(DB systemDB) {
		this.put("type", this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.') + 1, this.getClass().getName().length()).toLowerCase());
		systemDB.getCollection("storageservice").save(this);
	}
}