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
    static boolean hadError = false;

    // public static void main(String[] args) throws IOException {
    //     if (args.length > 1) {
    //         System.out.println("Usage: Evala [script]");
    //         System.exit(64);
    //     } else if (args.length == 1) {
    //         runFile(args[0]);
    //     } else {
    //         runPrompt();
    //     }
    // }
        
    //example on how to test lox parser, code up to ch10
        public static void main(String[] args) {
        // Create some test tokens for: "print 123 + 456;"
        List<Token> tokens = Arrays.asList(
            new Token(TokenType.PRINT, "print", null, 1),
            new Token(TokenType.NUMBER, "123", 123.0, 1),
            new Token(TokenType.PLUS, "+", null, 1),
            new Token(TokenType.NUMBER, "456", 456.0, 1),
            new Token(TokenType.COMMA, ";", null, 1),
            new Token(TokenType.EOF, "", null, 1)
        );

        // Create parser with test tokens
        Parser parser = new Parser(tokens);

        // Parse and print the result
        try {
            List<Stmt> statements = parser.parse();
            for (Stmt stmt : statements) {
                System.out.println(stmt.toString());
            }
        } catch (ParseError error) {
            System.err.println("Parse error occurred!");
        }
    }
    

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) { System.exit(65); }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        System.out.println("Welcome to Evala!");

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
        Expr expression = parser.parse();
    
        // Stop if there was a syntax error.
        if (hadError) return;
    
        System.out.println(expression);
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
}