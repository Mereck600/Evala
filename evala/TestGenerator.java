package evala;

import evala.Expr.Assign;
import evala.Expr.Binary;
import evala.Expr.Call;
import evala.Expr.Grouping;
import evala.Expr.Literal;
import evala.Expr.Logical;
import evala.Expr.Unary;
import evala.Expr.Variable;
import evala.Stmt.Block;
import evala.Stmt.Break;
import evala.Stmt.Expression;
import evala.Stmt.Function;
import evala.Stmt.If;
import evala.Stmt.Print;
import evala.Stmt.Return;
import evala.Stmt.Var;
import evala.Stmt.While;
import java.util.*;




interface TestVariation {            // context
    /**
     * Representative concrete values for this variation. The generator will
     * combine these across parameters to build test cases. A value of null
     * denotes a "nil" / omitted argument.
     */
    List<Object> representatives();
}

class NoInfoVar implements TestVariation {

    @Override public List<Object> representatives() { return Arrays.asList((Object) "nil"); }
}

class PosNegZeroVar implements TestVariation {
    int x = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000001);
    int y = java.util.concurrent.ThreadLocalRandom.current().nextInt(1000001);

    @Override public List<Object> representatives() { return Arrays.asList((Object) x, 0.0, -y); }
}

class BooleanVar implements TestVariation {
    
    @Override public List<Object> representatives(){return Arrays.asList((Object)true,false);}
}


class StringVar implements TestVariation {

    @Override
    public List<Object> representatives() {
        return Arrays.asList(
            (Object) "",                     // empty
            (Object) "a",                    // single char
            (Object) "hello",                // simple word
            (Object) "hello world",          // with space
            (Object) "123",                  // numeric-looking
            (Object) "spécial çhars ✓"       // unicode-ish
        );
    }
}


class TestCase {
    final String functionName;
    final List<Object> args;
    final Object expected;
    public int index;

    // Constructor used from native TestCase(...) callable
    TestCase(String functionName, List<Object> args, Object expected) {
        this.functionName = functionName;
        this.args = (args == null) ? Collections.emptyList() : new ArrayList<>(args);
        this.expected = expected;
    }


    // new TestCase("add", 100.0, 0.0, null, 100.0)
    TestCase(String functionName,int index, Object... argsAndExpected) {
        this.functionName = functionName;
        this.index=index;

        if (argsAndExpected == null || argsAndExpected.length == 0) {
            this.args = Collections.emptyList();
            this.expected = null;
        } else {
            int n = argsAndExpected.length;
            this.expected = argsAndExpected[n - 1];

            Object[] argArray = Arrays.copyOf(argsAndExpected, n - 1);
            this.args = Arrays.asList(argArray);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String exp = expected.toString();
       
        sb.append("var t").append(index).append(" = ");

        sb.append("TestCase(").append("\"").append(functionName).append("\"");
       // sb.append(", args=").append(args);
        sb.append(", ").append(exp.substring(1,exp.length()-1 ));
        
        return sb.toString();
    }
}




public class TestGenerator implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    Map<String, TestVariation> varCases = new HashMap<>();
    TestVariation curContext = new NoInfoVar();


    public TestGenerator(List<Token> params) {
        for (Token tok : params) {
            varCases.put(tok.lexeme, curContext);
        }
    }

    /**
     * Build test cases for the given function name and parameter list using
     * the variation information collected in {@link #varCases}.
     */
    public List<TestCase> generateTestCases(String functionName,int index, List<Token> params) {
        // For each parameter, obtain the representative values
        List<List<Object>> domain = new ArrayList<>();
        for (Token p : params) { 
            TestVariation tv = varCases.get(p.lexeme);
            if (tv == null) tv = new NoInfoVar();
            List<Object> reps = tv.representatives();
            // ensure non-empty domain
            if (reps == null || reps.isEmpty()) reps = Arrays.asList((Object) null);
            domain.add(reps);
        }

        // Cartesian product over domains
        List<List<Object>> combos = cartesianProduct(domain);
        List<TestCase> out = new ArrayList<>();
        for (List<Object> c : combos) out.add(new TestCase(functionName,index++, c));
        return out;
    }

    private static List<List<Object>> cartesianProduct(List<List<Object>> lists) {
        List<List<Object>> result = new ArrayList<>();
        if(lists == null || lists.isEmpty()){result.add(new ArrayList<>() ); return result;}
        // iterative product
        result.add(new ArrayList<>());
        for (List<Object> pool : lists) {
            List<List<Object>> next = new ArrayList<>();
            for (List<Object> acc : result) {
                for (Object item : pool) {
                    List<Object> extended = new ArrayList<>(acc);
                    extended.add(item);
                    next.add(extended);
                }
            }
            result = next;
        }
        return result;
    }




    //Walk through statements if not null
    public void walk(List<Stmt> program) {
        for (Stmt s : program) if (s != null) s.accept(this);
    }




    @Override
    public Void visitBlockStmt(Block stmt) {
        if (stmt == null) return null;
        if (stmt.statements != null) {
            for (Stmt s : stmt.statements) {
                if (s != null) s.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        if (stmt == null) return null;
        if (stmt.expression != null) stmt.expression.accept(this);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        if (stmt == null) return null;
        // params are Tokens; nothing to traverse there. Traverse body.
        if (stmt.body != null) {
            for (Stmt s : stmt.body) if (s != null) s.accept(this);
        }
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if (stmt == null) return null;
        if (stmt.condition != null) stmt.condition.accept(this);
        if (stmt.thenBranch != null) stmt.thenBranch.accept(this);
        if (stmt.elseBranch != null) stmt.elseBranch.accept(this);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        if (stmt == null) return null;
        if (stmt.expression != null) stmt.expression.accept(this);
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        if (stmt == null) return null;
        if (stmt.value != null) stmt.value.accept(this);
        return null;
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        if (stmt == null) return null;
        if (stmt.condition != null) stmt.condition.accept(this);
        if (stmt.body != null) stmt.body.accept(this);
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        if (stmt == null) return null;
        if (stmt.initializer != null) stmt.initializer.accept(this);
        return null;
    }

    @Override
    public Void visitBreakStmt(Break stmt) {
        // nothing to traverse for a break statement
        return null;
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        if (expr == null) return null;
        if (expr.value != null) expr.value.accept(this);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        if (expr == null) return null;
        if(expr.operator.type == TokenType.PLUS ||
            expr.operator.type== TokenType.MINUS ||
            expr.operator.type== TokenType.SLASH ||
            expr.operator.type== TokenType.STAR){

                TestVariation saveContext = curContext;
                curContext = new PosNegZeroVar();
                if (expr.left != null) expr.left.accept(this);
                if (expr.right != null) expr.right.accept(this);
                curContext=saveContext;
            }else{
                 if (expr.left != null) expr.left.accept(this);
                 if (expr.right != null) expr.right.accept(this);
            }   
       
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        if (expr == null) return null;
        if (expr.callee != null) expr.callee.accept(this);
        if (expr.arguments != null) {
            for (Expr e : expr.arguments) if (e != null) e.accept(this);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        if (expr == null) return null;
        if (expr.expression != null) expr.expression.accept(this);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        // literal has no children to traverse
        return null;
    }

    @Override
    public Void visitLogicalExpr(Logical expr) {
        if (expr == null) return null;
        if (expr.left != null) expr.left.accept(this);
        if (expr.right != null) expr.right.accept(this);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        if (expr == null) return null;

        TestVariation saveContext = curContext;
        if (expr.operator.type == TokenType.MINUS) {
            curContext = new PosNegZeroVar();
        }


        if (expr.right != null) expr.right.accept(this);

        curContext = saveContext;
        return null;
    }

    @Override
    public Void visitVariableExpr(Variable expr) {
        this.varCases.put(expr.name.lexeme, curContext);
        return null;
    }




    @Override
    public String toString() {
        return "TestGenerator [varCases=" + varCases + ", curContext=" + curContext + "]";
    }

    
}
