/*
 */

package dockeywords;

import com.keywords.KeywordCollection;
import com.keywords.Processors;
import com.keywords.ProcessorsInfo;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.bson.types.ObjectId;

/**
 *
 * @author callumj
 */
public class DocProcessors implements Processors {

    String extension;

    public String getContentsOfFile(InputStream ioStream) {
        String words = "";

        if ("docx".equals(extension)) {
            try {

                XWPFDocument doc = new XWPFDocument(ioStream);

                XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

                words = extractor.getText();
            } catch (Exception ioErr) {

            }
        } else if ("doc".equals(extension)) {
            try {
                POIFSFileSystem fs = new POIFSFileSystem(ioStream);

                HWPFDocument doc = new HWPFDocument(fs);

                WordExtractor extractor = new WordExtractor(doc);


                words = extractor.getText();
            } catch (Exception ioErr2) {
                System.out.println("Failed processing Excel doc");
            }
        }

        return words;
    }

    public KeywordCollection processFile(ProcessorsInfo info) {
        extension = ((String) info.dbObject.get("item_type")).toLowerCase();
        String fileContents = getContentsOfFile(info.ioStream);
        try {
            info.ioStream.close();
        } catch (IOException ioErr) {

        }
        String[] words = fileContents.split(info.SPLITFORMAT);
        return KeywordCollection.create(words, info);
    }

    public String processorType() {
        return "doc|docx";
    }
}
