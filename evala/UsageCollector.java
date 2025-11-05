package evala;

import java.util.*;

import evala.Expr.Logical;
import evala.Stmt.Break;
/** Walks the AST to collect:
 * - variable declarations & reads
 * - per-function param reads
 * - count of if-without-else
 * - numeric literals (magic-number candidates)
 */
public final class UsageCollector implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

  // Names written (declared/assigned) vs read
  private final Set<String> writes = new HashSet<>();
  private final Set<String> reads  = new HashSet<>();
  // Track when we're visiting a var or assignment initializer this fixes magic number issue
    private boolean inAssignmentInitializer = false;


  // Function -> params and paramsRead
  public static final class FnUsage {
    final String fnName;
    final Set<String> params = new HashSet<>();
    final Set<String> paramsRead = new HashSet<>();
    FnUsage(String name) { 
        if(name==null){
            this.fnName="<anonymous>";
        }else{this.fnName=name;} 
    }
  }
  private final Deque<FnUsage> fnStack = new ArrayDeque<>();
  private final List<FnUsage> fnList = new ArrayList<>();

  // Structural/style metrics
  private int ifWithoutElse = 0;
  private final List<MagicNumber> magicNumbers = new ArrayList<>();

  // literal token line reporting
  public static final class MagicNumber {
    public final String lexeme;
    public final int line;
    MagicNumber(String lexeme, int line) { this.lexeme = lexeme; this.line = line; }
  }

  //Walk through statements if not null
  public void walk(List<Stmt> program) {
    for (Stmt s : program) if (s != null) s.accept(this);
  }

  public Usage getUsage() {
    return new Usage(reads, writes, fnList);
  }

  public int getIfWithoutElse() { return ifWithoutElse; }
  public List<MagicNumber> getMagicNumbers() { return magicNumbers; }

  public static final class Usage {
    public final Set<String> reads;
    public final Set<String> writes;
    public final List<FnUsage> functions;
    Usage(Set<String> r, Set<String> w, List<FnUsage> fns) {
      this.reads = Collections.unmodifiableSet(new HashSet<>(r)); //makes an unmutable view object
      this.writes = Collections.unmodifiableSet(new HashSet<>(w));
      this.functions = Collections.unmodifiableList(new ArrayList<>(fns));
    }
  }

  // ---------------- Stmt visitors ----------------
  @Override public Void visitVarStmt(Stmt.Var stmt) {
    writes.add(stmt.name.lexeme);
    if (stmt.initializer != null){
        boolean prev = inAssignmentInitializer;
        inAssignmentInitializer=true;
        stmt.initializer.accept(this);
        inAssignmentInitializer=prev;
    } 
    return null;
  }

  @Override public Void visitExpressionStmt(Stmt.Expression stmt) {
    stmt.expression.accept(this);
    return null;
  }

  @Override public Void visitPrintStmt(Stmt.Print stmt) {
    stmt.expression.accept(this);
    return null;
  }

  @Override public Void visitBlockStmt(Stmt.Block stmt) {
    for (Stmt s : stmt.statements) s.accept(this);
    return null;
  }

  @Override public Void visitIfStmt(Stmt.If stmt) {
    if (stmt.elseBranch == null) ifWithoutElse++;
    stmt.condition.accept(this);
    stmt.thenBranch.accept(this);
    if (stmt.elseBranch != null) stmt.elseBranch.accept(this);
    return null;
  }

  @Override public Void visitWhileStmt(Stmt.While stmt) {
    stmt.condition.accept(this);
    stmt.body.accept(this);
    return null;
  }

  @Override public Void visitFunctionStmt(Stmt.Function stmt) {
    FnUsage fu = new FnUsage(stmt.name == null ? null : stmt.name.lexeme);
    for (Token p : stmt.params) fu.params.add(p.lexeme);
    fnStack.push(fu);
    for (Stmt s : stmt.body) s.accept(this);
    fnStack.pop();
    fnList.add(fu);
    return null;
  }

  @Override public Void visitReturnStmt(Stmt.Return stmt) {
    if (stmt.value != null) stmt.value.accept(this);
    return null;
  }

  // ---------------- Expr visitors ----------------
  @Override public Void visitAssignExpr(Expr.Assign expr) {
    writes.add(expr.name.lexeme);
    boolean prev = inAssignmentInitializer;
    inAssignmentInitializer=true;
    expr.value.accept(this);
    inAssignmentInitializer=prev;
    return null;
  }

  @Override public Void visitVariableExpr(Expr.Variable expr) {
    reads.add(expr.name.lexeme);
    // If inside a function, mark param-read if applicable
    if (!fnStack.isEmpty() && fnStack.peek().params.contains(expr.name.lexeme)) {
      fnStack.peek().paramsRead.add(expr.name.lexeme);
    }
    return null;
  }

  @Override public Void visitBinaryExpr(Expr.Binary expr) {
    expr.left.accept(this); expr.right.accept(this);
    return null;
  }

  @Override public Void visitUnaryExpr(Expr.Unary expr) {
    expr.right.accept(this);
    return null;
  }

  @Override public Void visitGroupingExpr(Expr.Grouping expr) {
    expr.expression.accept(this);
    return null;
  }

  @Override public Void visitCallExpr(Expr.Call expr) {
    expr.callee.accept(this);
    for (Expr a : expr.arguments) a.accept(this);
    return null;
  }


@Override public Void visitLiteralExpr(Expr.Literal expr) {
  if (expr.value instanceof Double && !inAssignmentInitializer) {
    // No token available, so we can't report a line. Use -1.
    magicNumbers.add(new MagicNumber(String.valueOf(expr.value), -1));
  }
  return null;
}


  @Override
  public Void visitBreakStmt(Break stmt) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitBreakStmt'");
  }

  @Override
  public Void visitLogicalExpr(Logical expr) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitLogicalExpr'");
  }
}
