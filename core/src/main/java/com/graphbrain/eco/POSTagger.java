package com.graphbrain.eco;

import com.graphbrain.nlp.StanfordLemmatizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.util.List;

public class POSTagger {

    private MaxentTagger tagger;
    private StanfordLemmatizer s;

    public POSTagger() {
	    tagger = new MaxentTagger("pos_models/english-left3words-distsim.tagger");
        s = new StanfordLemmatizer();
    }

    /**
     Returns a list of annotated words: (word, pos, lemma)
     */
    public Word[] annotate(String stringToAnnotate) {
        // taggedTokens
        String taggedString = tagger.tagString(stringToAnnotate);
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