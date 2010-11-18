/*
 */

package txtkeywords;

import com.keywords.KeywordCollection;
import com.keywords.Processors;
import com.keywords.ProcessorsInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.bson.types.ObjectId;

/**
 *
 * @author callumj
 */
public class TxtProcessors implements Processors {

    public String getContentsOfFile(InputStream ioStream) {
        StringBuilder contents = new StringBuilder();

        try {

          BufferedReader input =  new BufferedReader(new InputStreamReader(ioStream));
          try {
            String line = null;

            while (( line = input.readLine()) != null){
              contents.append(line);
              contents.append(System.getProperty("line.separator"));
            }
          }
          finally {
            input.close();
          }
        }
        catch (IOException ex){
          ex.printStackTrace();
        }

        return contents.toString();
    }

    public KeywordCollection processFile(ProcessorsInfo info) {
        String fileContents = getContentsOfFile(info.ioStream);
        String[] words = fileContents.split(info.SPLITFORMAT);
        return KeywordCollection.create(words, info);
    }

    public String processorType() {
        return "txt";
    }
}
