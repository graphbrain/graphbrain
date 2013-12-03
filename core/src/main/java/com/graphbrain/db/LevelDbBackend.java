package com.graphbrain.db;

import org.iq80.leveldb.*;
import static org.fusesource.leveldbjni.JniDBFactory.*;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import static com.graphbrain.utils.Permutations.*;


public class LevelDbBackend implements Backend {

    private static DB db = null;

    public static char EDGE_PREFIX = '#';
    public static char GLOBAL_LINK_PREFIX = '*';

    public LevelDbBackend(String name) {

        if (db == null) {
            try {
                Options options = new Options();
                options.createIfMissing(true);
                db = factory.open(new File(name), options);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public LevelDbBackend() {
        this("dbnode");
    }
	
	public void close() {
        try {
            db.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private char typeToChar(VertexType vtype) {
        switch(vtype) {
            case Edge: return 'E';
            case Entity: return 'X';
            case URL: return 'H';
            case EdgeType: return 'Y';
            case User: return 'U';
            case Prog: return 'P';
            case Text: return 'T';
            default: return '?';
        }
    }

    private Map<String, String> stringToMap(String input) {

        HashMap<String, String> map = new HashMap<String, String>();

        String[] nameValuePairs = input.split("&");

        for (String nameValuePair : nameValuePairs) {
            String[] nameValue = nameValuePair.split("=");

            String key = "";
            String value = "";

            try {
                key = URLDecoder.decode(nameValue[0], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (nameValue.length > 1) {
                try {
                    value = URLDecoder.decode(nameValue[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            if (!key.isEmpty()) {
                map.put(key, value);
            }
        }

        return map;
    }

    private Vertex decodeVertex(String realId, String value) {
        char typeChar = realId.charAt(0);
        String id = realId.substring(1);

        switch(typeChar) {
            case 'E': return new Edge(id, stringToMap(value));
            case 'X': return new EntityNode(id, stringToMap(value));
            case 'H': return new URLNode(id, stringToMap(value));
            case 'Y': return new EdgeType(id, stringToMap(value));
            case 'U': return new UserNode(id, stringToMap(value));
            case 'P': return new ProgNode(id, stringToMap(value));
            case 'T': return new TextNode(id, stringToMap(value));
            default: return null;
        }
    }

	public Vertex get(String id, VertexType vtype) {
        //logger.debug(s"get: $id")
		String realId = typeToChar(vtype) + id;
        byte[] raw = db.get(bytes(realId));
        if (raw == null) {
            return null;
        }
        else {
		    String value = asString(raw);
		    return decodeVertex(realId, value);
        }
	}

    private char typeToChar(Vertex v) {
        switch(v.type()) {
            case Edge: return 'E';
            case Entity: return 'X';
            case URL: return 'H';
            case EdgeType: return 'Y';
            case User: return 'U';
            case Prog: return 'P';
            case Text: return 'T';
            default: return '?';
        }
    }
	
	private String mapToString(Map<String, String> map) {
		StringBuilder stringBuilder = new StringBuilder(50);

        boolean first = true;
		for (String k : map.keySet()) {
			if (first)
                first = false;
            else
				stringBuilder.append("&");

            String key = "";
		    String value = map.get(k);

            if (k != null) {
                try {
                    key = URLEncoder.encode(key, "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            if (value == null)
                value = "";

            stringBuilder.append(key);
		    stringBuilder.append("=");
		    stringBuilder.append(value);
		}

		return stringBuilder.toString();
	}
	
	public Vertex put(Vertex vertex) {
		String realId = typeToChar(vertex) + vertex.id;
		String value = mapToString(vertex.toMap());
		db.put(bytes(realId), bytes(value));

        if (vertex.type() == VertexType.Edge) {
            Edge e = (Edge)vertex;
            writeEdgePermutations(e);
        }
		
		return vertex;
	}
	
	public Vertex update(Vertex vertex) {
        return put(vertex);
    }
	
	public void remove(Vertex vertex) {
		String realId = typeToChar(vertex) + vertex.id;
		db.delete(bytes(realId));

        if (vertex.type() == VertexType.Edge) {
            Edge e = (Edge)vertex;
            removeEdgePermutations(e);
        }
	}
	
	public void associateEmailToUsername(String email, String username) {
		db.put(bytes(ID.emailId(email)), bytes(username));
    }
	
	public String usernameByEmail(String email) {
		return asString(db.get(bytes(ID.emailId(email))));
    }

	public List<Vertex> listByType(VertexType vtype) {
		List<Vertex> res = new LinkedList<Vertex>();
		
		char startChar = typeToChar(vtype);
		char endChar = (char) (startChar + 1);
		String startStr = "" + startChar;
		String endStr = "" + endChar;
		DBIterator iterator = db.iterator();
		try {
			iterator.seek(bytes(startStr));
		    String id = startStr;
		    String value;
		    
		    while (iterator.hasNext() && id.compareTo(endStr) < 0) {
		    	Map.Entry<byte[], byte[]> entry = iterator.next();
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

    public Set<Edge> edges(Edge pattern) {
        //logger.debug(s"edges pattern: pattern")
        Set<Edge> res = new HashSet<Edge>();

        StringBuilder sb = new StringBuilder(100);
        sb.append(EDGE_PREFIX);

        boolean first = true;
        for (String p : pattern.getIds()) {
            if (!p.equals("*")) {
                if (first)
                    first = false;
                else
                    sb.append(" ");
                sb.append(p);
            }
        }

        String startStr = sb.toString();
        String endStr = LevelDbBackend.strPlusOne(startStr);
        DBIterator iterator = db.iterator();
        try {
            iterator.seek(bytes(startStr));
            String key = startStr;

            while (iterator.hasNext() && key.compareTo(endStr) < 0) {
                Map.Entry<byte[], byte[]> entry = iterator.next();
                key = asString(entry.getKey());

                if (key.compareTo(endStr) < 0) {
                    String key2 = key.substring(1);
                    String[] tokens = key2.split(" ");
                    int perm = Integer.parseInt(tokens[tokens.length - 1]);
                    tokens = Arrays.copyOfRange(tokens, 0, tokens.length - 1);
                    tokens = strArrayUnpermutate(tokens, perm);
                    Edge edge = Edge.fromParticipants(tokens);

                    if (edge.matches(pattern))
                        res.add(edge);
                }
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

    public Set<Edge> edges(Vertex center) {
        //logger.debug(s"edges center: $center")
        Set<Edge> res = new HashSet<Edge>();

        if (center == null)
            return res;

        String startStr = EDGE_PREFIX + center.id + " ";
        String endStr = LevelDbBackend.strPlusOne(startStr);
        DBIterator iterator = db.iterator();

        try {
            iterator.seek(bytes(startStr));
            String key = startStr;

            while (iterator.hasNext() && key.compareTo(endStr) < 0) {
                Map.Entry<byte[], byte[]> entry = iterator.next();
                key = asString(entry.getKey());

                if (key.compareTo(endStr) < 0) {
                    String key2 = key.substring(1);
                    String[] tokens = key2.split(" ");
                    int perm = Integer.parseInt(tokens[tokens.length - 1]);
                    tokens = Arrays.copyOfRange(tokens, 0, tokens.length - 1);
                    tokens = strArrayUnpermutate(tokens, perm);
                    Edge edge = Edge.fromParticipants(tokens);
                    res.add(edge);
                }
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
        //logger.debug(s"writeEdgePermutations: $edge")
		int count = edge.getIds().length;
		int perms = permutations(count);

        for (int i = 0; i < perms; i++) {
            String[] ids = strArrayPermutation(edge.getIds(), i);
            String permId = EDGE_PREFIX + Edge.fromParticipants(ids).id + " " + i;
			String value = "";
			db.put(bytes(permId), bytes(value));
		}
	}
	
	private void removeEdgePermutations(Edge edge) {
        //logger.debug(s"removeEdgePermutations: $edge")
		int count = edge.getIds().length;
		int perms = permutations(count);

        for (int i = 0; i < perms; i++) {
			String[] ids = strArrayPermutation(edge.getIds(), i);
			String permId = EDGE_PREFIX + Edge.fromParticipants(ids).id + " " + i;
			db.delete(bytes(permId));
		}
	}

    public void addLinkToGlobal(String globalId, String userId) {
        String permId = GLOBAL_LINK_PREFIX + globalId + " " + userId;
        String value = "";
        db.put(bytes(permId), bytes(value));
    }

    public void removeLinkToGlobal(String globalId, String userId) {
        String permId = GLOBAL_LINK_PREFIX + globalId + " " + userId;
        db.delete(bytes(permId));
    }

    public Set<String> alts(String globalId) {
        Set<String> res = new HashSet<String>();

        String startStr = GLOBAL_LINK_PREFIX + globalId;
        String endStr = LevelDbBackend.strPlusOne(startStr);
        DBIterator iterator = db.iterator();
        try {
            iterator.seek(bytes(startStr));
            String key = startStr;

            while (iterator.hasNext() && key.compareTo(endStr) < 0) {
                Map.Entry<byte[], byte[]> entry = iterator.next();
                key = asString(entry.getKey());

                key = key.substring(1);
                String[] tokens = key.split(" ");
                if (tokens.length > 1) {
                    String id = tokens[1];
                    res.add(id);
                }
                /*
                else {
                    //logger.warn(s"This should not happen: $key")
                }*/
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

    public static String strPlusOne(String str) {
        char lastChar = str.charAt(str.length() - 1);
        return str.substring(0, str.length() - 1) + ((char)(lastChar + 1));
    }
}