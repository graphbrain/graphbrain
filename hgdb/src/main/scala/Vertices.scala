package com.graphbrain.hgdb


abstract class Vertex {
  val id: String
  val edges: Set[String]
  val vtype: String
  val extra: Int
  
  def toMap: Map[String, Any]
  protected def toMapBase: Map[String, Any] = Map(("vtype" -> vtype), ("edges" -> iter2str(edges)), ("extra" -> extra))
  
  def setEdges(newEdges: Set[String]): Vertex
  def setExtra(newExtra: Int): Vertex

  protected def iter2str(iter: Iterable[String]) = {
    if (iter.size == 0)
      ""
    else
      (for (str <- iter)
        yield str.replace("$", "$1").replace(",", "$2")).reduceLeft(_ + "," + _)
  }

  override def toString: String = id
}

case class Edge(id: String="", etype: String="", edges: Set[String]=Set[String](), extra: Int = -1) extends Vertex {
  override val vtype: String = "edg"

  def this(etype:String, participants: Array[String]) =
    this((List[String](etype) ++ participants).reduceLeft(_ + " " + _),
      etype, Set[String]())

  override def toMap: Map[String, Any] = toMapBase ++ Map(("etype" -> etype))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)

  def participantIds = Edge.participantIds(id)
}

object Edge {
  def participantIds(edgeId: String) = {
    val tokens = edgeId.split(' ')
    for (i <- 1 until tokens.length) yield tokens(i)
  }

  def edgeType(edgeId: String) = {
    val tokens = edgeId.split(' ')
    if (tokens.length > 0)
      tokens(0)
    else
      ""
  }

  def valid(edgeId: String) = participantIds(edgeId).size > 1
}

case class ExtraEdges(id: String="", edges: Set[String]=Set[String](), extra: Int = -1) extends Vertex {
  override val vtype: String = "ext"

  override def toMap: Map[String, Any] = toMapBase

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
} 

case class EdgeType(id: String="", label: String="", roles: List[String]=List[String](),
  rolen: String="", edges: Set[String]=Set[String](), extra: Int = -1) extends Vertex {

  override val vtype: String = "edgt"
  
  override def toMap: Map[String, Any] = toMapBase ++
    Map(("label" -> label), ("roles" -> iter2str(roles)), ("rolen" -> rolen))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
}


case class Brain(id: String="", name: String="", access: String = "public", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "brn"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("name" -> name), ("access" -> access))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)

  override def toString: String = name
}


case class TextNode(id: String="", text: String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "txt"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("text" -> text))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)

  override def toString: String = text
}


case class URLNode(id: String="", url: String="", title: String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "url"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("url" -> url), ("title" -> title))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
  def setTitle(newTitle: String) = copy(title=newTitle)
}


case class ImageNode(id: String="", url: String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "img"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("url" -> url))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
}


case class VideoNode(id: String="", url: String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "vid"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("url" -> url))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
}


case class SVGNode(id: String="", svg:String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "svg"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("svg" -> svg))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
}


case class SourceNode(id: String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "src"

  override def toMap: Map[String, Any] = toMapBase

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
}


case class UserNode(id: String="", username: String="", name: String="", email: String="",
  pwdhash: String="", role: String="", session: String="", creationTs: Long= -1, sessionTs: Long= -1,
  lastSeen: Long= -1, brains: Set[String]=Set[String](), edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  
  override val vtype: String = "usr"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("username" -> username), ("name" -> name),
    ("email" -> email), ("pwdhash" -> pwdhash), ("role" -> role), ("session" -> session), ("creationTs" -> creationTs)
    , ("sessionTs" -> sessionTs), ("lastSeen" -> lastSeen), ("brains" -> iter2str(brains)))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
  def setBrains(newBrains: Set[String]) = copy(brains=newBrains)
}

case class UserEmailNode(id: String="", username: String="", email: String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "usre"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("username" -> username), ("email" -> email))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
}