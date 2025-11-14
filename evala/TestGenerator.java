package evala;

import java.util.*;

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



interface TestVariation { }          // context

class NoInfoVar implements TestVariation {}
class PosNegZeroVar implements TestVariation {}

class BooleanVar implements TestVariation {}


class TestCase {
    String functionName;
    List<Object> args;

    
}


public class TestGenerator implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    Map<String, TestVariation> varCases = new HashMap<>();
    TestVariation curContext = new NoInfoVar();


    public TestGenerator(List<Token> params) {
        for (Token tok : params) {
            varCases.put(tok.lexeme, curContext);
        }
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
        if (expr.left != null) expr.left.accept(this);
        if (expr.right != null) expr.right.accept(this);
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
