package com.graphbrain.eco;


import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
//import opennlp.tools.sentdetect.SentenceDetector;
//import opennlp.tools.sentdetect.SentenceDetectorME;
//import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import java.io.IOException;
import java.io.InputStream;


public class POSTagger {

    //private SentenceDetector sentenceDetector;
    private Tokenizer tokenizer;
    private opennlp.tools.postag.POSTagger posTagger;

    public POSTagger() {
        // init sentence detector
        /*
        sentenceDetector = null;

        InputStream modelIn = null;
        try {
            // Loading sentence detection model
            File file = new File("en-sent.bin");
            modelIn = new FileInputStream(file);
            final SentenceModel sentenceModel = new SentenceModel(modelIn);
            modelIn.close();

            sentenceDetector = new SentenceDetectorME(sentenceModel);
        }
        catch (final IOException ioe) {
            ioe.printStackTrace();
        }
        */

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

    public Word[] annotate(String stringToAnnotate) {
        //String[] sentences = sentenceDetector.sentDetect(stringToAnnotate);
        String[] tokens = tokenizer.tokenize(stringToAnnotate);
        String[] posTokens = posTagger.tag(tokens);

        int length = tokens.length;
        Word[] annotated = new Word[length];

        for (int i = 0; i < length; i++) {
            Word word = new Word(tokens[i], posTokens[i], "");
            annotated[i] = word;
        }

        /*
        // lemmatise
        List<String> lemmas = s.lemmatize(stringToAnnotate, 0);

        // merge
        i = 0;
        for (String lemma : lemmas) {
            annotated[i].setLemma(lemma);
            i++;
        }
        */

        return annotated;
    }

    public static void main(String[] args) {
        POSTagger tagger = new POSTagger();
        Word[] annotated = tagger.annotate("Telmo likes chocolate.");

        for (Word w : annotated) {
            System.out.println(w + "[" + w.getPos() + "]");
        }
    }
}