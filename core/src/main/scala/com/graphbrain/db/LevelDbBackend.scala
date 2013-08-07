package com.graphbrain.db

//import org.apache.commons.lang3.ArrayUtils
import org.iq80.leveldb._
import org.fusesource.leveldbjni.JniDBFactory._
import java.io._
import java.net.URLDecoder
import java.net.URLEncoder

import com.graphbrain.utils.Permutations
import VertexType.VertexType


class LevelDbBackend extends Backend  {
	val EDGE_PREFIX = '#'
	
	var db: DB

	val options = new Options()
  options.createIfMissing(true)
  try {
    db = factory.open(new File("dbnode"), options)
  }
  catch {
    case e => e.printStackTrace()
  }
	
	def close() = {
		try {
			db.close()
		}
		catch {
			case e => e.printStackTrace()
		}
	}
	
	def get(id: String, vtype: VertexType) = {
		val realId = typeToChar(vtype) + id
		val value = asString(db.get(bytes(realId)))
		decodeVertex(realId, value)
	}
	
	private def decodeVertex(realId: String, value: String): Vertex = {
		val typeChar = realId.charAt(0)
		val id = realId.substring(1)
		
		typeChar match {
		case 'E' => Edge(id, stringToMap(value))
		case 'T' => TextNode(id, stringToMap(value))
		case 'H' => URLNode(id, stringToMap(value))
		case 'Y' => EdgeType(id, stringToMap(value))
		case 'U' => UserNode(id, stringToMap(value))
		case 'R' => RuleNode(id, stringToMap(value))
		case 'S' => SourceNode(id, stringToMap(value))
		//case 'C' => ContextNode(id, stringToMap(value))
		case _ => null
		}
	}
	
	private def typeToChar(vtype: VertexType) = {
		vtype match {
		case VertexType.Edge => 'E'
		case VertexType.Text => 'T'
		case VertexType.URL => 'H'
		case VertexType.EdgeType => 'Y'
		case VertexType.User => 'U'
		case VertexType.Rule => 'R'
		case VertexType.Source => 'S'
		case VertexType.Context => 'C'
		case _ => '?'
		}
	}
	
	private def mapToString(map: Map[String, String]) = {
		val stringBuilder = new StringBuilder()

		for (key <- map.keySet) {
			if (stringBuilder.length > 0) {
				stringBuilder.append("&")
		  }
		  val value = map(key)
		  stringBuilder.append(if (key != null) URLEncoder.encode(key, "UTF-8") else "")
		  stringBuilder.append("=")
		  stringBuilder.append(if (value != null) URLEncoder.encode(value, "UTF-8") else "")
		}

		stringBuilder.toString
	}
	
	private def stringToMap(input: String) = {
		val map = Map[String, String]()

		val nameValuePairs = input.split("&")
		for (nameValuePair <- nameValuePairs) {
			val nameValue = nameValuePair.split("=")
      yield (URLDecoder.decode(nameValue(0), "UTF-8") -> if (nameValue.length > 1) URLDecoder.decode(nameValue(1), "UTF-8") else "")
		}
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