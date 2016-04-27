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

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.util.List;

public class POSTagger {

    // prevent instantiation
    private POSTagger() {}

    private static MaxentTagger tagger;
    private static StanfordLemmatizer s;

    static {
	tagger = new MaxentTagger(MaxentTagger.DEFAULT_JAR_PATH);
        s = new StanfordLemmatizer();
    }

    /**
     Returns a list of annotated words: (word, pos, lemma)
     */
    public static Word[] annotate(String stringToAnnotate) {
        // taggedTokens
        String taggedString = tagger.tagString(stringToAnnotate);
	if (taggedString.length() == 0) {
	    return new Word[0];
	}
        String[] wordTagPairs = taggedString.split(" ");
	
        int length = wordTagPairs.length;
        Word[] annotated = new Word[length];

        int i = 0;
        for (String wordTag : wordTagPairs) {
            String[] tt = wordTag.split("_");
            Word word = new Word(tt[0], tt[1], "");
            annotated[i] = word;
            i++;
        }

        // lemmatise
        List<String> lemmas = s.lemmatize(stringToAnnotate, 0);

        // merge
        i = 0;
        for (String lemma : lemmas) {
            annotated[i].setLemma(lemma);
            i++;
        }

        return annotated;
    }

    public static void main(String[] args) {
        /*
        POSTagger tagger = new POSTagger();
        List<String[]> annotated = tagger.annotate("Telmo likes chocolate.");

        for (String[] elem : annotated) {
            System.out.println("-> " + elem[0] + " " + elem[1] + " " + elem[2]);
        }
        */
    }
}
