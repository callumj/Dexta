/*
 */

package rtfkeywords;

import com.keywords.KeywordCollection;
import com.keywords.Processors;
import com.keywords.ProcessorsInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;
import org.bson.types.ObjectId;

/**
 *
 * @author callumj
 */
public class RtfProcessors implements Processors {

    public String getContentsOfFile(InputStream ioStream) {
        StringBuilder contents = new StringBuilder();

        try {

          RTFEditorKit rtfEditKit = new RTFEditorKit();
          Document doc = rtfEditKit.createDefaultDocument();

          rtfEditKit.read(ioStream, doc, 0);

          contents.append(doc.getText(0, doc.getLength()));

          ioStream.close();
        } catch (BadLocationException ex) {
          ex.printStackTrace();
        } catch (IOException ex){
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
        return "rtf";
    }
}
