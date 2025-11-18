package evala;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;

public final class GradeReport {

  public static final class Magic {
    public final String lexeme;
    public final int line;
    public Magic(String lexeme, int line) { this.lexeme = lexeme; this.line = line; }
  }
  public static final class UnusedParam {
    public final String functionName;
    public final String paramName;
    public UnusedParam(String fn, String p) { this.functionName = fn; this.paramName = p; }
  }
  public static final class CommentAnalysis {
    public final int commentLines, codeLines, totalLines;
    public final double ratio;
    public final String verdict;
    public CommentAnalysis(int cl, int code, int tot, double r, String v) {
      this.commentLines = cl; this.codeLines = code; this.totalLines = tot;
      this.ratio = r; this.verdict = v;
    }
  }

  private final int ifWithoutElse;
  private final List<Magic> magicNumbers;
  private final Set<String> unusedLocals;
  private final List<UnusedParam> unusedParams;
  private final CommentAnalysis comments;
  private final double gradeLocals;
  private final double gradeParams;
  private final int ifTotal;

  public GradeReport(int ifWithoutElse,
                     List<Magic> magicNumbers,
                     Set<String> unusedLocals,
                     List<UnusedParam> unusedParams,
                     CommentAnalysis comments, double gradeLocals, double gradeParams,int ifTotal) {
        this.ifWithoutElse = ifWithoutElse;
        this.magicNumbers = magicNumbers;
        this.unusedLocals = unusedLocals;
        this.unusedParams = unusedParams;
        this.comments = comments;
        this.gradeLocals = gradeLocals;
        this.gradeParams = gradeParams;
        this.ifTotal = ifTotal;
    }
    /**This is the formatted printer for the grade! */
  public String summaryLine() {
    // return String.format(
    //   "Grade: if-no-else=%d, magic=%d, unused-locals=%d, unused-params=%d, comments=%.1f%% (%s)",
    //   ifWithoutElse, magicNumbers.size(), unusedLocals.size(), unusedParams.size(),
    //   comments.ratio * 100.0, comments.verdict);
    //return "Grade written to CodeReview/GradedCode.md, Total Grade: ";
    return "";
  }
  /**Write to a file w/ the grades given */
  public void writeToFile(String filename) {
        String dirPath = "CodeReview";
        java.io.File dir = new java.io.File(dirPath);

        // Create directory if missing
        if (!dir.exists()) dir.mkdirs();

        // Build full path
        String fullPath = dirPath + "/" + filename+".md";
    try (PrintWriter out = new PrintWriter(fullPath)) {
      out.println("# Evala static grading\n");

     // System.out.println("if total "+ (ifTotal - ifWithoutElse));
      int ifCalc=0;
      // if( (ifTotal - ifWithoutElse) / ifTotal == 0){
      //   ifCalc = 20.0;
      // }else{
      //   ifCalc= ifTotal- ifWithoutElse;
      // }
      // TODO: fix 
      double gradeIfs = 20.0 *  (ifTotal - ifWithoutElse) / ifTotal; // Math.max( 20.0 -ifCalc,0);
      out.println("// "+gradeIfs+"/20.0");
      out.printf("If without else: %d%n%n", ifWithoutElse);

      double magicGrade= Math.max( 20.0 - magicNumbers.size(),0);
      out.println("// "+magicGrade+"/20.0");

      out.printf("Magic numbers: %d%n", magicNumbers.size());
      for (var m : magicNumbers) {
        if (m.line >= 0) out.printf("  line %d: %s%n", m.line, m.lexeme);
        else out.printf("  %s%n", m.lexeme);
      }
      out.println();
      out.println("// "+ gradeLocals+"/20.0");
      out.printf("Unused locals: %d%n", unusedLocals.size());
      for (String n : unusedLocals) out.printf("  %s%n", n);
      out.println();


      out.println("// "+gradeParams+"/20.0");
      out.printf("Unused parameters: %d%n", unusedParams.size());
      for (var up : unusedParams) out.printf("  function %s: %s%n", up.functionName, up.paramName);
      out.println();

      double commentGrade = Math.max(0, (20 - Math.abs(50 - (comments.ratio *100.0))));
      DecimalFormat df  = new DecimalFormat("#.##");
      String formatted = df.format(commentGrade);

      out.println("// "+ formatted+"/20.0");
      out.println("Comment density:");
      out.printf("  total lines: %d%n", comments.totalLines);
      out.printf("  code lines:  %d%n", comments.codeLines);
      out.printf("  comment lines: %d%n", comments.commentLines);
      out.printf("  ratio: %.1f%%%n", comments.ratio * 100.0);
      out.printf("  verdict: %s%n", comments.verdict);
      double finalGrade = gradeIfs+gradeLocals+gradeParams+magicGrade+commentGrade;
      String endGrade = df.format(finalGrade);
      out.println("\n # Total Grade: "+endGrade+"/100.0");
     
      System.out.println("Grade written to: CodeReview/GradedCode.md \nTotal Grade: "+endGrade+"/100.0");
       System.out.println("\n-------------------------");
      System.out.println("Code Execution output ...");
      
      out.flush();
    } catch (Exception e) {
      System.err.println("Failed to write grade file: " + e);
    }
  }
}
