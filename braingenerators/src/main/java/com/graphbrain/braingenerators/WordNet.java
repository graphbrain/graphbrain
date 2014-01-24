package com.graphbrain.braingenerators;


import com.graphbrain.db.Edge;
import com.graphbrain.db.Graph;
import com.graphbrain.db.ID;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.dictionary.Dictionary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;


public class WordNet {

    private Dictionary dictionary;
    private Graph graph;
    private boolean dryTest;

    public WordNet(Dictionary dictionary, boolean dryTest) throws JWNLException {
        this.dictionary = dictionary;
        this.dryTest = dryTest;
        graph = new Graph();
    }

    public WordNet(Dictionary dictionary) throws JWNLException {
        this(dictionary, false);
    }

    private void addRelation(String rel) {
        System.out.println(rel);
        Edge edge = Edge.fromId(rel);
        if (!dryTest) {
            graph.put(edge, "wordnet");
        }
    }

    private Word superType(Word word) {

        Synset concept = word.getSynset();

        try {
            PointerTargetNodeList hypernyms = PointerUtils.getDirectHypernyms(concept);

            if (hypernyms.size() == 0) {
                return null;
            }

            PointerTargetNode hypernym = hypernyms.getFirst();

            if (hypernym == null) {
                return null;
            }

            return hypernym.getSynset().getWords().get(0);
        }
        catch (JWNLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean hasSuperType(Word word) {
        return superType(word) != null;
    }

    private String hash(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return Long.toHexString(h);
    }

    private String getVertexId(Word word) {
        String id = ID.sanitize(word.getLemma());

        Word st = superType(word);

        if (st == null) {
            return id;
        }

        String stId = getVertexId(st);

        String rel = "(r/+type_of " + id + " " + stId + ")";
        //System.out.println(rel);
        return hash(rel) + "/" + id;
    }

    private void processSuperTypes(String vid, Word word) {

        Synset concept = word.getSynset();

        try {
            PointerTargetNodeList hypernyms = PointerUtils.getDirectHypernyms(concept);

            for (PointerTargetNode hypernym : hypernyms) {
                Word superWord = hypernym.getSynset().getWords().get(0);
                String superId = getVertexId(superWord);

                String rel = "(r/+type_of " + vid + " " + superId + ")";
                addRelation(rel);
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    private void processSynonyms(Synset synset) {
        List<Word> wordList = synset.getWords();
        int wordCount = wordList.size();
        Word mainWord = wordList.get(0);
        String vid = getVertexId(mainWord);
        for (int i = 1; i < wordCount; i++) {
            Word syn =  wordList.get(i);
            String synId = getVertexId(syn);
            String rel = "(r/+synonym " + vid + " " + synId + ")";
            addRelation(rel);
        }
    }

    private void processMeronyms(Word word) {
        Synset concept = word.getSynset();
        String vid = getVertexId(word);

        try {
            PointerTargetNodeList results = PointerUtils.getMeronyms(concept);
            for (PointerTargetNode result : results) {
                Word partWord = result.getSynset().getWords().get(0);
                String partId = getVertexId(partWord);
                String rel = "(r/+part_of " + partId + " " + vid + ")";
                addRelation(rel);
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    private void processAntonyms(Word word) {
        Synset concept = word.getSynset();
        String vid = getVertexId(word);

        try {
            PointerTargetNodeList results = PointerUtils.getAntonyms(concept);
            for (PointerTargetNode result : results) {
                Word antWord = result.getSynset().getWords().get(0);
                String antId = getVertexId(antWord);
                String rel = "(r/+antonym " + vid + " " + antId + ")";
                addRelation(rel);
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    private void processAlsoSees(Word word) {
        Synset concept = word.getSynset();
        String vid = getVertexId(word);

        try {
            PointerTargetNodeList results = PointerUtils.getAlsoSees(concept);
            for (PointerTargetNode result : results) {
                Word alsoWord = result.getSynset().getWords().get(0);
                String alsoId = getVertexId(alsoWord);
                String rel = "(r/+also_see " + vid + " " + alsoId + ")";
                addRelation(rel);
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    private void processCanMean(String vid, Word word) {
        if (!hasSuperType(word)) {
            return;
        }

        String sid = ID.sanitize(word.getLemma());
        String rel = "(r/+can_mean " + sid + " " + vid + ")";
        addRelation(rel);
    }

    private void processPOS(String vid, Word word) {
        POS pos = word.getPOS();
        String posId = null;

        switch(pos) {
            case NOUN:
                posId = "850e2accee28f70e/noun";
                break;
            case VERB:
                posId = "b43b5b40bb0873e9/verb";
                break;
            case ADJECTIVE:
                posId = "90a283c76334fb9d/adjective";
                break;
            case ADVERB:
                posId = "20383f8100e0be26/adverb";
                break;
        }

        if (pos != null) {
            String rel = "(r/+pos " + vid + " " + posId + ")";
            addRelation(rel);
        }
    }

    private void processSynset(Synset synset) {
        processSynonyms(synset);

        processMeronyms(synset.getWords().get(0));
        processAntonyms(synset.getWords().get(0));
        processAlsoSees(synset.getWords().get(0));

        for (Word word : synset.getWords()) {
            String vid = getVertexId(word);
            System.out.println(vid);

            processCanMean(vid, word);
            processSuperTypes(vid, word);
            processPOS(vid, word);
        }
    }

    public void processPOSSynset(POS pos) {
        try {
            Iterator<Synset> iter = dictionary.getSynsetIterator(pos);

            while (iter.hasNext()) {
                Synset synset = iter.next();
                System.out.println(synset);
                processSynset(synset);
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        graph.createUser("wordnet", "wordnet", "", "", "crawler");

        processPOSSynset(POS.NOUN);
        processPOSSynset(POS.VERB);
        processPOSSynset(POS.ADJECTIVE);
        processPOSSynset(POS.ADVERB);
    }

    public void test() throws JWNLException {
        List<Synset> synsets = dictionary.getIndexWord(POS.ADJECTIVE, "violent").getSenses();
        for (Synset s : synsets) {
            System.out.println(s);

            Word st = superType(s.getWords().get(0));
            System.out.println(st);

            processSynset(s);

            System.out.println("\n");
        }
    }


    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        try {
            FileInputStream inputStream = new FileInputStream("braingenerators/file_properties.xml");
            Dictionary dictionary = Dictionary.getInstance(inputStream);

            WordNet wn = new WordNet(dictionary, true);
            //wn.run();
            wn.test();
        }
        catch (FileNotFoundException | JWNLException e) {
            e.printStackTrace();
        }
    }
}
