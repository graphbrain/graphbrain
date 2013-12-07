package com.graphbrain.eco;

public class Words {
    private static POSTagger tagger = new POSTagger();

    private Word[] words;
    private int pos;

    public Words(Word[] words, int pos) {
        this.words = words;
        this.pos = pos;
    }

    public Words(Word[] words) {
        this(words, 0);
    }

    public String text() {
        StringBuilder sb = new StringBuilder(100);

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(words[i]);
        }

        return sb.toString();
    }

    public int length() {
        return words.length;
    }

    public Words append(Words extra) {
        int len1 = words.length;
        int len2 = extra.words.length;
        Word[] m = new Word[len1 + len2];
        System.arraycopy(words, 0, m, 0, len1);
        System.arraycopy(extra.words, 0, m, len1, len2);

        return new Words(m);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(words[i]);
        }

        return sb.toString();
    }

    public boolean endsWith(Words words2) {
        int length1 = words.length;
        int length2 = words2.words.length;

        if (length2 > length1)
            return false;
        if (length1 == 0)
            return false;
        if (length2 == 0)
            return false;

        for (int i = 0; i < length2; i++)
            if (!words2.words[length2 - i - 1].getWord().equals(words[length1 - i - 1].getWord()))
                return false;

        return true;
    }

    public Words removeFullStop() {
        if(words.length < 1)
            return this;

        if (words[words.length - 1].getWord().equals(".")) {
            int newLen = words.length - 1;
            Word[] newWords = new Word[newLen];
            System.arraycopy(words, 0, newWords, 0, newLen);
            return new Words(newWords, pos);
        }
        else {
            return this;
        }
    }

    public Words slice(int start, int end) {
        int newLen = end - start + 1;
        Word[] newWords = new Word[newLen];
        System.arraycopy(words, start, newWords, 0, newLen);

        return new Words(newWords, pos + start);
    }

    public static Words empty() {
        return new Words(new Word[]{});
    }

    public static Words fromString(String s) {
        return new Words(tagger.annotate(s));
    }

    public Word[] getWords() {
        return words;
    }

    public int getPos() {
        return pos;
    }
}