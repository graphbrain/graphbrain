package com.graphbrain.eco;

public class Token {

    private String text;
    private TokenType ttype;

    public Token(String text, TokenType ttype) {
        this.text = text;
        this.ttype = ttype;
    }

    @Override
    public String toString() {
        return text + " <" + ttype.toString() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token) {
            Token t = (Token)obj;
            return t.text.equals(text) && t.ttype.equals(ttype);
        }

        return false;
    }

    public String getText() {
        return text;
    }

    public TokenType getTtype() {
        return ttype;
    }
}