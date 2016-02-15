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

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ling.CoreLabel;
import java.util.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;


class StanfordLemmatizer {

    protected StanfordCoreNLP pipeline;

    public StanfordLemmatizer() {
        // Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        // StanfordCoreNLP loads a lot of models, so you probably
        // only want to do this once per execution
        this.pipeline = new StanfordCoreNLP(props);
    }

    public List<String> lemmatize(String documentText, int returnType)
    {

        List<String> words = new LinkedList<>();
        List<String> lemmas = new LinkedList<>();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);

        // run all Annotators on this text
        this.pipeline.annotate(document);

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
                words.add(token.word());
                lemmas.add(token.lemma());
//                lemmas.add(token.get(LemmaAnnotation.class));
            }
        }
        if(returnType==0) {
            return lemmas;
        }
        else {
            return words;
        }

    }

    public static void main(String[] args) {
        StanfordLemmatizer s = new StanfordLemmatizer();
        String tree = "tree";
        List<String> lemmas=s.lemmatize(tree, 1);

        System.out.println(lemmas);
    }
}
