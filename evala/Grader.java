package evala;

import java.util.*;
import java.util.stream.Collectors;

public final class Grader {
  private final Scanner.CommentStats comments;
  private final UsageCollector.Usage usage;
  private final int ifWithoutElse;
  private final List<UsageCollector.MagicNumber> numericLiterals;
  public int gradeLocals;
  public int gradeParams; 
  public int ifTotal;

  
  //variables for calculating grade: 



  private static final Set<String> MAGIC_WHITELIST = Set.of("-1.0","0.0","1.0");

  public Grader(Scanner.CommentStats comments,
                UsageCollector.Usage usage,
                int ifWithoutElse,
                int ifTotal,
                List<UsageCollector.MagicNumber> numericLiterals) {
    this.comments = comments;
    this.usage = usage;
    this.ifWithoutElse = ifWithoutElse;
    this.ifTotal = ifTotal;
    this.numericLiterals = numericLiterals;
  }

  public GradeReport grade() {
    
    // Unused locals: writes - reads
    Set<String> unusedLocals = new HashSet<>(usage.writes);
    unusedLocals.removeAll(usage.reads);
    // System.out.println("Number of locals: "+usage.writes.size());
    // System.out.println("Number of unused locals "+unusedLocals.size());

    gradeLocals = Math.min (20 -(usage.writes.size() - unusedLocals.size()), 20); // not sure if the min is needed 
    //System.out.println("local Grade: "+gradeLocals); 
    

    // Unused params per function
    List<GradeReport.UnusedParam> unusedParams = new ArrayList<>();
    int funParamCount=0;
    for (var fn : usage.functions) {
      Set<String> diff = new HashSet<>(fn.params);
      funParamCount=  funParamCount + fn.params.size();
      
      diff.removeAll(fn.paramsRead);
      for (String p : diff) {
        unusedParams.add(new GradeReport.UnusedParam(fn.fnName, p));
      }
    }
    // System.out.println("Number of fun params: "+ funParamCount);
    // System.out.println("Number of unsed "+unusedParams.size());

    gradeParams = Math.min(20 - (funParamCount- unusedParams.size()), 20); 
   // System.out.println("Param Grade: "+gradeParams); 
    

    // Magic numbers: numeric literals not in whitelist and not obvious constants
    // Heuristic: ignore literals that are sole initializer of an ALL_CAPS variable (handled in UsageCollector?).
    // Here we simply filter by text whitelist.
    List<GradeReport.Magic> magic = numericLiterals.stream()
      .filter(m -> !MAGIC_WHITELIST.contains(m.lexeme))
      .map(m -> new GradeReport.Magic(m.lexeme, m.line))
      .collect(Collectors.toList());

    // Comment density verdict
    int commentLines = comments.singleLine + comments.blockLines;
    int total = Math.max(comments.totalLines, 1);
    double ratio = (double) commentLines / total;
    String verdict =
        (total >= 20 && ratio < 0.05) ? "Too few comments"
      : (ratio > 0.35)               ? "Too many comments"
                                     : "Good";
    

    var commentAnalysis = new GradeReport.CommentAnalysis(
      commentLines, comments.codeLines, comments.totalLines, ratio, verdict);

    return new GradeReport(ifWithoutElse, magic, unusedLocals, unusedParams, commentAnalysis,gradeLocals,gradeParams,ifTotal);
  }
}
