package com.graphbrain.eco;

public class Word {

    private String word;
    private String pos;
    private String lemma;

    public Word(String word, String pos, String lemma) {
        this.word = word;
        this.pos = pos;
        this.lemma = lemma;
    }

    @Override
    public String toString() {
        return word + " [" + pos + ", " + lemma + "]";
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
}