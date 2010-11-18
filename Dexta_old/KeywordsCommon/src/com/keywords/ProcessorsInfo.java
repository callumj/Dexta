/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.keywords;

import com.mongodb.DBObject;
import java.io.InputStream;
import org.bson.types.ObjectId;

/**
 *
 * @author callumj
 */
public class ProcessorsInfo {
    
    public String SPLITFORMAT = "\\s+";
    public ObjectId userRef;
    public ObjectId documentRef;
    public InputStream ioStream;
    public int compactSize;

    public DBObject dbObject;
}
