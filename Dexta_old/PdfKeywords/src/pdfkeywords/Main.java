/*
 */

package pdfkeywords;

import com.keywords.ProcessorsRunner;

/**
 *
 * @author callumj
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        PdfProcessors instanceObj = new PdfProcessors();
        ProcessorsRunner.run(instanceObj);
    }

}
