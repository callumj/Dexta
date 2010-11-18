/*
 */

package pptkeywords;

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
        PptProcessors instanceObj = new PptProcessors();
        ProcessorsRunner.run(instanceObj);
    }

}
