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


case class TextNode(id: String="", text: String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "txt"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("text" -> text))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
}


case class URLNode(id: String="", url: String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "url"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("url" -> url))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
}


case class ImageNode(id: String="", url: String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "img"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("url" -> url))

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
}


case class SourceNode(id: String="", edges: Set[String]=Set[String](), extra: Int= -1) extends Vertex {
  override val vtype: String = "src"

  override def toMap: Map[String, Any] = toMapBase

  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setExtra(newExtra: Int) = copy(extra=newExtra)
}


case class ErrorVertex(message: String) extends Vertex {
  override val vtype: String = "error"

  val id = ""
  val edges = Set[String]()
  val extra = -1

  override def toMap: Map[String, Any] = null

  def setEdges(newEdges: Set[String]) = ErrorVertex(message)
  def setExtra(newExtra: Int) = ErrorVertex(message)
}