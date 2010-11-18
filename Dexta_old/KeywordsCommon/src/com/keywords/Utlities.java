/*
 */

package com.keywords;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author callumj
 */
public class Utlities {

    public static File writeToFile(InputStream ioStream, String fileName) throws IOException {
    //write the inputStream to a FileOutputStream
        File tmpWrite = new File(fileName);
        OutputStream out = new FileOutputStream(tmpWrite);

        int read=0;
        byte[] bytes = new byte[1024];

        while((read = ioStream.read(bytes))!= -1){
                out.write(bytes, 0, read);
        }

        ioStream.close();
        out.flush();
        out.close();

        return tmpWrite;
}
}
