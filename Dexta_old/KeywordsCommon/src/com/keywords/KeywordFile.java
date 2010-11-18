/*
 */

package com.keywords;

import com.mongodb.*;
import java.util.Calendar;
/**
 *
 * @author callumj
 */
public class KeywordFile {
    public String itemType;
    public static String COLLECTIONNAME = "documents";
    public static String PENDINGCOLLECTIONNAME = "pending_documents";
    public static String BUCKETNAME = "keyword_filestor";
    public org.bson.types.ObjectId parentID;
    public org.bson.types.ObjectId myID;
    public org.bson.types.ObjectId userID;
    public String filename;

    public void addToPending(DB thisDB) {
        DBCollection pendingCol = thisDB.getCollection(PENDINGCOLLECTIONNAME);
        BasicDBObject insertionObj = new BasicDBObject();
        insertionObj.put("document", myID);
        insertionObj.put("user", userID);
        insertionObj.put("locked", false);
        insertionObj.put("added", Calendar.getInstance().getTimeInMillis());
        insertionObj.put("item_type", itemType);
        pendingCol.save(insertionObj);
    }
}
