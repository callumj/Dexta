/*
 */

package txtkeywords;

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
        TxtProcessors instanceObj = new TxtProcessors();
        ProcessorsRunner.run(instanceObj);
    }

}