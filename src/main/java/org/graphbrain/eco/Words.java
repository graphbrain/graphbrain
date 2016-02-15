/*
   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
   All rights reserved.

   Written by Telmo Menezes <telmo@telmomenezes.com>

   This file is part of GraphBrain.

   GraphBrain is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   GraphBrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.graphbrain.eco;

import java.util.List;
import java.util.ListIterator;

public class Words {
    private Word[] words;
    private int pos;

    public Words(Word[] words, int pos) {
        this.words = words;
        this.pos = pos;
    }

    public Words(Word[] words) {
        this(words, 0);
    }

    public Words(Words words) {
        this(cloneWords(words.words), words.pos);
    }

    private static Word[] cloneWords(Word[] words) {
        Word[] cwords = new Word[words.length];
        for (int i = 0; i < words.length; i++) {
            cwords[i] = new Word(words[i]);
        }
        return cwords;
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
        if (words2 == null)
            return false;

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
        return new Words(POSTagger.annotate(s));
    }

    public static Words fromStringAndHtmlTags(String s, List<HtmlTag> htmlTags) {
        Words words = new Words(POSTagger.annotate(s));

        ListIterator<HtmlTag> iter = htmlTags.listIterator();
        for (Word w : words.getWords()) {
            if (iter.hasNext()) {
                w.setHtmlTag(iter.next());
            }
        }

        return words;
    }

    public Word[] getWords() {
        return words;
    }

    public int getPos() {
        return pos;
    }
}
