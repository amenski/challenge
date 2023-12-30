package it.aman.jsonparser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static it.aman.jsonparser.Lexer.Type.*;

public class Lexer {

    private final List<Token> tokenList = new ArrayList<>();
    private final LinkedList<Character> characterQueue = new LinkedList<>();

    public Lexer(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null");
        }
        copy(input);
    }

    private void copy(String input) {
        for (char c : input.toCharArray()) {
            characterQueue.add(c);
        }
    }

    private void parseValue() throws ParseException {
        this.removeWhitespace();
        if(this.nextToken() == null) return;
        switch (this.nextToken()) {
            case '{':
                parseObject();
                break;
            case '[':
                parseArray();
                break;
            case '"':
                readString();
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '-':
                readNumber();
                break;
            case 't':
                parseTrue();
                break;
            case 'f':
                parseFalse();
                break;
            case 'n':
                parseNull();
                break;
            default:
                throw new ParseException("Parse exception at " + getErrorSection(), 0);
        }
    }

    private String getErrorSection() {
        StringBuilder builder = new StringBuilder();
        for(int i =0; i < 20; i++){
            if(this.nextToken() != null) builder.append(this.consumeNextToken());
        }
        return builder.toString();
    }

    public void parseObject() throws ParseException {
        this.consumeNextToken(); // skip opening
        while(!isNextToken('}') && this.nextToken() != null) {
            this.parseKey();
            if(!isNextToken(':')) {
                throw new RuntimeException("Wrong format. Missing double colon.");
            }
            this.consumeNextToken(); // remove :
            this.parseValue();

            if(!isNextToken(',') && !isNextToken('}')) {
                throw new RuntimeException("Unexpected EOF.");
            }
        }
        this.consumeNextToken();
    }

    public void parseArray() throws ParseException {
        this.consumeNextToken(); // skip opening
        while(!isNextToken(']') && this.nextToken() != null) {
            if (isNextToken('{')) parseObject();
            else parseValue();
        }
        this.consumeNextToken();
    }

    private boolean isNextToken(char c) {
        this.removeWhitespace();
        Character cc = this.nextToken();
        return cc != null && cc == c;
    }

    private Character nextToken() {
        return characterQueue.peek();
    }

    private Character consumeNextToken() {
        return characterQueue.poll();
    }

    private void removeWhitespace() {
        while (this.nextToken() != null && Character.isWhitespace(this.nextToken())) this.consumeNextToken();
    }

    private void parseKey() throws ParseException {
        if (isNextToken(',')) { // a comma after an object inside a parent obj
            this.removeWhitespace();
            this.consumeNextToken();
        }
        this.removeWhitespace();
        if(this.consumeNextToken() != '"') {// skip `"`
            throw new RuntimeException("Key parsing error");
        }
        while (!isNextToken('"')) {
            if (this.isControlChar()) {
                throw new RuntimeException("Wrong key value");
            }
            this.consumeNextToken();
        }
        this.consumeNextToken(); // skip last `"`
    }

    private void parseTrue() {
        parseStringEquals("true");
    }

    private void parseFalse() {
        parseStringEquals("false");
    }

    private void parseNull() {
        parseStringEquals("null");
    }

    private void parseStringEquals(String compare) {
        StringBuilder value = new StringBuilder();
        while (this.nextToken() != null && Character.isLetter(this.nextToken())) {
            value.append(this.consumeNextToken());
        }
        if (!compare.contentEquals(value))
            throw new RuntimeException(String.format("Wrong value: %s vs %s", value, compare));
    }

    private void readNumber() {
        while (this.nextToken() != null && Character.isDigit(this.nextToken())) {
            this.consumeNextToken();
        }
    }

    private void readString() throws ParseException {
        this.consumeNextToken(); // skip `"`
        while (this.nextToken() != null && (this.nextToken()) != '"') {
            if (this.isControlChar()) {
                this.consumeNextToken();
                this.consumeNextToken();
                continue;
            }
            this.consumeNextToken();
        }
        this.consumeNextToken(); // skip last `"`
    }

    private boolean isControlChar() throws ParseException {
        Character currentChar = this.nextToken();
        if (null == currentChar) return false;
        if (currentChar == '\\' && characterQueue.size() > 1) {
            char nextChar = characterQueue.get(1);
            switch (nextChar) {
                case '"':
                case '\'':
                case '\\':
                case 't':
                case 'n':
                case 'r':
                case 'f':
                case 'b':
                    return true;
                default:
                    throw new ParseException("Unknown escape character.", 0);
            }
        }
        return false;
    }

    public List<Token> getTokenList() {
        return tokenList;
    }

    public enum Type {
        BEGIN_OBJECT,
        CLOSE_OBJECT,
        BEGIN_ARRAY,
        CLOSE_ARRAY,
        COLON,
        COMMA,
        STRING,
        NUMBER,
        TRUE,
        FALSE,
        NULL
    }

    public static class Token {
        Type t;
        String c;

        public Token(Type t, String c) {
            this.t = t;
            this.c = c;
        }

        public boolean isBeginObject() {
            return BEGIN_OBJECT == t;
        }

        public boolean isBeginArray() {
            return BEGIN_ARRAY == t;
        }

        public boolean isCloseObject() {
            return CLOSE_OBJECT == t;
        }

        public boolean isCloseArray() {
            return CLOSE_ARRAY == t;
        }

        @Override
        public String toString() {
            return "Token{ t=" + t +", c=" + c + " }";
        }
    }
}
