package com.graphbrain.braingenerators;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBPediaGraphFromCategories {

    private static Pattern thingRegex = Pattern.compile("(<http:\\/\\/dbpedia.org\\/resource\\/.+?>)");
    private static Pattern predicateRegex = Pattern.compile("(<http:\\/\\/dbpedia.org\\/ontology\\/.+?>)");
    private static Pattern owlString = Pattern.compile("<http:.*owl#Thing>");
    private static Pattern wikiRegex = Pattern.compile("(<http:\\/\\/en.wikipedia.org\\/wiki\\/.+?>)");
    public static String sourceName = "dbpedia/instancetypes";
    public static String sourceURL = "http://downloads.dbpedia.org/3.7/en/instance_types_en.nq.bz2";
    public static String dataFile = "instance_types_en.nq";
    public static String[] vowels = new String[]{"a", "e", "i", "o", "u"};

    /*
    Gets a qtuple and returns a 4-tuple with (node, relation, node, source)
    If the qtuple is not in the correct format, the tuple ("", "", "", "", "") is returned.
    */

    public static String[] processQTuple(String qTuple) {
        Matcher mThings = thingRegex.matcher(qTuple);
        List<String> lThings = new LinkedList<String>();
        while (mThings.find())
            lThings.add(mThings.group());
        String[] thing = lThings.toArray(new String[lThings.size()]);

        Matcher mWiki = wikiRegex.matcher(qTuple);
        List<String> lWikiSource = new LinkedList<String>();
        while (mWiki.find())
            lWikiSource.add(mWiki.group());
        String[] wikiSource = lWikiSource.toArray(new String[lWikiSource.size()]);

        String category = getCategory(qTuple);
    

        if(thing.length == 1 && category.length() >= 1) {
            String subj = Formatting.normalizeWikiTitle(thing[0].replace("<http://dbpedia.org/resource/", "").replace(">", ""));
            if (Formatting.isList(subj)) {
                return new String[]{"", "", "", ""};
            }
            if(wikiSource.length == 1) {
                System.out.println(category);

                if(isVowel(("" + category.charAt(0)).toLowerCase())) {
                    System.out.println(subj + "," + "isAn" + "," + category + "," +  wikiSource[0].replace("<", "").replace(">", ""));
                    return new String[]{subj, "isAn", category, wikiSource[0].replace("<", "").replace(">", "")};
                }
                else {
                    return new String[]{subj, "isA", category, wikiSource[0].replace("<", "").replace(">", "")};
                }
            }
            else {
                if(isVowel(("" + category.charAt(0)).toLowerCase())) {
                    System.out.println(subj + "," + "isAn" + "," + category);
                    return new String[]{subj, "isAn", category, ""};
                }
                else {
                    return new String[]{subj, "isA", category, ""};
                }
            }
        }
        return new String[]{"", "", "", ""};
    }

    private static boolean isVowel(String text) {
        for(String v : vowels) {
            if(text.toLowerCase().equals(v)) {
                return true;
            }
        }
        return false;
    }

    private static String getCategory(String qTuple) {
        Matcher mPredicate = predicateRegex.matcher(qTuple);
        if(mPredicate.find()) {
            String category = mPredicate.group();
            return Formatting.separateWords(category.replace("<http://dbpedia.org/ontology/", "").replace(">", "")).trim();
        }

        Matcher mOwlString = owlString.matcher(qTuple);
        if(mOwlString.find()) {
            return "owl#Thing";
        }
        return "";
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

        int counter = 0;
        int inserted = 0;
        output.writeUser();
        inserted += 1;
    

        List<String[]> items = new LinkedList<String[]>();

        while(reader.hasNext()) {
            String line = reader.nextLine();
            System.out.println("Processed Line: " + counter);
            System.out.println(line);

            if(line.isEmpty()) {
                return;
            }
            else {
                String[] res = processQTuple(line);

                if (res[0].isEmpty() && res[1].isEmpty() && res[2].isEmpty() && res[3].isEmpty()) {
                    System.out.println("empty"); //Don't output
                }
                //case (b:String, c:String, d:String, e:String) => d match {
                if (res[2].equals("owl#Thing")) {
                    inserted += addTypes(items, output);
                    items.clear();
                    System.out.println("Top");
                }
          	    else {
                    items.add(processQTuple(line));
                    System.out.println("Thing");
                }
            }

            counter += 1;
        }

        System.out.println("Start line: 0");
        System.out.println("End line: "+ counter);
        System.out.println("Inserted: "+ inserted);
    }

    public static int addTypes(List<String[]> items, OutputDBWriter output) {
        int inserted = 0;
        String rel = "isA";

        if (items.size() == 0) {
            return inserted;
        }
        if (items.size() == 1) {
            String[] x = items.get(0);
            if(isVowel(("" + x[2].charAt(0)).toLowerCase())) {
                rel="isAn";
            }
            output.writeOutDBInfo(Formatting.normalizeWikiTitle(x[0]), rel, Formatting.normalizeWikiTitle(x[2]));
            inserted += 1;
            System.out.println("Inserted: " + inserted);
            System.out.println(Formatting.normalizeWikiTitle(x[0]) + "," + rel + "," + Formatting.normalizeWikiTitle(x[2]) + "," + x[3]);
            return inserted;
        }
        else {
            String[] x = items.get(0);
            String[] y = items.get(1);
            //String a = x[0];
            //String b = x[1];
            String c = x[2];
            //String d = x[3];

            //String f = y[0];
            //String g = y[1];
            String h = y[2];
            //String i = y[3];

            if(isVowel(("" + c.charAt(0)).toLowerCase())) {
                rel = "isAn";
            }
            output.writeOutDBInfo(Formatting.normalizeWikiTitle(h), rel, Formatting.normalizeWikiTitle(c));
            inserted += 1;
            System.out.println("Inserted: " + inserted);
            System.out.println(Formatting.normalizeWikiTitle(h) + "," + rel + "," + Formatting.normalizeWikiTitle(c) + ",");
            return addTypes(items.subList(1, items.size()), output);
        }
    }

    public static void main(String[] args) {
        if (args.length == 3) {
            processFile(args[0], new OutputDBWriter(args[1], args[2], args[4], args[5]));
        }
        else {
            processFile(DBPediaGraphFromCategories.dataFile, new OutputDBWriter("gb", DBPediaGraphFromCategories.sourceName, "dbpedia", "crawler"));
        }
    }
}