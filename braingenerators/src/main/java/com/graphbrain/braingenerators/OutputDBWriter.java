package com.graphbrain.braingenerators;

import com.graphbrain.db.*;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputDBWriter {

    private String username;
    private String name;
    private String role;

    private Graph store;
    private Vertex wikiPageTitle;
    private String asInRel;

    private static Pattern patCapRegex = Pattern.compile("[A-Z][a-z]*");
    private static Pattern patNonCapRegex = Pattern.compile("[a-z]+");
    private static Pattern patDisambig = Pattern.compile("\\(.*?\\)");

    public OutputDBWriter(String storeName, String username, String name, String role) {
        this.username = username;
        this.name = name;
        this.role = role;

        store = new Graph(storeName);
        store.createUser("dbpedia", "dbpedia", "", "", "crawler");

    }

    public void writeOutDBInfo(String node1, String relin, String node2) {
        String globalRelType = relin.trim();

        String id1 = insertAndGetID(node1, username);
        System.out.println("id1: " + id1);

        String id2 = insertAndGetID(node2, username);
        System.out.println("id2: " + id1);

        String rel = "(r/+" + globalRelType + " " + id1 + " " + id2 + ")";
        addRelation(rel);
        System.out.println("rel: " + rel);


    }


	public boolean nodeExists(Vertex node) {
		return store.exists(node.id);
	}

	public String removeWikiDisambig(String wikiTitle) {
        String title = wikiTitle.split("\\(")[0];
        title = StringUtils.strip(title, "_");
        title = title.trim();
        return title;
    }

    private void addRelation(String rel) {
        System.out.println(rel);
        Edge edge = Edge.fromId(rel);
        try {
            store.put(edge, "dbpedia");
        }
        catch (Exception e) {

        }

    }



	public String insertAndGetID(String wikiText, String username) {
        String decodedText;
        TextNode newNode;





        try {
            decodedText = URLDecoder.decode(wikiText, "UTF-8");

            newNode = new TextNode(wikiText);

        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        String titleSP = removeWikiDisambig(decodedText);
        Matcher m = patDisambig.matcher(decodedText);



        String disAmb = "";
        if (m.find()) {
            disAmb = m.group();
        }
        if(!disAmb.isEmpty()) {

            String rel = "(r/as_in " + titleSP + " " + newNode.id + ")";
            addRelation(rel);

        }

		return newNode.id;
	}

    public void writeUser() {
        store.createUser(username, name, "", "", role);
    }

    public void writeURLNode(Vertex node, String url) {
  	    //val sourceNode = store.getSourceNode(ID.source_id(source))
  		Vertex urlNode = store.put(URLNode.fromUrl(url));
  		store.getOrInsert(node, ID.userIdFromUsername(username));
  		store.getOrInsert(urlNode, ID.userIdFromUsername(username));
  		//store.getOrInsert(sourceNode, HGDBID.userIdFromUsername(username))
  		store.connectVertices(new String[]{"en_wikipage", urlNode.id, node.id}, ID.userIdFromUsername(username));
  		//store.addrel2("source", Array[String](sourceNode.id, urlNode.id), HGDBID.userIdFromUsername(username))
    }

  	public void addWikiPageToDB(String pageTitle) {
    	String pageURL = Wikipedia.wikipediaBaseURL + pageTitle.trim().replace(" ", "_");
    	Vertex pageNode = store.put(EntityNode.fromNsAndText("wikipedia", pageTitle));
    	writeURLNode(pageNode, pageURL);
  	}

  	public String separateWords(String stringToSep) {
  		String separated = "";
  		Matcher nonCapsSep = patNonCapRegex.matcher(stringToSep);
  		Matcher capsSeparated = patCapRegex.matcher(stringToSep);
  		
  		if (nonCapsSep.find()) {
  			separated += " " + nonCapsSep.group().toLowerCase();
  		}

  		while (capsSeparated.find()) {
            separated += " " + capsSeparated.group().toLowerCase();
  		}
  		return separated.trim();
  	}

    /*
	public String getRelID(String rel, String node1ID, String node2ID) {
        return Edge.buildId(new String[]{rel, node1ID, node2ID});
	}
	*/
}