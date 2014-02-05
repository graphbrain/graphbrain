package com.graphbrain.db;

import java.util.LinkedList;
import java.util.List;

public class EdgeParser {

    private String input;
    private int pos;
    private char c;
    private char EOF;

    public EdgeParser(String input) {
        this.input = input;
        pos = 0;
        c = input.charAt(pos);
        EOF = (char)(-1);
    }

    private void consume() {
        pos += 1;
        if (pos >= input.length())
            c = EOF;
        else
            c = input.charAt(pos);
    }

    public Vertex parse() {
        jumpSpace();
        if (c != '(') {
            return Vertex.fromId(nextToken());
        }
        else {
            consume();
            List<Vertex> params = new LinkedList<>();
            while (c != ')') {
                params.add(parse());
            }

            Vertex[] elems = params.toArray(new Vertex[params.size()]);

            return new Edge(elems);
        }
    }

    private String nextToken() {
        int start = pos;

        while ((c != ' ') && (c != ')') && (c != EOF))
            consume();

        return input.substring(start, pos);
    }

    private void jumpSpace() {
        while (c == ' ')
            consume();
    }

    public static Vertex parse(String input) {
        return (new EdgeParser(input)).parse();
    }
}