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

import org.apache.commons.lang.builder.HashCodeBuilder;

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
    public boolean equals(Object obj) {
        if (obj instanceof Word) {
            Word wrd = (Word)obj;
            return (wrd.word.equals(word)
                    && wrd.pos.equals(pos));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
            append(word).
            append(pos).
            toHashCode();
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
