package com.dexta.coreservices.models;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import org.bson.types.ObjectId;
import com.mongodb.DB;

public class Document extends DBAbstract {
	
	public HashMap<String,ArrayList<KeywordDocument>> keywordList;
	public StringBuilder documentContents;
	
	public Document(User owner, String documentTitle, String resourceURI) {
		super();
		keywordList = new HashMap<String,ArrayList<KeywordDocument>>();
		documentContents = new StringBuilder();
		this.setUser(owner);
		this.setDocumentTitle(documentTitle);
		this.setResourceURI(resourceURI);
	}
	
	public void setUser(User owner) {
		this.setUserID(owner.getID());
	}
	
	public User getUser(DB systemDB) {
		User lookup = new User();
		lookup.put("_id", this.getUserID());
		lookup.find(systemDB);
		return lookup;
	}
	
	public void setUserID(ObjectId id) {
		this.put("user", id);
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
			try {
				KeywordDocument insertion = new KeywordDocument(words[i], this, i);
	            ArrayList<KeywordDocument> listContainer = keywordList.get(insertion.getKeyword().toString());
				if (listContainer == null)
					listContainer = new ArrayList<KeywordDocument>();
			
			
				listContainer.add(insertion);
				keywordList.put(insertion.getKeyword().toString(),listContainer);
			} catch (Exception keywordErr) {
			}

            String word = words[i];
            for (int xtra = 1; xtra < seekAmount; xtra++) {
                //increase key word space
                int incr = i + xtra;
                if (incr < words.length) {
                    word = word.concat(" " + words[incr]);
					
					try {
						KeywordDocument insertion_xtra = new KeywordDocument(word, this, i + xtra);
					
						ArrayList<KeywordDocument> listContainer_xtra = keywordList.get(insertion_xtra.getKeyword().toString());
						if (listContainer_xtra == null)
							listContainer_xtra = new ArrayList<KeywordDocument>();


						listContainer_xtra.add(insertion_xtra);
						keywordList.put(insertion_xtra.getKeyword().toString(),listContainer_xtra);
					} catch (Exception keywordErr) {
					}
					
                } else {
                    break;
                }
            }
        }
	}
	
	public void commit(DB systemDB) {
		super.commit(systemDB);
		//update keywords and commit
		for (String key : keywordList.keySet()) {
			ArrayList<KeywordDocument> container = keywordList.get(key);
			for (KeywordDocument linker : container) {
				linker.commit(systemDB);
			}
		}
	}
	
	public void compact(int compactSize) {
		ArrayList<String> toDelete = new ArrayList<String>();
		for (String key : keywordList.keySet()) {
			ArrayList<KeywordDocument> container = keywordList.get(key);
			if (container == null || container.size() < compactSize)
				toDelete.add(key);
		}
		
		for (String key : toDelete) {
			keywordList.remove(key);
		}
	}
}