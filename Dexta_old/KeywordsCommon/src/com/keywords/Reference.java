package com.keywords;

import com.mongodb.BasicDBObject;
/**
 * Points to the reference of a specific keyword
 * @author callumj
 */
public class Reference {

    public int wordIndex;
    public org.bson.types.ObjectId documentRef;
    public org.bson.types.ObjectId userRef;

    public BasicDBObject getDBObject() {
        BasicDBObject returnObj = new BasicDBObject();
        returnObj.put("word_index", wordIndex);
        returnObj.put("document", documentRef);
        returnObj.put("user_ref", userRef);
        return returnObj;
    }
}
