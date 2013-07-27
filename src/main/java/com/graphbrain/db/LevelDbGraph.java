package com.graphbrain.db;

import org.iq80.leveldb.*;
import static org.fusesource.leveldbjni.JniDBFactory.*;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class LevelDbGraph extends Graph  {
	private DB db;
	
	public LevelDbGraph() {
		Options options = new Options();
    	options.createIfMissing(true);
    	try {
    		db = factory.open(new File("gbdb"), options);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	public Vertex getTextNode(String id) {
		String value = asString(db.get(bytes(id)));
		return new TextNode(id, stringToMap(value));
	}
	
	public Vertex getURLNode(String id) {
		String value = asString(db.get(bytes(id)));
		return new URLNode(id, stringToMap(value));
	}
	
	public Vertex getUserNode(String id) {
		String value = asString(db.get(bytes(id)));
		return new UserNode(id, stringToMap(value));
	}
	
	public Vertex getEdgeType(String id) {
		String value = asString(db.get(bytes(id)));
		return new EdgeType(id, stringToMap(value));
	}
	
	private static String mapToString(Map<String, String> map) {
		StringBuilder stringBuilder = new StringBuilder();

		for (String key : map.keySet()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("&");
		    }
		    String value = map.get(key);
		    try {
		    	stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
		    	stringBuilder.append("=");
		    	stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
		    }
		    catch (UnsupportedEncodingException e) {
		    	throw new RuntimeException("This method requires UTF-8 encoding support", e);
		    }
		}

		return stringBuilder.toString();
	}
	
	private static Map<String, String> stringToMap(String input) {
		Map<String, String> map = new HashMap<String, String>();

		String[] nameValuePairs = input.split("&");
		for (String nameValuePair : nameValuePairs) {
			String[] nameValue = nameValuePair.split("=");
		    try {
		    	map.put(URLDecoder.decode(nameValue[0], "UTF-8"), nameValue.length > 1 ? URLDecoder.decode(
		    			nameValue[1], "UTF-8") : "");
		    }
		    catch (UnsupportedEncodingException e) {
		    	throw new RuntimeException("This method requires UTF-8 encoding support", e);
		    }
		}

		return map;
	}
	
	public Vertex put(Vertex vertex) {
		String value = mapToString(vertex.toMap());
		db.put(bytes(vertex.getId()), bytes(value));
		return vertex;
	}
	
    public static void main( String[] args ) {
    	Options options = new Options();
    	options.createIfMissing(true);
    	try {
    		DB db = factory.open(new File("example"), options);
    		try {
    			/*
    			for (int i = 0; i < 1000000; i++) {
    				String key = UUID.randomUUID().toString();
    				String value = UUID.randomUUID().toString();
    				db.put(bytes(key), bytes(value));
    			}*/
    			
    			DBIterator iterator = db.iterator();
    			try {
    				iterator.seek(bytes("c"));
    			    String key = asString(iterator.peekNext().getKey());
    			    String value = asString(iterator.peekNext().getValue());
    			    
    			    while (key.compareTo("c1") < 0) {
    			    	System.out.println(key + " = " + value);
    			    	if (iterator.hasNext()) {
    			    		iterator.next();
    			    		key = asString(iterator.peekNext().getKey());
    	    			    value = asString(iterator.peekNext().getValue());
    			    	}
    			    	else {
    			    		key = "ZZZ";
    			    	}
    			    }
    			}
    			finally {
    			  iterator.close();
    			}
    			
    			//String value = asString(db.get(bytes("Tampa")));
    			//System.out.println(value);
    			//db.delete(bytes("Tampa"));
    		}
    		finally {
    			db.close();
    		}
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
