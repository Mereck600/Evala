package evala;
import java.io.PrintWriter;
import java.util.*;

public class TestPrinter {
    List<TestCase> generated = null;
    
    public TestPrinter(List<TestCase> generated){
        this.generated = generated;
    }
    public void writeToFile(String filename){
        String dirPath = "CodeReview";
        java.io.File dir = new java.io.File(dirPath);
        filename = filename +".eva";
        // Create directory if missing
        if (!dir.exists()) dir.mkdirs();

        // Build full path
        String fullPath = dirPath + "/" + filename;

        try (PrintWriter out = new PrintWriter(fullPath)) {
             out.println("// =======================================================");
            out.println("//            Evala Generated Test Files");
            out.println("//     Note: These tests may not be comprehensive.");
            out.println("//   Usage: Fill in expectedOutput and run the file.");
            out.println("// ========================================================");
            out.println("\n");

            for (TestCase tc : generated) { //generate the test file here 
                //have now: new TestCase(("add", 100.0, 100.0, nil)
                //new TestCase(("add", 100.0, 100.0, nil) expectedOutput)
                String res = tc.toString() +", expectedOutput )";
                out.println(res);
                out.println("\n");
                }
                
            out.flush();
            System.out.println("Test cases generated to: "+ fullPath);

        }catch(Exception e){
            System.err.println("Failed to write grade file: "+ e);
        }
    }

}
