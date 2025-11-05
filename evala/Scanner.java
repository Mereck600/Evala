package evala;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static evala.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // ADD: comment/line metrics for grading
    private int singleLineCommentLines = 0;
    private int blockCommentLines = 0;     // only used if you enable /* ... */
    private int codeLines = 0;

    // Track which line we already counted as "code" to avoid double-counting
    private int lastCodeLineCounted = -1;


    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
        keywords.put("break", BREAK);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() { 
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {    
            case '(' -> addToken(LEFT_PAREN);                       // .nah.   updated switch syntax 
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);

            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> { //change this to work with the new count comments
                if (match('/')) {
                    singleLineCommentLines++;
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // consume /* ... */ and count lines inside
                    int startLine = line;
                    while (!isAtEnd()) {
                        if (peek() == '\n') line++;
                        if (peek() == '*' && peekNext() == '/') { advance(); advance(); break; }
                        advance();
                    }
                    int consumed = Math.max(0, line - startLine);
                    blockCommentLines += (consumed == 0 ? 1 : consumed); // count at least 1 line
                } else {
                    addToken(SLASH);
                }
            }



            case ' ', '\r', '\t' -> {} // Ignore whitespace.
            case '\n' -> line++;

            case '"' -> string();

            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Evala.error(line, "Unexpected character.");
                }
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        // See if the identifier is a reserved word.
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the ".".
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Evala.error(line, "Unterminated string.");
            return;
        }

        advance(); // The closing ".

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }
    //Changed to count code lines
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));

        if (type != TokenType.EOF && lastCodeLineCounted != line) {
            // We don't count tokens that come only from whitespace/comments (we never add those anyway)
            codeLines++;
            lastCodeLineCounted = line;
        }

    }
    //Check comments
    public static final class CommentStats {
        public final int singleLine;
        public final int blockLines;
        public final int codeLines;
        public final int totalLines;
        public CommentStats(int singleLine, int blockLines, int codeLines, int totalLines) {
            this.singleLine = singleLine;
            this.blockLines = blockLines;
            this.codeLines  = codeLines;
            this.totalLines = totalLines;
        }
    }
    public CommentStats getCommentStats() {
        // 'line' is 1-based and already tracks total lines as you scan
        return new CommentStats(singleLineCommentLines, blockCommentLines, codeLines, line);
    }
}
