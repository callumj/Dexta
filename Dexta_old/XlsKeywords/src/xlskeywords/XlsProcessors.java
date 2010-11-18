/*
 */

package xlskeywords;

import com.keywords.KeywordCollection;
import com.keywords.Processors;
import com.keywords.ProcessorsInfo;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;

/**
 *
 * @author callumj
 */
public class XlsProcessors implements Processors {

    String extension;

    public String getContentsOfFile(InputStream ioStream) {
        String words = "";

        if ("xlsx".equals(extension)) {
            try {
                XSSFWorkbook doc = new XSSFWorkbook(ioStream);
                XSSFExcelExtractor extractor = new XSSFExcelExtractor(doc);

                extractor.setFormulasNotResults(true);
                extractor.setIncludeSheetNames(false);

                words = extractor.getText();
            } catch (Exception ioErr) {

            }
        } else if ("xls".equals(extension)) {
            try {
                POIFSFileSystem fs = new POIFSFileSystem(ioStream);

                HSSFWorkbook hsWorkBook = new HSSFWorkbook(fs);
                ExcelExtractor extractor = new ExcelExtractor(hsWorkBook);

                extractor.setFormulasNotResults(true);
                extractor.setIncludeSheetNames(false);


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
        return "xls|xlsx";
    }
}
