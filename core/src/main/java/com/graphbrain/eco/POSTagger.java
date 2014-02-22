package com.graphbrain.eco;


import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import java.io.IOException;
import java.io.InputStream;


public class POSTagger {

    // prevent instantiation
    private POSTagger() {}

    private static Tokenizer tokenizer;
    private static opennlp.tools.postag.POSTagger posTagger;

    static {
        // init tokenizer
        tokenizer = null;

        InputStream modelIn = null;
        try {
            // Loading tokenizer model
            modelIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("pos_models/en-token.bin");
            final TokenizerModel tokenModel = new TokenizerModel(modelIn);
            modelIn.close();

            tokenizer = new TokenizerME(tokenModel);
        }
        catch (final IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (final IOException ignored) {} // oh well!
            }
        }

        // init POS tagger
        modelIn = null;
        try {
            // Loading tokenizer model
            modelIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("pos_models/en-pos-maxent.bin");
            final POSModel posModel = new POSModel(modelIn);
            modelIn.close();

            posTagger = new POSTaggerME(posModel);

        }
        catch (final IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (final IOException ignored) {} // oh well!
            }
        }
    }

    public static String[] tokenize(String sentence) {
        return tokenizer.tokenize(sentence);
    }

    public static Word[] annotate(String stringToAnnotate) {
        String[] tokens = tokenize(stringToAnnotate);
        String[] posTokens = posTagger.tag(tokens);

        int length = tokens.length;
        Word[] annotated = new Word[length];

        for (int i = 0; i < length; i++) {
            Word word = new Word(tokens[i], posTokens[i], "");
            annotated[i] = word;
        }

        // lemmatise
        for (int i = 0; i < length; i++) {
            Stemmer stemmer = new Stemmer();
            String word = annotated[i].getWord();
            stemmer.add(word.toCharArray(), word.length());
            stemmer.stem();
            annotated[i].setLemma(stemmer.toString());
        }

        return annotated;
    }

    public static void main(String[] args) {
        Word[] annotated = POSTagger.annotate("Telmo played guitar.");

        for (Word w : annotated) {
            System.out.println(w + "[" + w.getPos() + "; " + w.getLemma() + "]");
        }
    }
}