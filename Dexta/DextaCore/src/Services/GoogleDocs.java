package com.dexta.coreservices.models.services;

import com.dexta.coreservices.models.base.DBAbstract;
import com.dexta.tools.GoogleDocsTools;
import com.dexta.tools.StorageWrapper;
import com.dexta.coreservices.models.documents.PendingDocument;
import com.dexta.coreservices.models.documents.Document;

import com.google.gdata.client.authn.oauth.*;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.MediaContent;
import com.google.gdata.util.ServiceException;

import com.mongodb.DB;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import java.io.IOException;
import java.net.URL;

public class GoogleDocs extends Service {
	public GoogleDocs() {
		
	}
	
	public GoogleDocs(Service importFrom) {
		super(importFrom);
	}
	
	public GoogleDocs(String name, String tokenKey, String tokenSecret) {
		super();
		this.setName(name);
		this.setTokenKey(tokenKey);
		this.setTokenSecret(tokenSecret);
	}
	
	public void setName(String name) {
		this.put("name", name);
	}
	
	public String getName() {
		return (String) this.get("name");
	}
	
	public void setTokenKey(String key) {
		this.put("token_key", key);
	}
	
	public String getTokenKey() {
		return (String) this.get("token_key");
	}
	
	public void setTokenSecret(String secret) {
		this.put("token_secret", secret);
	}
	
	public String getTokenSecret() {
		return (String) this.get("token_secret");
	}
	
	public long getLastCheck() {
		Long lastCheck =  (Long) this.get("last_check");
		if (lastCheck == null)
			lastCheck = 0l;
		return lastCheck;
	}
	
	public void setLastCheck(long time) {
		this.put("last_check", time);
	}
	
	public void addNewFiles(StorageWrapper wrapper) throws IOException, ServiceException, OAuthException, Exception {
		//construct Dropbox client
		DB systemDB = wrapper.mongoDatabase;
		ServiceProvider gDocsSettings = new ServiceProvider("google");
		if (gDocsSettings.find(systemDB)) {
			
			GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
			oauthParameters.setOAuthConsumerKey((String) gDocsSettings.get("consumer_key"));
			oauthParameters.setOAuthConsumerSecret((String) gDocsSettings.get("consumer_secret"));

			oauthParameters.setOAuthToken((String) this.getTokenKey());
			oauthParameters.setOAuthTokenSecret((String) this.getTokenSecret());
	
			DocsService client = new DocsService("Dexta-DocumentIndexer-v1");
			client.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());

			URL feedUrl = new URL("https://docs.google.com/feeds/default/private/full");
			DocumentListFeed resultFeed = client.getFeed(feedUrl, DocumentListFeed.class);
			for (DocumentListEntry entry : resultFeed.getEntries()) {
				MediaContent content = (MediaContent) entry.getContent();
				String extension = null;
				
				try {
					extension = GoogleDocsTools.determineExtension(entry);
				} catch (Exception typeErr) {
					
				}
				
				if (extension != null) {
					PendingDocument newDocument = new PendingDocument(entry.getTitle().getPlainText() + "." + extension, this);
					newDocument.setMimeType(content.getMimeType().getMediaType() + "/" + content.getMimeType().getSubType());
					newDocument.put("googledocs_resourceid", entry.getResourceId());
				
					//first check if the same document has not been processed and stored already
					Document testDocument = newDocument.toDocument();
					testDocument.put("_last_edited", entry.getEdited().getValue());
					System.out.println("Now testing for: " + newDocument.getfileName());
					if (!(testDocument.find(systemDB))) {
						//now proceed to check if a similar document is in the queue (and not being processed)
						newDocument.setLocked(false);
						boolean findResult = newDocument.find(systemDB); //if we find one but with an older revision, we can update it
						if (newDocument.get("_last_edited") == null || ((Long) newDocument.get("_last_edited")) < entry.getEdited().getValue()) {
							System.out.println("\tWill be processing: " + newDocument.getfileName());
							//handle if we need to replace a file currently sitting in queue
							if (findResult) {
								newDocument.setLocked(true); //lock it while we replace it's AWS S3 file
								newDocument.commit(systemDB);
								newDocument.setLocked(false);
							}
							
							byte[] rawData = null;
							try {
								rawData = GoogleDocsTools.downloadDocument(client, entry, extension);
							} catch (Exception genErr) {
								
							}
							if (rawData != null) {
								ByteArrayInputStream gDocsFile = new ByteArrayInputStream(rawData);
								//create the S3 file
								AWSS3 insertFile = new AWSS3(gDocsFile);
								insertFile.commit(wrapper);
								if (insertFile.getID() != null) {
									newDocument.put("_last_edited", entry.getEdited().getValue());
									newDocument.put("original_link", entry.getDocumentLink().getHref());
									newDocument.setStorageReference(insertFile.getID());
									newDocument.commit(systemDB);
								}
							}
						}
					}
				}
			}
		}
	}
}