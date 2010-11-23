package com.dexta.coreservices.models.services;

import com.dexta.coreservices.models.base.DBAbstract;
import com.dexta.tools.DropboxTools;
import com.dexta.coreservices.models.documents.PendingDocument;

import com.mongodb.DB;
import com.dropbox.*;

import org.json.simple.JSONObject;

import org.apache.http.HttpResponse;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.IOException;
import oauth.signpost.exception.OAuthException;

public class Dropbox extends Service {
	public Dropbox(String name, String tokenKey, String tokenSecret) {
		super();
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
	
	public void setIgnorePaths(String paths) {
		this.put("ignore", paths);
	}
	
	public String[] getIgnorePath() {
		String ignore = (String) this.get("ignore");
		return ignore.split("[,]\\s+");
	}
	
	public long getLastCheck() {
		Long lastCheck =  (Long) this.get("last_check");
		if (lastCheck == null)
			lastCheck = 0l;
		return lastCheck;
	}
	
	public void addNewFiles(DB systemDB) throws IOException, OAuthException, DropboxException {
		//construct Dropbox client
		ServiceProvider dropboxSettings = new ServiceProvider("dropbox");
		if (dropboxSettings.find(systemDB)) {
	        HashMap dropboxSettingsMap = new HashMap();
	        dropboxSettingsMap.put("server", dropboxSettings.get("server"));
	        dropboxSettingsMap.put("content_server", dropboxSettings.get("content_server"));
	        dropboxSettingsMap.put("port", Long.parseLong((String) dropboxSettings.get("port")));
	        dropboxSettingsMap.put("request_token_url", dropboxSettings.get("request_token_url"));
	        dropboxSettingsMap.put("access_token_url", dropboxSettings.get("access_token_url"));
	        dropboxSettingsMap.put("authorization_url", dropboxSettings.get("authorization_url"));
	        dropboxSettingsMap.put("consumer_key", dropboxSettings.get("consumer_key"));
	        dropboxSettingsMap.put("consumer_secret", dropboxSettings.get("consumer_secret"));
		
			HashMap thisUserSettings = new HashMap(dropboxSettingsMap);
	        thisUserSettings.put("access_token_key", this.getTokenKey());
	        thisUserSettings.put("access_token_secret", this.getTokenSecret());

	        Authenticator authClient = new Authenticator(thisUserSettings);

	        DropboxClient dboxClient = new DropboxClient(thisUserSettings, authClient);
	
			ArrayList<JSONObject> updatedFiles = DropboxTools.getAllFilesForUser(dboxClient, new ArrayList<String>(Arrays.asList(this.getIgnorePath())), this.getLastCheck());
			
			for (JSONObject object : updatedFiles) {
				String path = (String) object.get("path");
				String fileName = path.substring(path.lastIndexOf("/") + 1);
				
				PendingDocument newDocument = new PendingDocument(fileName, this);
				
				//pull from Dropbox and insert into S3
				HttpResponse dboxGet = dboxClient.getFile("dropbox", path);
				InputStream dboxFile = dboxGet.getEntity().getContent();
				//create the S3 file
				AWSS3 insertFile = new AWSS3(dboxFile);
				insertFile.commit(systemDB);
				if (insertFile.getID() != null) {
					newDocument.setStorageReference(insertFile.getID());
					newDocument.commit(systemDB);
				}
			}
		}
	}
}