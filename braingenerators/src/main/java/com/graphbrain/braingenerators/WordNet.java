package com.graphbrain.braingenerators;


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
import java.util.List;


public class WordNet {

    private final Dictionary dictionary;

    public WordNet(Dictionary dictionary) throws JWNLException {
        this.dictionary = dictionary;
    }

    public void test() throws JWNLException, CloneNotSupportedException {

        Synset concept = null;
        try {
            concept = dictionary.getIndexWord(POS.NOUN, "paris").getSenses().get(0);
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }

        System.out.println("===> Concept");
        System.out.println(concept);

        PointerTargetNodeList results;

        System.out.println("\n===> Hypernyms");
        results = PointerUtils.getDirectHypernyms(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> Also sees");
        results = PointerUtils.getAlsoSees(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> Antonyms");
        results = PointerUtils.getAntonyms(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> Attributes");
        results = PointerUtils.getAttributes(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> Causes");
        results = PointerUtils.getCauses(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> Entailments");
        results = PointerUtils.getEntailments(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> Holonyms");
        results = PointerUtils.getHolonyms(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> Meronyms");
        results = PointerUtils.getMeronyms(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> Part Meronyms");
        results = PointerUtils.getPartMeronyms(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> ParticipleOf");
        results = PointerUtils.getParticipleOf(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> Pertainyms");
        results = PointerUtils.getPertainyms(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> Synonyms");
        results = PointerUtils.getSynonyms(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
        }

        System.out.println("\n===> VerbGroup");
        results = PointerUtils.getVerbGroup(concept);
        for (PointerTargetNode result : results) {
            System.out.println(result.getSynset());
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

    public void go() throws JWNLException, CloneNotSupportedException {
        /*
        Iterator<IndexWord> iter = dictionary.getIndexWordIterator(POS.NOUN);

        while (iter.hasNext()) {
            IndexWord word = iter.next();
            System.out.println(word);
        }
        */

        /*
        Synset concept = dictionary.getIndexWord(POS.NOUN, "dog").getSenses().get(0);

        while (concept != null) {
            System.out.println(concept);
            concept = superType(concept);
        }
        */

        /*
        List<Synset> synsets = word.getSenses();

        for (Synset synset : synsets) {
            System.out.println("\n\n");
            System.out.println(synset);
            PointerTargetNodeList hypernyms = PointerUtils.getDirectHypernyms(synset);
            hypernyms.print();
        }8?

        /*
        PointerTargetNodeList hypernyms = PointerUtils.getDirectHypernyms(word.getSenses().get(0));
        System.out.println("Direct hypernyms of \"" + word.getLemma() + "\":");
        hypernyms.print();
        */
    }

    public static String hash(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return Long.toHexString(h);
    }

    public String getVertexId(Word word) {
        String id = ID.sanitize(word.getLemma());

        Word st = superType(word);

        if (st == null) {
            return word.getLemma();
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
                System.out.println(rel);
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
            System.out.println(rel);
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
                System.out.println(rel);
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
                System.out.println(rel);
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
                System.out.println(rel);
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    private void processCanMean(String vid, Word word) {
        String sid = ID.sanitize(word.getLemma());
        String rel = "(r/+can_mean " + sid + " " + vid + ")";
        System.out.println(rel);
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
            System.out.println(rel);
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

    public void process() {
        try {
            List<Synset> concepts = dictionary.getIndexWord(POS.NOUN, "sex").getSenses();

            for (Synset concept : concepts) {
                System.out.println(concept);
                processSynset(concept);
            }
        }
        catch (JWNLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //System.out.println(hash("8e41616fa327b286/Telmo_Menezes"));

        try {
            FileInputStream inputStream = new FileInputStream("braingenerators/file_properties.xml");
            Dictionary dictionary = Dictionary.getInstance(inputStream);

            WordNet wn = new WordNet(dictionary);
            wn.process();
            //wn.test();
        }
        catch (FileNotFoundException | JWNLException e) {
            e.printStackTrace();
        }
        /*catch (FileNotFoundException | JWNLException | CloneNotSupportedException e) {
            e.printStackTrace();
        }*/
    }
}
