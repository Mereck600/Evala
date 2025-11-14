package evala;

import java.util.List;
import java.util.ArrayList;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {  
    final Environment globals = new Environment();      // a fixed reference to the outermost global environment
    private Environment environment = globals;          // changes as we enter and exit local scopes
    private class BreakException extends RuntimeException {}

    Interpreter() {
        globals.define("TestCase", new EvalaCallable() {
            @Override
            public int arity() {
                // variable arity: we want TestCase("fn", arg1, ..., expected)
                // we'll handle the count manually in call()
                return -1; // see note on arity handling below
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.size() < 2) {
                    // Need at least function name + expected
                    throw new RuntimeError(
                        new Token(TokenType.IDENTIFIER, "TestCase", null, -1),
                        "TestCase(fnName, arg1, ..., expected) requires at least 2 arguments.");
                }

                Object fnNameObj = arguments.get(0);
                if (!(fnNameObj instanceof String)) {
                    throw new RuntimeError(
                        new Token(TokenType.IDENTIFIER, "TestCase", null, -1),
                        "First argument to TestCase must be a function name (string).");
                }

                String fnName = (String) fnNameObj;

                // Last argument is expected value
                Object expected = arguments.get(arguments.size() - 1);

                // Everything between is a function argument
                List<Object> args = new ArrayList<>();
                for (int i = 1; i < arguments.size() - 1; i++) {
                    args.add(arguments.get(i));
                }

                return new TestCase(fnName, args, expected);
            }

            @Override
            public String toString() { return "<native TestCase>"; }
        });

        globals.define("clock", new EvalaCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter,
                                List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        globals.define("runTests", new EvalaCallable() {
            @Override
            public int arity() {
                // allow any number of TestCase arguments
                return -1; 
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                int total = 0;
                int passed = 0;

                for (Object obj : arguments) {
                    if (!(obj instanceof TestCase)) {
                        System.out.println("[WARN] runTests: argument is not a TestCase: " + obj);
                        continue;
                    }
                    total++;
                    TestCase tc = (TestCase) obj;

                    Object result = callFunctionByName(interpreter, tc.functionName, tc.args);
                    boolean ok = java.util.Objects.equals(result, tc.expected);

                    if (ok) {
                        passed++;
                        System.out.println("[PASS] " + tc.functionName + tc.args + " == " + tc.expected);
                    } else {
                        System.out.println("[FAIL] " + tc.functionName + tc.args
                                + " expected: " + tc.expected
                                + ", got: " + result);
                    }
                }

                System.out.println("--- TEST SUMMARY ---");
                System.out.println("Passed " + passed + " / " + total);

                // return fraction passed (double)
                if (total == 0) return 0.0;
                return (double) passed / (double) total;
            }

            @Override
            public String toString() { return "<native runTests>"; }
         });

        

    }
    static Object callFunctionByName(Interpreter interpreter, String name, List<Object> args) {
        // Create a fake Token just to reuse the existing Environment.get
        Token nameToken = new Token(TokenType.IDENTIFIER, name, null, -1);

        Object callee = interpreter.globals.get(nameToken);
        if (!(callee instanceof EvalaCallable)) {
            throw new RuntimeError(nameToken, "Test error: '" + name + "' is not a function.");
        }

        EvalaCallable fn = (EvalaCallable) callee;

        int expectedArity = fn.arity();
        if (expectedArity >= 0 && expectedArity != args.size()) {
            throw new RuntimeError(nameToken,
                "Test error: function '" + name + "' expects " + expectedArity
            + " args but got " + args.size());
        }

        return fn.call(interpreter, args);
}


    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Evala.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            default:
                break;
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) { 
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof EvalaCallable)) {
            throw new RuntimeError(expr.paren,
                "Can only call functions and classes.");
        }

        EvalaCallable function = (EvalaCallable)callee;
        if (arguments.size() != function.arity()) {
            int expected = function.arity();
            if (expected >= 0 && arguments.size() != expected) {
                throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
            }
           
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
            default:
                return null;
        }
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    // note: not private
    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;    // restore
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        EvalaFunction function = new EvalaFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (BreakException e) {
                break;
            }
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }
    
}