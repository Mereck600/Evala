package evala;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Evala {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    // public static void main(String[] args) throws IOException {

    //     if (args.length > 1) {
    //         System.out.println("Usage: evala [script]");
    //         System.exit(64);
    //     } else if (args.length == 1) {
    //         runFile(args[0]);
    //     } else {
    //         runPrompt();
    //     }
    // }
       
    //example on how to test lox parser, code up to ch10
    //     public static void main(String[] args) {
    //     // Create some test tokens for: "print 123 + 456;"
    //     List<Token> tokens = Arrays.asList(
    //     new Token(TokenType.PRINT, "print", null, 1),
    //     new Token(TokenType.NUMBER, "123", 123.0, 1),
    //     new Token(TokenType.PLUS, "+", null, 1),
    //     new Token(TokenType.NUMBER, "456", 456.0, 1),
    //     new Token(TokenType.SEMICOLON, ";", null, 1),
    //     new Token(TokenType.EOF, "", null, 1)
    // );


    //     // Create parser with test tokens
    //     Parser parser = new Parser(tokens);

    //     // Parse and print the result
    //     try {
    //         List<Stmt> statements = parser.parse();
    //         for (Stmt stmt : statements) {
    //             System.out.println(stmt.toString());
    //         }
    //     } catch (RuntimeException error) {
    //         System.err.println("Parse error occurred!");
    //     }
    // }
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: evala [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
        }

    

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) { System.exit(65); }
        if (hadRuntimeError) { System.exit(70); }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        System.out.println("Welcome to evala!");

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        // Stop if there was a syntax error.
        if (hadError) return;

        // Print the AST.
        //System.out.println("Parsed expression: " + expression.toString());
        // >>> grading: walk AST to collect usage (reads/writes/params) + structural checks
        UsageCollector usage = new UsageCollector();           // NEW (see class below)
        usage.walk(statements);

        Grader grader = new Grader(scanner.getCommentStats(),  // NEW (see class below)
                                usage.getUsage(),
                                usage.getIfWithoutElse(),
                                usage.getMagicNumbers());
        GradeReport report = grader.grade();
        report.writeToFile("grade");                            // writes ./grade
        System.out.println(report.summaryLine());
        // <<< grading

        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[Line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}