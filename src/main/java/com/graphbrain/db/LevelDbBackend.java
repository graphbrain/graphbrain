package com.graphbrain.db;

import org.iq80.leveldb.*;
import static org.fusesource.leveldbjni.JniDBFactory.*;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class LevelDbBackend implements Backend  {
	private DB db;
	
	public LevelDbBackend() {
		Options options = new Options();
    	options.createIfMissing(true);
    	try {
    		db = factory.open(new File("gbdb"), options);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	public Vertex get(String id, VertexType type) {
		String realId = typeToChar(type) + id;
		String value = asString(db.get(bytes(realId)));
		return decodeVertex(realId, value);
	}
	
	private Vertex decodeVertex(String realId, String value) {
		char typeChar = realId.charAt(0);
		String id = realId.substring(1);
		
		switch(typeChar) {
		case 'T':
			return new TextNode(id, stringToMap(value));
		case 'H':
			return new URLNode(id, stringToMap(value));
		case 'Y':
			return new EdgeType(id, stringToMap(value));
		case 'U':
			return new UserNode(id, stringToMap(value));
		case 'R':
			return new RuleNode(id, stringToMap(value));
		case 'S':
			return new SourceNode(id, stringToMap(value));
		case 'C':
			return new ContextNode(id, stringToMap(value));
		default:
			return null;
		}
	}
	
	private char typeToChar(VertexType type) {
		switch(type) {
		case Text:
			return 'T';
		case URL:
			return 'H';
		case EdgeType:
			return 'Y';
		case User:
			return 'U';
		case Rule:
			return 'R';
		case Source:
			return 'S';
		case Context:
			return 'C';
		default:
			return '?';
		}
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
		String realId = typeToChar(vertex.type()) + vertex.getId();
		String value = mapToString(vertex.toMap());
		db.put(bytes(realId), bytes(value));
		return vertex;
	}
	
	public Vertex update(Vertex vertex) {
		return put(vertex);
	}
	
	public void remove(Vertex vertex) {
		String realId = typeToChar(vertex.type()) + vertex.getId();
		db.delete(bytes(realId));
	}
	
	public void associateEmailToUsername(String email, String username) {
		db.put(bytes(ID.emailId(email)), bytes(username));
	}
	
	public String usernameByEmail(String email) {
		return asString(db.get(bytes(ID.emailId(email))));
	}
	
	public List<Vertex> listByType(VertexType type) {
		List<Vertex> res = new LinkedList<Vertex>();
		
		char startChar = typeToChar(type);
		char endChar = startChar++;
		String startStr = "" + startChar;
		String endStr = "" + endChar;
		DBIterator iterator = db.iterator();
		try {
			iterator.seek(bytes(startStr));
		    String id = startStr;
		    String value;
		    
		    while (iterator.hasNext() && id.compareTo(endStr) < 0) {
		    	Entry<byte[], byte[]> entry = iterator.next();
		    	id = asString(entry.getKey());
    			value = asString(entry.getValue());
    			res.add(decodeVertex(id, value));
		    }
		}
		finally {
			try {
				iterator.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
    public static void main( String[] args ) {
    	Options options = new Options();
    	options.createIfMissing(true);
    	try {
    		DB db = factory.open(new File("example"), options);
    		try {
    			
    			System.out.println("=> " + db.get(bytes("xpto666")));
    			
    			/*
    			for (int i = 0; i < 1000000; i++) {
    				String key = UUID.randomUUID().toString();
    				String value = UUID.randomUUID().toString();
    				db.put(bytes(key), bytes(value));
    			}*/
    			
    			/*
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
    			*/
    			
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
