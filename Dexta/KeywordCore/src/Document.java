package com.dexta.coreservices.models;

import java.util.HashMap;
import org.bson.types.ObjectId;
import com.mongodb.DB;

public class Document extends DBAbstract {
	
	public HashMap<String,KeywordDocument> keywordList;
	public StringBuilder documentContents;
	
	public Document(User owner, String documentTitle, String resourceURI) {
		super();
		keywordList = new HashMap<String,KeywordDocument>();
		documentContents = new StringBuilder();
	}
	
	public void setUser(User owner) {
		this.put("user", owner.getID());
	}
	
	public User getUser(DB systemDB) {
		User lookup = new User();
		lookup.put("_id", this.getUserID());
		lookup.find(systemDB);
		return lookup;
	}
	
	public ObjectId getUserID() {
		return (ObjectId) this.get("user");
	}
	
	public void setDocumentTitle(String title) {
		this.put("title", title);
	}
	
	public String getDocumentTitle() {
		return (String) this.get("title");
	}
	
	public void setResourceURI(String resourceURI) {
		this.put("resource_uri", resourceURI);
	}
	
	public String getResourceURI() {
		return (String) this.get("resource_uri");
	}
	
	public void addDataToContent(String input) {
		documentContents.append(input);
	}
	
	
	public void buildKeywords(int seekAmount) {
		String[] words = documentContents.toString().split("\\s+");
		for (int i = 0; i < words.length; i++) {
			KeywordDocument insertion = new KeywordDocument(words[i], this, i);
            keywordList.put(insertion.getKeyword().toString(), insertion);

            String word = words[i];
            for (int xtra = 1; xtra < seekAmount; xtra++) {
                //increase key word space
                int incr = i + xtra;
                if (incr < words.length) {
                    word = word.concat(" " + words[incr]);
					KeywordDocument xtrainsertion = new KeywordDocument(word, this, i + xtra);
					keywordList.put(xtrainsertion.getKeyword().toString(), xtrainsertion);
                } else {
                    break;
                }
            }
        }
	}
}