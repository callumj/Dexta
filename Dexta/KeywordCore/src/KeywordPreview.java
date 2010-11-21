package com.dexta.coreservices.models;

import com.mongodb.DB;

public class KeywordPreview extends DBAbstract {
	
	public KeywordPreview(String preview) {
		super();
		this.setPreview(preview);
	}
	
	public void setPreview(String preview) {
		this.put("text", preview);
	}
	
	public String getPreview() {
		return (String) this.get("text");
	}
	
	public void commit(DB systemDB) {
		if (!this.find(systemDB))
			super.commit(systemDB);
	}
}