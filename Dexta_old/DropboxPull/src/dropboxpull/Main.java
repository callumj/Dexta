/*
 */

package dropboxpull;

import com.dropbox.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import com.mongodb.*;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 *
 * @author callumj
 */
public class Main {

    public static ArrayList<String> allowedFileExts;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //process users
            Mongo m = new Mongo();
            DB db = m.getDB("keywords");
            DBCollection apiSettings = db.getCollection("apis");
            DBCollection servicesCol = db.getCollection("services");

            //Allocate settings for Dropbox
            DBObject dropboxSettings = apiSettings.findOne(new BasicDBObject("name", "dropbox"));
            HashMap dropboxSettingsMap = new HashMap();
            dropboxSettingsMap.put("server", dropboxSettings.get("server"));
            dropboxSettingsMap.put("content_server", dropboxSettings.get("content_server"));
            dropboxSettingsMap.put("port", Long.parseLong((String) dropboxSettings.get("port")));
            dropboxSettingsMap.put("request_token_url", dropboxSettings.get("request_token_url"));
            dropboxSettingsMap.put("access_token_url", dropboxSettings.get("access_token_url"));
            dropboxSettingsMap.put("authorization_url", dropboxSettings.get("authorization_url"));
            dropboxSettingsMap.put("consumer_key", dropboxSettings.get("consumer_key"));
            dropboxSettingsMap.put("consumer_secret", dropboxSettings.get("consumer_secret"));

            allowedFileExts = new ArrayList<String>();
            allowedFileExts.addAll(Arrays.asList(((String) dropboxSettings.get("extensions")).split(",")));

            //Setup AWS
            DBObject awsSettings = apiSettings.findOne(new BasicDBObject("name", "aws"));
            String awsKey = (String) awsSettings.get("consumer_key");
            String awsSecret = (String) awsSettings.get("consumer_secret");
            BasicAWSCredentials awsCred = new BasicAWSCredentials(awsKey, awsSecret);
            AmazonS3Client s3Client = new AmazonS3Client(awsCred);


            int limit = 100;
            int count = 0;
            BasicDBObject query = new BasicDBObject("type", "dropbox");
            DBCursor findCursor = servicesCol.find(query).limit(limit);
            List obj = findCursor.toArray();
            while (!obj.isEmpty()) {
                for (int i = 0; i < obj.size(); i++) {
                    DBObject item = (DBObject) obj.get(i);
                    String dropboxKey = (String) item.get("token_key");
                    String dropboxSecret = (String) item.get("token_secret");

                    HashMap thisUserSettings = new HashMap(dropboxSettingsMap);
                    thisUserSettings.put("access_token_key", dropboxKey);
                    thisUserSettings.put("access_token_secret", dropboxSecret);
                    org.bson.types.ObjectId thisID = (org.bson.types.ObjectId) item.get("_id");
                    org.bson.types.ObjectId userID = (org.bson.types.ObjectId) item.get("user");

                    Authenticator authClient = new Authenticator(thisUserSettings);

                    DropboxClient dboxClient = new DropboxClient(thisUserSettings, authClient);

                    //Setup ignore array
                    ArrayList<String> ignorePaths = new ArrayList<String>();
                    String splitIgnore = (String) item.get("ignore");
                    if (splitIgnore != null) {
                        for (String splitObj : splitIgnore.split(","))
                            ignorePaths.add(splitObj.toLowerCase());
                    }

                    ArrayList<DropboxFile> userFiles = getAllFilesForUser(dboxClient, ignorePaths);
                    for (DropboxFile file : userFiles) {
                        file.parentID = thisID; //link up association
                        file.userID = userID;
                        file.commitIfNeeded(s3Client, dboxClient, db);
                    }
                }
                count++;
                obj = servicesCol.find(query).skip(limit * count).limit(limit).toArray();
            }
        } catch (Exception genErr) {
            System.out.println("Error " + genErr);
        }
    }

    public static ArrayList<DropboxFile> getAllFilesForUser(DropboxClient dboxClient, ArrayList<String> ignoreList) {
            ArrayList<DropboxFile> fileList = getFiles("/", dboxClient, ignoreList);
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

    public static DropboxFile handleFile(JSONObject jsonObj) {
        if ((Boolean)jsonObj.get("is_dir"))
            return null;
        
        DropboxFile returnFile = new DropboxFile();

        returnFile.path = (String) jsonObj.get("path");
        returnFile.filename = returnFile.path.substring(returnFile.path.lastIndexOf("/") + 1);
        if ((returnFile.filename.lastIndexOf(".") + 1) < returnFile.filename.length())
            returnFile.itemType = returnFile.filename.substring(returnFile.filename.lastIndexOf(".") + 1);

        if (!allowedFileExts.contains(returnFile.itemType))
            return null;

        returnFile.size = (Long) jsonObj.get("bytes");
        returnFile.mimeType = (String) jsonObj.get("mime_type");
        returnFile.revision = (Long) jsonObj.get("revision");
        try {
            DateFormat dFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
            returnFile.modified = dFormat.parse((String) jsonObj.get("modified"));
        } catch (ParseException dateErr) {
            System.out.println(dateErr);
        }
        return returnFile;
    }

    public static ArrayList<DropboxFile> getFiles(String path, DropboxClient dbClient, ArrayList<String> ignoreList) {
       ArrayList<DropboxFile> fileArray = new ArrayList<DropboxFile>();
       System.out.println("Processing " + path);
       if (!ignoreList.contains(path.toLowerCase())) {
           try {
               JSONArray contentsArray = getJSONArrayForDropboxPath(dbClient, path);
               for (Object item : contentsArray) {
                    JSONObject jsonItem = (JSONObject) item;
                    String innerPath = (String) jsonItem.get("path");
                    Boolean isDir = (Boolean) jsonItem.get("is_dir");

                    if (!isDir) {
                        DropboxFile processedFile = handleFile(jsonItem);
                        if (processedFile != null)
                           fileArray.add(processedFile);
                    } else
                        fileArray.addAll(getFiles(innerPath, dbClient, ignoreList));
                }
           } catch (Exception ioErr) {
               System.out.println(ioErr);
           }
        }

       return fileArray;
    }

}
