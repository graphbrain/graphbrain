package com.graphbrain.eco;

import java.util.LinkedList;
import java.util.List;

public class Text {

    private String text;
    private List<String> sentences;

    private int start;
    private int end;
    private char c;

    public static List<String> toSentences(String text) {
        return (new Text(text)).getSentences();
    }

    public Text(String text) {
        this.text = text;
        sentences = new LinkedList<>();
        text2Sentences();
    }

    private boolean isFinalPonctuation(char ch) {
        return ((ch == '.') || (ch == '!') || (ch == '?') || (ch == '\n'));
    }

    private boolean isSentenceEnd() {
        if (end == text.length() - 1) {
            return true;
        }

        if ((c != '.') && (c != '!') && (c != '?') && (c != '\n')) {
            return false;
        }

        char nextChar = text.charAt(end + 1);

        if (c == '.') {
            if (Character.isDigit(nextChar)) {
                return false;
            }
        }

        return !isFinalPonctuation(nextChar);
    }

    private void findSentenceEnd() {
        while (true) {
            if (end >= text.length()) {
                return;
            }

            c = text.charAt(end);

            if (isSentenceEnd()) {
                return;
            }
            end++;
        }
    }

    private void addSentence() {
        String sentence = text.substring(start, end + 1).trim();
        if (sentence.length() > 1) {
            sentences.add(sentence);
        }
    }

    private void text2Sentences() {
        start = 0;

        while (start < text.length()) {
            end = start;
            findSentenceEnd();
            addSentence();
            start = end + 1;
        }
    }

    public List<String> getSentences() {
        return sentences;
    }

    public static void main(String[] args) {
        String text = ".Does Telmo like chocolate???    Telmo likes chocolate. He also likes meat. Dogecoin is worth .35 BTC today.";
        text += "\n\nhello world\n";
        text += "\n\nxpto...";

        List<String> sentences = Text.toSentences(text);

        for (String s : sentences) {
            System.out.println("> " + s);
        }
    }
}