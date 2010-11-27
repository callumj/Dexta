package com.dexta.coreservices.models.documents;

import com.dexta.coreservices.models.base.DBAbstract;
import com.dexta.coreservices.models.keywords.*;
import com.dexta.coreservices.models.users.User;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import org.bson.types.ObjectId;
import com.mongodb.DB;
import java.util.Calendar;

public class Document extends DBAbstract {
	
	public HashMap<String,ArrayList<KeywordDocument>> keywordList;
	public ArrayList<String> importants;
	public StringBuilder documentContents;
	
	public Document() {	}
	
	public Document(User owner, String documentTitle, String resourceURI) {
		super();
		keywordList = new HashMap<String,ArrayList<KeywordDocument>>();
		documentContents = new StringBuilder();
		importants = new ArrayList<String>();
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
			ArrayList<KeywordDocument> relatedWords = new ArrayList<KeywordDocument>();
			try {
				KeywordDocument insertion = new KeywordDocument(words[i], this);
				insertion.setPosition(i);
	            ArrayList<KeywordDocument> listContainer = keywordList.get(insertion.getKeyword().toString());
				if (listContainer == null)
					listContainer = new ArrayList<KeywordDocument>();
			
			
				listContainer.add(insertion);
				relatedWords.add(insertion);
				keywordList.put(insertion.getKeyword().toString(),listContainer);
				if (insertion.isImportant())
					importants.add(insertion.getKeyword().toString());
			} catch (Exception keywordErr) {
			}

            String word = words[i];
            for (int xtra = 1; xtra < seekAmount; xtra++) {
                //increase key word space
                int incr = i + xtra;
                if (incr < words.length) {
                    word = word.concat(" " + words[incr]);
					
					try {
						KeywordDocument insertion_xtra = new KeywordDocument(word, this);
						insertion_xtra.setPosition(i + xtra);
						ArrayList<KeywordDocument> listContainer_xtra = keywordList.get(insertion_xtra.getKeyword().toString());
						if (listContainer_xtra == null)
							listContainer_xtra = new ArrayList<KeywordDocument>();


						listContainer_xtra.add(insertion_xtra);
						relatedWords.add(insertion_xtra);
						keywordList.put(insertion_xtra.getKeyword().toString(),listContainer_xtra);
						if (insertion_xtra.isImportant() && !importants.contains(insertion_xtra.getKeyword().toString()))
							importants.add(insertion_xtra.getKeyword().toString());
					} catch (Exception keywordErr) {
					}
					
                } else {
                    break;
                }
            }
        
			//construct a preview
			for (KeywordDocument keyword : relatedWords) {
				keyword.setPreview(word);
			}
		}
	}
	
	public void commit(DB systemDB) {
		//set date added
		this.put("date_added", Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT")).getTimeInMillis() / 1000);
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
			if ((container == null || container.size() < compactSize) && !importants.contains(key))
				toDelete.add(key);
		}
		
		for (String key : toDelete) {
			keywordList.remove(key);
		}
	}
}