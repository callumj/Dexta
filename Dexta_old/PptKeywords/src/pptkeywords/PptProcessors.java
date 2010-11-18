/*
 */

package pptkeywords;

import com.keywords.KeywordCollection;
import com.keywords.Processors;
import com.keywords.ProcessorsInfo;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hslf.extractor.QuickButCruddyTextExtractor;

/**
 *
 * @author callumj
 */
public class PptProcessors implements Processors {

    public String getContentsOfFile(InputStream ioStream) {
        StringBuilder contents = new StringBuilder();

        try {

          QuickButCruddyTextExtractor quickExtractor = new QuickButCruddyTextExtractor(ioStream);

          contents.append(quickExtractor.getTextAsString());

          ioStream.close();
        }
        catch (Exception ex){
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
        return "ppt";
    }
}
