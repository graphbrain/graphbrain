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
        wikiPageTitle = store.put(new EdgeType(ID.reltype_id("sys/wikipedia"), "wikipedia"));
        asInRel = ID.reltype_id("as in", 1);
    }

	public void writeOutDBInfo(String node1, String relin, String node2) {
		try {
			String globalRelType = ID.reltype_id(separateWords(relin.trim()), 1);
			
			Vertex ng1 = insertAndGetWikiDisambigNode(node1, username);
			Vertex ng2 = insertAndGetWikiDisambigNode(node2, username);
			EdgeType relType = new EdgeType(globalRelType, separateWords(relin.trim()));
            store.put(relType);

            System.out.println("! relType: " + relType);
            System.out.println("! username: " + username);
            System.out.println("! userIdFromUsername: " + ID.userIdFromUsername(username));
            System.out.println("! relType.getLabel(): " + relType.getLabel());
            System.out.println("! store.getOrInsert(relType, ID.userIdFromUsername(username)): " + store.getOrInsert(relType, ID.userIdFromUsername(username)));

			System.out.println("relType: " + store.getOrInsert(relType, ID.userIdFromUsername(username)).id + ", " + relType.getLabel());
            System.out.println("ng1: " + store.getOrInsert(ng1, ID.userIdFromUsername(username)).id);
            System.out.println("ng2: " + store.getOrInsert(ng2, ID.userIdFromUsername(username)).id);
			
			// Relationship at global level
			store.connectVertices(new String[]{relType.id, ng1.id, ng2.id}, ID.userIdFromUsername(username));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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

	public Vertex insertAndGetWikiDisambigNode(String wikiTitle, String username) {
        String decodedTitle;
        Vertex wikiNode;

        try {
            decodedTitle = URLDecoder.decode(wikiTitle, "UTF-8");
		    wikiNode = store.put(EntityNode.fromNsAndText("wikipedia", URLDecoder.decode(wikiTitle, "UTF-8")));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        String titleSP = removeWikiDisambig(decodedTitle);

        int i = 1;

		while(store.exists(EntityNode.id("" + i, titleSP))) {
			Vertex existingNode = store.get(EntityNode.id("" + i, titleSP));
            Matcher m = patDisambig.matcher(decodedTitle);

            String disAmb = "";
            if (m.find()) {
                disAmb = m.group();
            }

            if (existingNode.type() == VertexType.Entity) {
		        EntityNode e = (EntityNode)existingNode;

                if(!disAmb.isEmpty()) {
				  	String da = disAmb.replace("(", "").replace(")", "").trim();
				  	Vertex daNode = store.put(EntityNode.fromNsAndText("1", da));
				  	String daID = daNode.id;
				  	Edge daRel = Edge.fromParticipants(new String[]{asInRel, e.id, daID});
				  	if(e.text().equals(titleSP.toLowerCase())) {
				  		for(Edge nEdge : store.edges(existingNode.id)) {
				  			System.out.println(nEdge);
				  			if(nEdge == daRel) {
                                System.out.println("Match: " + nEdge);
				  				return existingNode;
				  			}
				  		}
				  	}
				}
				else {
				  	if(e.text().equals(titleSP.toLowerCase())) {
				        return existingNode;
				  	}
				}
			}
			i += 1;
		}

        Vertex newNode = store.put(EntityNode.fromNsAndText("" + i, titleSP));
		System.out.println(store.getOrInsert(newNode, ID.userIdFromUsername(username)).id);

		
		if(!store.exists(wikiNode.id)) {
		    store.put(wikiNode);
            System.out.println("Wiki_ID: " + store.get(wikiNode.id).id);
		}
		try {
		    store.connectVertices(new String[]{wikiPageTitle.id, newNode.id, wikiNode.id});
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }

        Matcher m = patDisambig.matcher(decodedTitle);
        String disAmbA = "";
        if (m.find())
            disAmbA = m.group();
		
		if(!disAmbA.isEmpty()) {
			String da = disAmbA.replace("(", "").replace(")", "").trim();
			EntityNode daNode = EntityNode.fromNsAndText("1", da);
            store.put(daNode);
			System.out.println(store.getOrInsert(daNode, ID.userIdFromUsername(username)).id + ", da: " +  daNode.text());
			store.connectVertices(new String[]{asInRel, newNode.id, daNode.id}, ID.userIdFromUsername(username));
		}

		return newNode;
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

	public String getRelID(String rel, String node1ID, String node2ID) {
        return Edge.buildId(new String[]{rel, node1ID, node2ID});
	}
}