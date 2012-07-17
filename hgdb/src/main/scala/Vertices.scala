package com.graphbrain.hgdb


abstract class Vertex {
  val id: String
  val edgesets: Set[String]
  val vtype: String
  val degree: Int
  val ts: Long

  def toMap: Map[String, Any]
  protected def toMapBase: Map[String, Any] = Map(("vtype" -> vtype), ("edgesets" -> iter2str(edgesets)), ("degree" -> degree), ("ts" -> ts))
  
  def clone(newid: String): Vertex

  def setEdgeSets(newEdgeSets: Set[String]): Vertex
  def setDegree(newDegree: Int): Vertex
  def setTs(newTs: Long): Vertex

  protected def iter2str(iter: Iterable[String]) = {
    if (iter.size == 0)
      ""
    else
      (for (str <- iter)
        yield str.replace("$", "$1").replace(",", "$2")).reduceLeft(_ + "," + _)
  }

  override def toString: String = id
}

case class Edge(id: String="", etype: String="", edgesets: Set[String]=Set[String](), degree: Int=0, ts: Long=0) extends Vertex {
  override val vtype: String = "edg"

  def this(etype:String, participants: Array[String]) =
    this((List[String](etype) ++ participants).reduceLeft(_ + " " + _),
      etype, Set[String]())

  override def toMap: Map[String, Any] = toMapBase ++ Map(("etype" -> etype))

  override def clone(newid: String) = Edge(newid, etype)

  def setEdgeSets(newEdgeSets: Set[String]) = copy(edgesets=newEdgeSets)
  def setDegree(newDegree: Int) = copy(degree=newDegree)
  def setTs(newTs: Long) = copy(ts=newTs)

  def edgeType = Edge.edgeType(id)
  def participantIds = Edge.participantIds(id)
}

object Edge {
  def parts(edgeId: String) = edgeId.split(' ')

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

case class EdgeType(id: String="", label: String="", edgesets: Set[String]=Set[String](), instances: Int = 0, degree: Int=0, ts: Long=0) extends Vertex {

  override val vtype: String = "edgt"
  
  override def toMap: Map[String, Any] = toMapBase ++ Map(("label" -> label), ("instances" -> instances))

  override def clone(newid: String) = EdgeType(newid, label)

  def setEdgeSets(newEdgeSets: Set[String]) = copy(edgesets=newEdgeSets)
  def setDegree(newDegree: Int) = copy(degree=newDegree)
  def setTs(newTs: Long) = copy(ts=newTs)
  def setInstances(newInst: Int) = copy(instances=newInst)
}

case class EdgeSet(id: String="", edges: Set[String]=Set[String](), extra: Int = -1, size: Int = 0) extends Vertex {
  override val vtype: String = "edgs"
  override val edgesets: Set[String] = Set[String]()
  override val degree: Int = 0
  override val ts: Long = 0

  override def toMap: Map[String, Any] = toMapBase ++ Map(("edges" -> iter2str(edges)), ("extra" -> extra), ("size" -> size))

  override def clone(newid: String) = this

  // let's not let EdgeSets have EdgeSets - brains could explode
  def setEdgeSets(newEdgeSets: Set[String]) = this
  def setDegree(newDegree: Int) = this
  def setTs(newTs: Long) = this
  def setExtra(newExtra: Int) = copy(extra=newExtra)
  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
  def setSize(newSize: Int) = copy(size=newSize)
}

case class ExtraEdges(id: String="", edges: Set[String]=Set[String]()) extends Vertex {
  override val vtype: String = "ext"
  override val edgesets: Set[String] = Set[String]()
  override val degree: Int = 0
  override val ts: Long = 0

  override def toMap: Map[String, Any] = toMapBase ++ Map(("edges" -> iter2str(edges)))

  override def clone(newid: String) = this

  // no EdgeSets on ExtraEdges either
  def setEdgeSets(newEdgeSets: Set[String]) = this
  def setDegree(newDegree: Int) = this
  def setTs(newTs: Long) = this
  def setEdges(newEdges: Set[String]) = copy(edges=newEdges)
}

case class TextNode(id: String="", text: String="", edgesets: Set[String]=Set[String](), degree: Int=0, ts: Long=0) extends Vertex {
  override val vtype: String = "txt"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("text" -> text))

  override def clone(newid: String) = TextNode(newid, text)

  def setEdgeSets(newEdgeSets: Set[String]) = copy(edgesets=newEdgeSets)
  def setDegree(newDegree: Int) = copy(degree=newDegree)
  def setTs(newTs: Long) = copy(ts=newTs)

  override def toString: String = text
}

//To store the rule body
case class RuleNode(id: String="", rule: String="", edgesets: Set[String]=Set[String](), degree: Int=0, ts: Long=0) extends Vertex {
  override val vtype: String = "rule"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("rule" -> rule))

  override def clone(newid: String) = RuleNode(newid, rule)

  def setEdgeSets(newEdgeSets: Set[String]) = copy(edgesets=newEdgeSets)
  def setDegree(newDegree: Int) = copy(degree=newDegree)
  def setTs(newTs: Long) = copy(ts=newTs)

  override def toString: String = rule
}


case class URLNode(id: String="", url: String="", title: String="", edgesets: Set[String]=Set[String](), degree: Int=0, ts: Long=0) extends Vertex {
  override val vtype: String = "url"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("url" -> url), ("title" -> title))

  override def clone(newid: String) = URLNode(newid, url)

  def setEdgeSets(newEdgeSets: Set[String]) = copy(edgesets=newEdgeSets)
  def setDegree(newDegree: Int) = copy(degree=newDegree)
  def setTs(newTs: Long) = copy(ts=newTs)
  def setTitle(newTitle: String) = copy(title=newTitle)
}


case class SourceNode(id: String="", edgesets: Set[String]=Set[String](), degree: Int=0, ts: Long=0) extends Vertex {
  override val vtype: String = "src"

  override def toMap: Map[String, Any] = toMapBase

  override def clone(newid: String) = SourceNode(newid)

  def setEdgeSets(newEdgeSets: Set[String]) = copy(edgesets=newEdgeSets)
  def setDegree(newDegree: Int) = copy(degree=newDegree)
  def setTs(newTs: Long) = copy(ts=newTs)
}


case class UserNode(id: String="", username: String="", name: String="", email: String="",
  pwdhash: String="", role: String="", session: String="", sessionTs: Long= -1,
  lastSeen: Long= -1, edgesets: Set[String]=Set[String](), degree: Int=0, ts: Long=0) extends Vertex {
  
  override val vtype: String = "usr"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("username" -> username), ("name" -> name),
    ("email" -> email), ("pwdhash" -> pwdhash), ("role" -> role), ("session" -> session),
    ("sessionTs" -> sessionTs), ("lastSeen" -> lastSeen))

  override def clone(newid: String) = this

  def setEdgeSets(newEdgeSets: Set[String]) = copy(edgesets=newEdgeSets)
  def setDegree(newDegree: Int) = copy(degree=newDegree)
  def setTs(newTs: Long) = copy(ts=newTs)
}


case class UserEmailNode(id: String="", username: String="", email: String="", edgesets: Set[String]=Set[String](), degree: Int=0, ts: Long=0) extends Vertex {
  override val vtype: String = "usre"

  override def toMap: Map[String, Any] = toMapBase ++ Map(("username" -> username), ("email" -> email))

  override def clone(newid: String) = this

  def setEdgeSets(newEdgeSets: Set[String]) = copy(edgesets=newEdgeSets)
  def setDegree(newDegree: Int) = copy(degree=newDegree)
  def setTs(newTs: Long) = copy(ts=newTs)
}