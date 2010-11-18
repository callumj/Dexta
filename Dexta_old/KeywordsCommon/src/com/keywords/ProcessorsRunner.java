/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.keywords;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.keywords.KeywordCollection;
import com.keywords.KeywordFile;
import com.keywords.Utlities;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Pattern;
import org.bson.types.ObjectId;

/**
 *
 * @author callumj
 */
public class ProcessorsRunner {

    /**
     * @param args the command line arguments
     */
    public static void run(Processors obj) throws Exception {
        Mongo m = new Mongo();
        DB db = m.getDB("keywords");
        DBCollection apiSettings = db.getCollection("apis");
        DBCollection localSettings = db.getCollection("settings");
        //setup AWS
        DBObject awsSettings = apiSettings.findOne(new BasicDBObject("name", "aws"));
        String awsKey = (String) awsSettings.get("consumer_key");
        String awsSecret = (String) awsSettings.get("consumer_secret");
        BasicAWSCredentials awsCred = new BasicAWSCredentials(awsKey, awsSecret);
        AmazonS3Client s3Client = new AmazonS3Client(awsCred);

        //Setup base settings
        DBObject baseSettings = localSettings.findOne(new BasicDBObject("name", "base"));
        Double compactSize = (Double) baseSettings.get("compact_size");
        DBCollection pendingQueue = db.getCollection(KeywordFile.PENDINGCOLLECTIONNAME);
        DBCollection documentCol = db.getCollection(KeywordFile.COLLECTIONNAME);
        DBCollection keywordIndexCol = db.getCollection((String) baseSettings.get("keywords_collection"));
        DBCollection documentKeywordsCol = db.getCollection((String) baseSettings.get("documentkeywords_map"));

        //query build
        BasicDBObject query = new BasicDBObject();
        Pattern regexSearch = Pattern.compile("(" + obj.processorType() + ")", Pattern.CASE_INSENSITIVE);
        query.append("item_type", regexSearch);
        query.append("locked", false);

        while (true) {
            DBObject findObject = pendingQueue.findOne(query);
            if (findObject == null) {
                System.out.println("Pausing for 10 seconds, nothing to do.");
                Thread.sleep(10000); //don't thrash the system
            } else {

                findObject.put("locked", true); //place a lock
                pendingQueue.save(findObject);

                BasicDBObject queryForRelatedDoc = new BasicDBObject();
                queryForRelatedDoc.append("_id", findObject.get("document"));

                DBObject document = documentCol.findOne(queryForRelatedDoc);
                if (document == null)
                    System.out.println("Pending queue (" + findObject.get("_id") + ") points no where");
                else {
                    //get userInfo


                    //fetch AWS object
                    String objectKey = (String) document.get("aws_key");
                    ObjectId docID = (ObjectId) document.get("_id");
                    S3Object fetchObject = s3Client.getObject(KeywordFile.BUCKETNAME, objectKey);
                    InputStream contentsStream = fetchObject.getObjectContent();
                    File tmpFile = Utlities.writeToFile(contentsStream, "/tmp/txtprocess" + objectKey);
                    contentsStream.close();

                    ProcessorsInfo infoObject = new ProcessorsInfo();
                    infoObject.compactSize = compactSize.intValue();
                    infoObject.ioStream = new FileInputStream(tmpFile);
                    infoObject.userRef = (ObjectId) findObject.get("user");
                    infoObject.documentRef = docID;
                    infoObject.dbObject = findObject;

                    KeywordCollection keywordIndex = obj.processFile(infoObject);

                    if (keywordIndex != null) {
                        keywordIndex.commit(keywordIndexCol, documentKeywordsCol);
                        pendingQueue.remove(findObject);
                        System.out.println("Finished with " + docID);
                    } else {
                        findObject.put("error", "INDEX_NULL");
                        pendingQueue.save(findObject);
                    }
                    tmpFile.delete();
                }
            }
        }
    }

}
