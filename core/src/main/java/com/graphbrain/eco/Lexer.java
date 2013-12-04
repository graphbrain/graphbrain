package com.graphbrain.eco;

import java.util.LinkedList;
import java.util.List;

public class Lexer {

    private String input;
    private int pos;
    private char c;
    private char EOF;

    public Lexer(String input) {
        this.input = input;
        pos = 0;
        c = input.charAt(pos);
        EOF = (char)-1;
    }

    public List<Token> tokens() {
        List<Token> toks = new LinkedList<Token>();

        Token tok = nextToken();
        while (tok != null) {
            toks.add(tok);
            tok = nextToken();
        }

        return toks;
    }

    private Token nextToken() {
        if (c == EOF) {
            return null;
        }
        else {
            while (Character.isWhitespace(c))
                consume();

            TokenType tt = predict();
            switch(tt) {
                case Symbol: return tokSymbolOrVertex();
                case Number: return tokNumber();
                case String: return tokString();
                case LPar: return tokLPar();
                case RPar: return tokRPar();
            }

            return null;
        }
    }

    private void consume() {
        pos += 1;
        if (pos >= input.length())
            c = EOF;
        else
            c = input.charAt(pos);
    }

    private boolean onLastChar() {
        return pos >= (input.length() - 1);
    }

    private TokenType predict() {
        if (Character.isDigit(c))
            return TokenType.Number;
        else
            switch(c) {
                case '"': return TokenType.String;
                case '(': return TokenType.LPar;
                case ')': return TokenType.RPar;
                case '-': {
                    if (onLastChar()) {
                        return TokenType.Symbol;
                    }
                    else {
                        char next = input.charAt(pos + 1);
                        if (Character.isDigit(next))
                            return TokenType.Number;
                        else
                            switch(next) {
                                case '.': return TokenType.Number;
                                default: return TokenType.Symbol;
                            }
                        }
                    }
                default: return TokenType.Symbol;
            }
    }

    private Token tokSymbolOrVertex() {
        StringBuilder sb = new StringBuilder(25);
        boolean done = false;

        while (!done) {
            sb.append(c);
            consume();

            if ((!Character.isLetter(c))
                && (!Character.isDigit(c))
                && (c != '-')
                && (c != '_')
                && (c != ':')
                && (c != '|')
                && (c != '+')) {

                done = true;
            }
        }

        String str = sb.toString();
        TokenType ttype;
        if (str.contains("/"))
            ttype = TokenType.Vertex;
        else
            ttype = TokenType.Symbol;

        return new Token(str, ttype);
    }

    private Token tokNumber() {
        StringBuilder sb = new StringBuilder(25);
        boolean done = false;
        boolean dotSeen = false;

        while (!done) {
            if (c == '.')
                dotSeen = true;

            sb.append(c);
            consume();

            if ((!Character.isDigit(c))
                && ((c != '.') || ((c == '.') && dotSeen))) {

                done = true;
            }
        }

        return new Token(sb.toString(), TokenType.Number);
    }

    private Token tokString() {
        consume();
        if (onLastChar())
            return new Token("", TokenType.String);

        StringBuilder sb = new StringBuilder(25);
        boolean done = false;

        while (!done) {
            sb.append(c);
            consume();

            if (c == '"') {
                done = true;
            }
        }

        consume();

        return new Token(sb.toString(), TokenType.String);
    }

    private Token tokLPar() {
        consume();
        return new Token("(", TokenType.LPar);
    }

    private Token tokRPar() {
        consume();
        return new Token(")", TokenType.RPar);
    }

    public static void main(String[] args) {
        Lexer l = new Lexer("1 + 1");
        System.out.println(l.tokens());
    }
}