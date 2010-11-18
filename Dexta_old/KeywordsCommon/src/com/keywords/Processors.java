/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.keywords;

import java.io.InputStream;
import org.bson.types.ObjectId;

/**
 *
 * @author callumj
 */
public interface Processors {

    public String getContentsOfFile(InputStream ioStream);
    public KeywordCollection processFile(ProcessorsInfo info);
    public String processorType();

}
