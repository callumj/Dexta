package com.dexta.coreservices.models.keywords;

import com.dexta.coreservices.models.base.DBAbstract;
import com.dexta.coreservices.models.documents.Document;

import com.mongodb.DB;
import org.bson.types.ObjectId;

public class KeywordDocument extends DBAbstract {
	
	Document parent;
	Keyword word;
	KeywordPreview preview;
	DB tempDB;
	
	public KeywordDocument(String keyword, Document document) throws Exception {
		super();
		word = new Keyword(keyword);
		parent = document;
		this.setImportant(false);
		
		int numImportant = 0;
		for (String innerWord : keyword.split(" ")) {
			if (innerWord.charAt(0) >= 65 && innerWord.charAt(0) <= 90)
				numImportant++;
		}
		
		if (numImportant >= 2)
			this.setImportant(true);
	}
	
	public Keyword getKeyword() {
		return word;
	}
	
	public void setUser(ObjectId id) {
		this.put("user", id);
	}
	
	private void setDocument() {
		this.put("document", parent.getID());
	}
	
	private void setKeyword() {		
		word.commit(tempDB);
		this.put("keyword", word.getID());
	}
	
	public void setPosition(int position) {
		this.put("position", position);
	}
	
	public void setPreview(String previewTxt) {
		preview = new KeywordPreview(previewTxt);
	}
	
	/**
	 * If this is a name of a person, or a name of a company it should be marked as important
	 */
	public void setImportant(boolean important) {
		this.put("important", important);
	}
	
	public Boolean isImportant() {
		return (Boolean) this.get("important");
	}
	
	public void commit(DB systemDB) {
		tempDB = systemDB;
		this.setDocument();
		this.setKeyword();
		
		if (preview != null) {
			preview.commit(systemDB);
			this.put("preview", preview.getID());
		}
		
		//we don't want duplicates. The keyword ID, position, document and user define uniqueness
		if (!this.find(systemDB))
			super.commit(systemDB);
	}
}