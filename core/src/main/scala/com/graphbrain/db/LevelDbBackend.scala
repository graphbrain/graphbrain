package com.graphbrain.db

import org.iq80.leveldb._
import org.fusesource.leveldbjni.JniDBFactory._
import java.io._
import java.net.URLDecoder
import java.net.URLEncoder
import com.graphbrain.utils.Permutations._
import VertexType.VertexType


class LevelDbBackend extends Backend  {
	val EDGE_PREFIX = '#'
  val GLOBAL_LINK_PREFIX = '*'

	val options = new Options()
  options.createIfMissing(true)
  val db = factory.open(new File("dbnode"), options)
	
	def close() = {
		db.close()
	}
	
	def get(id: String, vtype: VertexType) = {
		val realId = typeToChar(vtype) + id
    val raw = db.get(bytes(realId))
    if (raw == null) {
      null
    }
    else {
		  val value = asString(raw)
		  decodeVertex(realId, value)
    }
	}
	
	private def decodeVertex(realId: String, value: String): Vertex = {
		val typeChar = realId.charAt(0)
		val id = realId.substring(1)
		
		typeChar match {
		case 'E' => new Edge(id, stringToMap(value))
		case 'T' => new TextNode(id, stringToMap(value))
		case 'H' => new URLNode(id, stringToMap(value))
		case 'Y' => new EdgeType(id, stringToMap(value))
		case 'U' => new UserNode(id, stringToMap(value))
		case 'R' => new RuleNode(id, stringToMap(value))
		case 'S' => new SourceNode(id, stringToMap(value))
		case 'C' => new ContextNode(id, stringToMap(value))
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

  private def typeToChar(v: Vertex) = {
    v match {
      case v: Edge => 'E'
      case v: TextNode => 'T'
      case v: URLNode => 'H'
      case v: EdgeType => 'Y'
      case v: UserNode => 'U'
      case v: RuleNode => 'R'
      case v: SourceNode => 'S'
      case v: ContextNode => 'C'
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

		stringBuilder.toString()
	}
	
	private def stringToMap(input: String) = {
		//val map = Map[String, String]()

		val nameValuePairs = {
      input.split("&")
    }
    (for (nameValuePair <- nameValuePairs) yield {
		  val nameValue = nameValuePair.split("=")
      URLDecoder.decode(nameValue(0), "UTF-8") -> (if (nameValue.length > 1) URLDecoder.decode(nameValue(1), "UTF-8") else "")
		}).toMap[String, String]
	}
	
	def put(vertex: Vertex) = {
		val realId = typeToChar(vertex) + vertex.id
		val value = mapToString(vertex.toMap)
		db.put(bytes(realId), bytes(value))

    vertex match {
		  case e: Edge => writeEdgePermutations(e)
      case _ =>
    }
		
		vertex
	}
	
	def update(vertex: Vertex) = put(vertex)
	
	def remove(vertex: Vertex) = {
		val realId = typeToChar(vertex) + vertex.id
		db.delete(bytes(realId))

    vertex match {
      case e: Edge => removeEdgePermutations(e)
      case _ =>
    }
	}
	
	def associateEmailToUsername(email: String, username: String) =
		db.put(bytes(ID.emailId(email)), bytes(username))
	
	def usernameByEmail(email: String) =
		asString(db.get(bytes(ID.emailId(email))))

	def listByType(vtype: VertexType) = {
		var res = List[Vertex]()
		
		val startChar = typeToChar(vtype)
		val endChar = (startChar + 1).toChar
		val startStr = "" + startChar
		val endStr = "" + endChar
		val iterator = db.iterator()
		try {
			iterator.seek(bytes(startStr))
		    var id = startStr
		    var value = ""
		    
		    while (iterator.hasNext && id.compareTo(endStr) < 0) {
		    	val entry = iterator.next()
		    	id = asString(entry.getKey)
    			value = asString(entry.getValue)
    			res = decodeVertex(id, value) :: res
		    }
		}
		finally {
			try {
				iterator.close()
			}
			catch {
				case e: IOException => e.printStackTrace()
			}
		}

    res
	}

	def edges(center: Vertex): Set[Edge] = {
		var res = Set[Edge]()
		
		val startStr = EDGE_PREFIX + center.id
		val endStr = LevelDbBackend.strPlusOne(startStr)
		val iterator = db.iterator()
		try {
			iterator.seek(bytes(startStr))
		  var key = startStr
		    
		  while (iterator.hasNext && key.compareTo(endStr) < 0) {
		    	val entry = iterator.next()
		    	key = asString(entry.getKey)
    			
    			key = key.substring(1)
    			var tokens = key.split(" ")
    			val perm = tokens(tokens.length - 1).toInt
    			tokens = tokens.dropRight(1)
    			tokens = strArrayUnpermutate(tokens, perm)
    			val edge = Edge.fromParticipants(tokens)
    			res = res + edge
		    }
		}
		finally {
			try {
				iterator.close()
      }
			catch {
				case e: IOException => e.printStackTrace()
			}
		}
		res
	}

  def edges(pattern: Edge): Set[Edge] = {
    var res = Set[Edge]()

    val sb = new StringBuilder(100)
    sb.append(EDGE_PREFIX)

    var first = true
    for (p <- pattern.ids) {
      if (p != "*") {
        if (first)
          first = false
        else
          sb.append(" ")
        sb.append(p)
      }
    }

    val startStr = sb.toString()
    val endStr = LevelDbBackend.strPlusOne(startStr)
    val iterator = db.iterator()
    try {
      iterator.seek(bytes(startStr))
      var key = startStr

      while (iterator.hasNext && key.compareTo(endStr) < 0) {
        val entry = iterator.next()
        key = asString(entry.getKey)

        key = key.substring(1)
        var tokens = key.split(" ")
        val perm = tokens(tokens.length - 1).toInt
        tokens = tokens.dropRight(1)
        tokens = strArrayUnpermutate(tokens, perm)
        val edge = Edge.fromParticipants(tokens)
        if (edge matches pattern)
          res = res + edge
      }
    }
    finally {
      try {
        iterator.close()
      }
      catch {
        case e: IOException => e.printStackTrace()
      }
    }
    res
  }
	
	def writeEdgePermutations(edge: Edge) = {
		val count = edge.ids.length
		val perms = permutations(count)

    for (i <- 0 until perms) {
			val ids = strArrayPermutation(edge.ids, i)
			val permId = EDGE_PREFIX + Edge.idFromParticipants(ids) + " " + i
			val value = ""
			db.put(bytes(permId), bytes(value))
		}
	}
	
	def removeEdgePermutations(edge: Edge) = {
		val count = edge.ids.length
		val perms = permutations(count)

    for (i <- 0 until perms) {
			val ids = strArrayPermutation(edge.ids, i)
			val permId = EDGE_PREFIX + Edge.idFromParticipants(ids) + " " + i
			db.delete(bytes(permId))
		}
	}

  def addLinkToGlobal(globalId: String, userId: String) = {
    val permId = GLOBAL_LINK_PREFIX + globalId + " " + userId
    val value = ""
    db.put(bytes(permId), bytes(value))
  }

  def removeLinkToGlobal(globalId: String, userId: String) = {
    val permId = GLOBAL_LINK_PREFIX + globalId + " " + userId
    db.delete(bytes(permId))
  }

  def alts(globalId: String): Set[String] = {
    var res = Set[String]()

    val startStr = GLOBAL_LINK_PREFIX + globalId
    val endStr = LevelDbBackend.strPlusOne(startStr)
    val iterator = db.iterator()
    try {
      iterator.seek(bytes(startStr))
      var key = startStr

      while (iterator.hasNext && key.compareTo(endStr) < 0) {
        val entry = iterator.next()
        key = asString(entry.getKey)

        key = key.substring(1)
        val tokens = key.split(" ")
        val id = tokens(1)
        res = res + id
      }
    }
    finally {
      try {
        iterator.close()
      }
      catch {
        case e: IOException => e.printStackTrace()
      }
    }
    res
  }
}

object LevelDbBackend {
  def strPlusOne(str: String) = {
    val lastChar = str.charAt(str.length - 1)
    str.substring(0, str.length - 1) + (lastChar + 1).toChar
  }

  def main(args: Array[String]) = {
    val test = "1/hank_hill"
    println(strPlusOne(test))
    /*
    Edge edge = new Edge("rel/1/sells 1/hank_hill 1/propane");
    LevelDbBackend be = new LevelDbBackend();
    be.writeEdgePermutations(edge);
    */
  }
}