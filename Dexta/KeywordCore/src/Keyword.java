package com.dexta.coreservices.models;

import com.mongodb.DB;

public class Keyword extends DBAbstract {
	
	/**
	 * Create a new keyword object, with the keyword
	 */	
	public Keyword(String word) throws Exception {
		super(); //init upper
		setKeyword(word);
	}
	
	/**
	 * Set the keyword
	 */
	public void setKeyword(String word) throws Exception {
		word = word.replaceAll("[^A-Za-z0-9 ]", " ");
		word = word.replaceAll("\\s+", " ");
		
		if (word.length() == 0)
			throw new Exception("Word is empty");
			
		this.put("word", word.toLowerCase().trim());
	}
	
	/**
	 * Get the keyword
	 */
	public String getKeyword() {
		return (String) this.get("word");
	}
	
	/**
	 * Commit the keyword to the database, duplicates will not be created automatically
	 */
	public void commit(DB systemDB) {
		if (!this.find(systemDB))
			super.commit(systemDB);
	}
	
	public String toString() {
		return getKeyword();
	}
}