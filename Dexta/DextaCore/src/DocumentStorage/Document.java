package com.dexta.coreservices.models.documents;

import com.dexta.coreservices.models.base.DBAbstract;
import com.dexta.coreservices.models.keywords.*;
import com.dexta.coreservices.models.users.User;
import com.dexta.coreservices.models.users.UserService;
import com.dexta.coreservices.models.services.StorageService;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import org.bson.types.ObjectId;
import com.mongodb.DB;
import java.util.Calendar;

public class Document extends DBAbstract {
	
	public HashMap<String,ArrayList<KeywordDocument>> keywordList;
	public ArrayList<String> importants;
	public StringBuilder documentContents;
	
	public Document() {
		super();
		keywordList = new HashMap<String,ArrayList<KeywordDocument>>();
		documentContents = new StringBuilder();
		importants = new ArrayList<String>();
	}
	
	public void setServiceID(ObjectId id) {
		this.put("service", id);
	}
	
	public ObjectId getServiceID() {
		return (ObjectId) this.get("service");
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
	
	public void setStorageReference(ObjectId storageID) {
		this.put("storage", storageID);
	}
	
	public ObjectId getStorageReference() {
		return (ObjectId) this.get("storage");
	}
	
	public StorageService getStorage(DB systemDB) {
		StorageService returnObj = new StorageService();
		returnObj.put("_id", this.getStorageReference());
		if (returnObj.find(systemDB))
			return returnObj;
		else
			return null;
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
	
	public HashMap<String, Object> removeNonImportants() {
		HashMap<String, Object> removedItems = new HashMap<String, Object>();
		ArrayList<String> remove = new ArrayList<String>();
		for (String key : this.keySet()) {
			if (key.startsWith("_"))
				remove.add(key);
		}
		
		for (String key : remove) {
			removedItems.put(key, this.remove(key));
		}
		
		return removedItems;
	}
	
	public void commit(DB systemDB) {
		//set date added
		this.put("date_added", Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT")).getTimeInMillis() / 1000);
		super.commit(systemDB);
		List<UserService> sharedServices = UserService.getLinksForID(systemDB, getServiceID());
		//update keywords and commit
		for (String key : keywordList.keySet()) {
			ArrayList<KeywordDocument> container = keywordList.get(key);
			for (KeywordDocument linker : container) {
				for (UserService svc : sharedServices) {
					linker.setUser(svc.getUser());
					linker.commit(systemDB);
					linker.removeField("_id"); //pretend we are inserting a new one
				}
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