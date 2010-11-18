/*
 */

package pdfkeywords;

import com.keywords.KeywordCollection;
import com.keywords.Processors;
import com.keywords.ProcessorsInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.bson.types.ObjectId;

import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 *
 * @author callumj
 */
public class PdfProcessors implements Processors {
    
    public String getContentsOfFile(InputStream ioStream) {
        try {
            PDDocument pDocument = PDDocument.load(ioStream);

            StringWriter stringWriter = new StringWriter();

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(Integer.MAX_VALUE);
            stripper.writeText(pDocument, stringWriter);

            pDocument.close();

            return stringWriter.toString();
        } catch (IOException ioErr) {
            return "";
        }
    }

    public KeywordCollection processFile(ProcessorsInfo info) {
        String fileContents = this.getContentsOfFile(info.ioStream);
        try {
            info.ioStream.close();
        } catch (IOException ioErr) {
            
        }
        String[] words = fileContents.split(info.SPLITFORMAT);
        return KeywordCollection.create(words, info);
    }

    public String processorType() {
            return "pdf";
    }
}
