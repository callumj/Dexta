package com.dexta.tools;

import java.io.UnsupportedEncodingException; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import com.dropbox.*;

public class DropboxTools {
	
	public static DateFormat dropboxParser = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
	
	public static ArrayList<JSONObject> getAllFilesForUser(DropboxClient dboxClient, ArrayList<String> ignoreList, Long olderThan) {
            ArrayList<JSONObject> fileList = getFiles("/", dboxClient, ignoreList, olderThan);
            System.out.println("Finished processing");
            return fileList;
    }

    public static JSONArray getJSONArrayForDropboxPath(DropboxClient dbClient, String path) throws Exception {
            Map response = dbClient.metadata("dropbox", path, 0, null, true, true, null);
            JSONObject responseBody = (JSONObject)response.get("body");

            if (responseBody.get("contents") == null)
                throw new Exception("Request returned erro");

            return (JSONArray) responseBody.get("contents");
    }
	
	public static ArrayList<JSONObject> getFiles(String path, DropboxClient dbClient, ArrayList<String> ignoreList, Long olderThan) {
	   ArrayList<JSONObject> fileArray = new ArrayList<JSONObject>();
	   System.out.println("Processing " + path);
	   if (!ignoreList.contains(path.toLowerCase())) {
	       try {
	           JSONArray contentsArray = getJSONArrayForDropboxPath(dbClient, path);
	           for (Object item : contentsArray) {
	                JSONObject jsonItem = (JSONObject) item;
	                String innerPath = (String) jsonItem.get("path");
	                Boolean isDir = (Boolean) jsonItem.get("is_dir");

	                if (!isDir) {
						Date modifiedDate = dropboxParser.parse((String) jsonItem.get("modified"));
						if ((modifiedDate.getTime() / 1000) >=  olderThan)
	                    	fileArray.add(jsonItem);
	                } else
	                    fileArray.addAll(getFiles(innerPath, dbClient, ignoreList, olderThan));
	            }
	       } catch (Exception ioErr) {
	           System.out.println(ioErr);
	       }
	    }

	   return fileArray;
	}
}