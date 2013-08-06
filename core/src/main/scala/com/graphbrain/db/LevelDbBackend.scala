package com.graphbrain.core

//import org.apache.commons.lang3.ArrayUtils
import org.iq80.leveldb._
import org.fusesource.leveldbjni.JniDBFactory._
import java.io._
import java.net.URLDecoder
import java.net.URLEncoder

import com.graphbrain.core.Permutations.*;


public class LevelDbBackend implements Backend  {
	static final String EDGE_PREFIX = "#";
	
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
	
	public void close() {
		try {
			db.close();
		}
		catch (IOException e) {
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
		case 'E':
			return new Edge(id, stringToMap(value));
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
		case Edge:
			return 'E';
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
		
		if (vertex.type() == VertexType.Edge) {
			writeEdgePermutations((Edge)vertex);
		}
		
		return vertex;
	}
	
	public Vertex update(Vertex vertex) {
		return put(vertex);
	}
	
	public void remove(Vertex vertex) {
		String realId = typeToChar(vertex.type()) + vertex.getId();
		db.delete(bytes(realId));
		
		if (vertex.type() == VertexType.Edge) {
			removeEdgePermutations((Edge)vertex);
		}
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
	
	private static String strPlusOne(String str) {
		char lastChar = str.charAt(str.length() - 1);
		return str.substring(0, str.length() - 1) + (++lastChar);
	}
	
	public Set<Edge> edges(Vertex center) {
		Set<Edge> res = new HashSet<Edge>();
		
		String startStr = EDGE_PREFIX + center.getId();
		String endStr = strPlusOne(startStr);
		DBIterator iterator = db.iterator();
		try {
			iterator.seek(bytes(startStr));
		    String key = startStr;
		    //String value;
		    
		    while (iterator.hasNext() && key.compareTo(endStr) < 0) {
		    	Entry<byte[], byte[]> entry = iterator.next();
		    	key = asString(entry.getKey());
    			//value = asString(entry.getValue());
    			
    			key = key.substring(1);
    			String[] tokens = key.split(" ");
    			int perm = Integer.parseInt(tokens[tokens.length - 1]);
    			tokens = ArrayUtils.remove(tokens, tokens.length - 1);
    			tokens = strArrayUnpermutate(tokens, perm);
    			Edge edge = new Edge(tokens);
    			res.add(edge);
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
	
	private void writeEdgePermutations(Edge edge) {
		int count = edge.getIds().length;
		int perms = permutations(count);

		for (int i = 0; i < perms; i++) {
			String[] ids = strArrayPermutation(edge.getIds(), i);
			String permId = EDGE_PREFIX + Edge.idFromParticipants(ids) + " " + i;
			String value = "";
			db.put(bytes(permId), bytes(value));
		}
	}
	
	private void removeEdgePermutations(Edge edge) {
		int count = edge.getIds().length;
		int perms = permutations(count);

		for (int i = 0; i < perms; i++) {
			String[] ids = strArrayPermutation(edge.getIds(), i);
			String permId = EDGE_PREFIX + Edge.idFromParticipants(ids) + " " + i;
			db.delete(bytes(permId));
		}
	}
	
    public static void main(String[] args) {
    	String test = "1/hank_hill";
    	System.out.println(strPlusOne(test));
    	/*
    	Edge edge = new Edge("rel/1/sells 1/hank_hill 1/propane");
    	LevelDbBackend be = new LevelDbBackend();
    	be.writeEdgePermutations(edge);
    	*/
    }
}