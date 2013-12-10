package com.graphbrain.braingenerators;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBPediaGraphFromInfobox {

    private static Pattern thingRegex = Pattern.compile("(<http:\\/\\/dbpedia.org\\/resource\\/.+?>)");
    private static Pattern predicateRegex = Pattern.compile("(<http:\\/\\/dbpedia.org\\/ontology\\/.+?>)");
    private static Pattern wikiRegex = Pattern.compile("(<http:\\/\\/en.wikipedia.org\\/wiki\\/.+?>)");
    public static String sourceName = "dbpedia/mappingproperties";
    public static String dataFile = "mappingbased_properties_en.nq";
    //private static String sourceURL = "http://downloads.dbpedia.org/3.7/en/mappingbased_properties_en.nq.bz2";

    /*
    Gets a qtuple and returns a 4-tuple with (node, relation, node, source)
    If the qtuple is not in the correct format, the tuple ("", "", "", "", "") is returned.
    */
    public static String[] processQTuple(String qTuple) {
        Matcher mThings = thingRegex.matcher(qTuple);
        Matcher mPredicate = predicateRegex.matcher(qTuple);
        Matcher mWiki = wikiRegex.matcher(qTuple);

        List<String> lThings = new LinkedList<String>();
        List<String> lPredicate = new LinkedList<String>();
        List<String> lWikiSource = new LinkedList<String>();

        while (mThings.find())
            lThings.add(mThings.group());
        while (mPredicate.find())
            lPredicate.add(mPredicate.group());
        while (mWiki.find())
            lWikiSource.add(mWiki.group());

        String[] things = lThings.toArray(new String[lThings.size()]);
        String[] predicate = lPredicate.toArray(new String[lPredicate.size()]);
        String[] wikiSource = lWikiSource.toArray(new String[lWikiSource.size()]);

        if (things.length == 2 && predicate.length == 1) {
            String subj = Formatting.normalizeWikiTitle(things[0].replace("<http://dbpedia.org/resource/", "").replace(">", ""));
            String obj = Formatting.normalizeWikiTitle(things[1].replace("<http://dbpedia.org/resource/", "").replace(">", ""));
            if (Formatting.isList(subj) || Formatting.isList(obj)) {
                return new String[]{"", "", "", ""};
            }
            String pred = predicate[0].replace("<http://dbpedia.org/ontology/", "").replace(">", "");
            if (wikiSource.length == 1) {
                return new String[]{subj, pred, obj, wikiSource[0].replace("<", "").replace(">", "")};
            }
            else {
                return new String[]{subj, pred, obj,""};
            }
        }
        else {
            return new String[]{"", "", "", ""};
        }
    }

    public static void processFile(String filename, OutputDBWriter output) {

        File file = new File(filename);

        LineIterator reader;

        try {
            reader = FileUtils.lineIterator(file, "UTF-8");
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int inserted = 0;
        output.writeUser();
        inserted += 1;
        int counter = 0;

        //output.writeGeneratorSource(DBPediaGraphFromInfobox.sourceName, DBPediaGraphFromInfobox.sourceURL)
        //inserted += 1
    
        while (reader.hasNext()) {
            String line = reader.next();
            System.out.println("Processed Line: " + counter + " Inserted: " + inserted + line);

            if (line.isEmpty()) {
                return;
            }
            else {
                String[] res = processQTuple(line);

                if (!res[0].isEmpty() || !res[1].isEmpty() || !res[2].isEmpty() || !res[3].isEmpty()) {
                    output.writeOutDBInfo(res[0], res[1], res[2]);
                    inserted += 1;
                }
            }

            counter += 1;
        }


        System.out.println("Start line: 0");
        System.out.println("End line: " + counter);
        System.out.println("Inserted: "+ inserted);
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            processFile(args[0], new OutputDBWriter(args[1], args[2], args[4], args[5]));
        }
        else {
            processFile(DBPediaGraphFromInfobox.dataFile,
                new OutputDBWriter("dbnode",
                    DBPediaGraphFromInfobox.sourceName,
                    "dbpedia",
                    "crawler"));
        }
    }
}