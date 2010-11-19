package com.dexta.coreservices.models;

import com.mongodb.DB;

public class Keyword extends DBAbstract {
	
	/**
	 * Create a new keyword object, with the keyword
	 */	
	public Keyword(String word) throws Exception {
		super(); //init upper
		setImportant(false);
		setKeyword(word);
	}
	
	/**
	 * Set the keyword
	 */
	public void setKeyword(String word) throws Exception {
		word = word.replaceAll("[^A-Za-z0-9 ]", "");
		word = word.replaceAll("\\s+", " ");
		
		if (word.length() == 0)
			throw new Exception("Word is empty");
		
		int numImportant = 0;
		for (String innerWord : word.split(" ")) {
			if (innerWord.charAt(0) >= 65 && innerWord.charAt(0) <= 90)
				numImportant++;
		}
		
		if (numImportant >= 2)
			this.setImportant(true);
			
		this.put("word", word.toLowerCase().trim());
	}
	
	/**
	 * Get the keyword
	 */
	public String getKeyword() {
		return (String) this.get("word");
	}
	
	/**
	 * If this is a name of a person, or a name of a company it should be marked as important
	 */
	public void setImportant(boolean important) {
		this.put("important", true);
	}
	
	public Boolean isImportant() {
		return (Boolean) this.get("important");
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