package com.graphbrain.eco;

public class Word {

    private String word;
    private String pos;
    private String lemma;
    private HtmlTag htmlTag;

    public Word(String word, String pos, String lemma) {
        this.word = word;
        this.pos = pos;
        this.lemma = lemma;
        htmlTag = null;
    }

    public Word(Word word) {
        this(word.word, word.pos, word.lemma);
    }

    @Override
    public String toString() {
        return word;
    }

    public String getWord() {
        return word;
    }

    public String getPos() {
        return pos;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public HtmlTag getHtmlTag() {
        return htmlTag;
    }

    public void setHtmlTag(HtmlTag htmlTag) {
        this.htmlTag = htmlTag;
    }
}