package com.dexta.coreservices.models;

import com.mongodb.DB;

public class KeywordDocument extends DBAbstract {
	
	Document parent;
	Keyword word;
	int position;
	DB tempDB;
	
	public KeywordDocument(String keyword, Document document, int wordCount) throws Exception {
		super();
		word = new Keyword(keyword);
		parent = document;
		position = wordCount;
	}
	
	public Keyword getKeyword() {
		return word;
	}
	
	private void setDocument() {
		this.put("document", parent.getID());
		this.put("user", parent.getUserID());
	}
	
	private void setKeyword() {
		word.commit(tempDB);
		this.put("keyword", word.getID());
	}
	
	private void setPosition() {
		this.put("position", position);
	}
	
	public void commit(DB systemDB) {
		tempDB = systemDB;
		this.setDocument();
		this.setKeyword();
		this.setPosition();
		//we don't want duplicates. The keyword ID, position, document and user define uniqueness
		if (!this.find(systemDB))
			super.commit(systemDB);
	}
}