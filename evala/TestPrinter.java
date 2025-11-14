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

        // Create directory if missing
        if (!dir.exists()) dir.mkdirs();

        // Build full path
        String fullPath = dirPath + "/" + filename;

        try (PrintWriter out = new PrintWriter(fullPath)) {
            out.println("// Evala Generated Test Files");
            out.println("// Note: These tests may not be comprehensive.");
            for (TestCase tc : generated) { //generate the test file here 
                out.println(tc);
                }
            out.flush();

        }catch(Exception e){
            System.err.println("Failed to write grade file: "+ e);
        }
    }

}
