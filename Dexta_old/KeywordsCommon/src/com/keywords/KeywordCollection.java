package com.keywords;

import java.util.HashMap;
import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import org.bson.types.ObjectId;
/**
 *
 * @author callumj
 */
public class KeywordCollection {

    public HashMap<String,ArrayList<Reference>> keywordStor;

    public KeywordCollection() {
        keywordStor = new HashMap<String,ArrayList<Reference>>();
    }

    public void addkeyWord(String keyword, Reference referenceObj) {
        String dCase = keyword.toLowerCase();
        dCase = dCase.replaceAll("[^a-zA-Z0-9 ]","");

        String spaceTest = dCase.replaceAll("\\s", "");

        if (keyword.length() >= 2 && spaceTest.length() != 0) {

            ArrayList<Reference> storObj = keywordStor.get(dCase);
            if (storObj == null)
                storObj = new ArrayList<Reference>();

            storObj.add(referenceObj);

            //push back into Map
            keywordStor.put(dCase, storObj);
        }
    }

    public void compact(int minimum) {
        ArrayList<String> toDelete = new ArrayList<String>();
        for (String key : keywordStor.keySet()) {
            ArrayList<Reference> refCollection = keywordStor.get(key);
            if (refCollection.size() < minimum)
                toDelete.add(key);
        }

        for (String key : toDelete) {
            keywordStor.remove(key);
        }
    }

    public void commit(DBCollection keywordCollection, DBCollection pointerCollection) {

        int count = 0;
        for (String key : keywordStor.keySet()) {

            count++;
            if (count % 100 == 0)
                System.out.println("Processed " + count);
            DBObject result = keywordCollection.findOne(new BasicDBObject("word", key));
            BasicDBObject insert = null;
            if (result == null) {
                insert = new BasicDBObject();
                insert.put("word", key);

                keywordCollection.save(insert);
            }

            ObjectId refID = null;
            if (insert != null)
                refID = (ObjectId) insert.get("_id");
            else
                refID = (ObjectId) result.get("_id");

            for (Reference item : keywordStor.get(key)) {
                BasicDBObject insertion = item.getDBObject();
                insertion.put("ref_id", refID);
                pointerCollection.save(insertion);
            }
        }
    }

    public static KeywordCollection create(String[] words, ProcessorsInfo infoObj) {
        KeywordCollection keywordStorage = new KeywordCollection();
            for (int i = 0; i < words.length; i++) {
                String normalWord = words[i].toLowerCase().replaceAll("\\W", "");
                normalWord = normalWord.trim();
                Reference ref = new Reference();
                ref.wordIndex = i;
                ref.documentRef = infoObj.documentRef;
                ref.userRef = infoObj.userRef;
                keywordStorage.addkeyWord(normalWord, ref);


                String word = words[i];
                for (int xtra = 1; xtra < 10; xtra++) {


                    //increase key word space
                    int incr = i + xtra;
                    if (incr < words.length) {


                        String xtraWord = words[incr].toLowerCase().replaceAll("\\W", "");
                        xtraWord = xtraWord.trim();
                        word = word.concat(" " + xtraWord);
                        Reference xtraRef = new Reference();
                        xtraRef.wordIndex = i;
                        xtraRef.documentRef = infoObj.documentRef;
                        xtraRef.userRef = infoObj.userRef;
                        keywordStorage.addkeyWord(word, xtraRef);
                    } else {
                        break;
                    }
                }
            }

            keywordStorage.compact(infoObj.compactSize);
            return keywordStorage;
    }
}
