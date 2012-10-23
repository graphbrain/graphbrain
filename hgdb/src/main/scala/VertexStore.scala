package com.graphbrain.hgdb

import scala.collection.mutable.{Set => MSet}

import java.util.Arrays

import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater
import me.prettyprint.cassandra.service.ColumnSliceIterator
import me.prettyprint.cassandra.serializers.StringSerializer
import me.prettyprint.cassandra.serializers.IntegerSerializer
import me.prettyprint.cassandra.serializers.CompositeSerializer
import me.prettyprint.hector.api.beans.Composite

import IdFamily._

import com.graphbrain.utils.SimpleLog

class VertexStore(keyspaceName: String="gb", clusterName: String="hgdb", ip: String="localhost", port: Int=9160) extends SimpleLog {
  val backend = new CassandraBackend(clusterName, keyspaceName, ip, port)

  def get(id: String): Vertex = {
    ldebug("get vertex: " + id)
    IdFamily.family(id) match {
      case Global => {
        val res = backend.tpGlobal.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val text = res.getString("text")
        val summary = res.getString("summary")
        TextNode(this, ID.namespace(id), text, summary)
      }
      case UserSpace => {
        val res = backend.tpUserSpace.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val text = res.getString("text")
        val summary = res.getString("summary")
        TextNode(this, ID.namespace(id), text, summary)
      }
      case User => {
        val res = backend.tpUser.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val username = res.getString("username")
        val name = res.getString("name")
        val email = res.getString("email")
        val pwdhash = res.getString("pwdhash")
        val role = res.getString("role")
        val session = res.getString("session")
        val sessionTs = res.getLong("sessionTs")
        val lastSeen = res.getLong("lastSeen")
        val summary = res.getString("summary")
        UserNode(this, id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, summary)
      }
      case EType => {
        val res = backend.tpEdgeType.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val label = res.getString("label")
        EdgeType(this, id, label)
      }
      case URL => {
        val res = backend.tpGlobal.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val url = res.getString("url")
        val userId = ID.ownerId(id)
        val title = res.getString("title")
        URLNode(this, url, userId, title)
      }
      case UserURL => {
        val res = backend.tpUserSpace.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val url = res.getString("url")
        val userId = ID.ownerId(id)
        val title = res.getString("title")
        URLNode(this, url, userId, title)
      }
      case Rule => {
        val res = backend.tpGlobal.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val rule = res.getString("rule")
        RuleNode(this, id, rule)
      }
      case Source => {
        val res = backend.tpGlobal.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        SourceNode(this, id)
      }
    }
  }


  def put(vertex: Vertex): Vertex = vertex.put()


  def onPut(vertex: Vertex) = {}


  def update(vertex: Vertex): Vertex = put(vertex)


  def exists(id: String): Boolean = {
    try {
      val v = get(id)
    }
    catch {
      case _ => {
        ldebug(id + " does not exist")
        return false
      }
    }
    ldebug(id + " exists")
    true
  }


  def remove(vertex: Vertex): Vertex = {
    ldebug("remove" + vertex.id)

    val id = vertex.id
    
    // remove associated degree
    delDegree(id)

    // remove associated edges
    val nEdges = neighborEdges(id)
    for (e <- nEdges) delrel(e)

    vertex match {
      case t: TextNode => {
        val template = if (ID.isInUserSpace(id)) backend.tpUserSpace else backend.tpGlobal
        template.deleteRow(id)
      }
      case et: EdgeType => {
        delInstances(id)
        backend.tpEdgeType.deleteRow(id)
      }
      case r: RuleNode => backend.tpGlobal.deleteRow(id)
      case u: URLNode => {
        val template = if (ID.isInUserSpace(id)) backend.tpUserSpace else backend.tpGlobal
        template.deleteRow(id)
      }
      case s: SourceNode => backend.tpGlobal.deleteRow(id)
      case u: UserNode => backend.tpUser.deleteRow(id)
    }

    vertex
  }


  def addrel(edge: Edge): Edge = {
    ldebug("addrel " + edge)

    incInstances(edge.edgeType)
    for (i <- 0 until edge.participantIds.size) {
      val p = edge.participantIds(i)
      addEdgeEntry(p, edge)
      incVertexEdgeType(p, edge.edgeType, i)
      incDegree(p)
    }

    edge
  }


  def addrel(edgeType: String, participants: List[String]): Edge = addrel(Edge(edgeType, participants))


  def delrel(edge: Edge): Unit = {
    ldebug("delrel " + edge)

    decInstances(edge.edgeType)
    for (i <- 0 until edge.participantIds.size) {
      val p = edge.participantIds(i)
      delEdgeEntry(p, edge)
      decVertexEdgeType(p, edge.edgeType, i)
      decDegree(p)
    }
  }


  def delrel(edgeType: String, participants: List[String]): Unit = delrel(Edge(edgeType, participants))


  def neighborEdges(nodeId: String): Set[Edge] = {
    ldebug("neighborEdges " + nodeId)

    try {
      val eset = MSet[Edge]()

      val query = HFactory.createSliceQuery(backend.ksp, StringSerializer.get(),
                                                      new CompositeSerializer(), StringSerializer.get())
      query.setKey(nodeId)
      query.setColumnFamily("edges")

      val minPos: java.lang.Integer = Integer.MIN_VALUE
      val maxPos: java.lang.Integer = Integer.MAX_VALUE

      val start = new Composite()
      start.addComponent(String.valueOf(Character.MIN_VALUE), StringSerializer.get())
      start.addComponent(minPos, IntegerSerializer.get())
      start.addComponent(String.valueOf(Character.MIN_VALUE), StringSerializer.get())
      start.addComponent(String.valueOf(Character.MIN_VALUE), StringSerializer.get())
      start.addComponent(String.valueOf(Character.MIN_VALUE), StringSerializer.get())
        
      val finish = new Composite()
      finish.addComponent(String.valueOf(Character.MAX_VALUE), StringSerializer.get())
      finish.addComponent(maxPos, IntegerSerializer.get())
      finish.addComponent(String.valueOf(Character.MAX_VALUE), StringSerializer.get())
      finish.addComponent(String.valueOf(Character.MAX_VALUE), StringSerializer.get())
      finish.addComponent(String.valueOf(Character.MAX_VALUE), StringSerializer.get())

      val it = new ColumnSliceIterator[String, Composite, String](query, start, finish, false)
      while (it.hasNext()) {
        val column = it.next()
        val edgeType = column.getName().get(0, StringSerializer.get())
        val pos = column.getName().get(1, IntegerSerializer.get())
        val node1 = column.getName().get(2, StringSerializer.get())
        val node2 = column.getName().get(3, StringSerializer.get())
        val nodeN = column.getName().get(4, StringSerializer.get())
        val edge = Edge.fromEdgeEntry(nodeId, edgeType, pos, node1, node2, nodeN)
        eset += edge
      }

      eset.toSet
    }
    catch {
      case e => Set[Edge]()
    }
  }


  def nodesFromEdgeSet(edgeSet: Set[Edge]): Set[String] = {
    val nset = MSet[String]()

    for (e <- edgeSet) {
      for (pid <- e.participantIds)
        nset += pid
    }

    nset.toSet
  }


  def neighbors(nodeId: String): Set[String] = {
    ldebug("neighbors " + nodeId)

    val nedges = neighborEdges(nodeId)
    nodesFromEdgeSet(nedges) + nodeId
  }


  private def edgeEntryKey(nodeId: String, edge: Edge) = {
    ldebug("edgeEntryKey nodeId: " + nodeId + "; edge: " + edge)

    val pos: java.lang.Integer = edge.participantIds.indexOf(nodeId)    
    // TODO: throw exception if pos == -1

    val participants = edge.participantIds.filterNot(x => x == nodeId)
    val numberOfParticipants = participants.length

    val node2 = if (numberOfParticipants > 1) participants(1) else ""

    val extraNodes =
      if (numberOfParticipants > 2)
        participants.slice(2, numberOfParticipants).reduceLeft(_ + " " + _)
      else
        ""

    val c = new Composite()
    c.addComponent(edge.edgeType, StringSerializer.get())
    c.addComponent(pos, IntegerSerializer.get())
    c.addComponent(participants(0), StringSerializer.get())
    c.addComponent(node2, StringSerializer.get())
    c.addComponent(extraNodes, StringSerializer.get())

    c
  }


  private def addEdgeEntry(nodeId: String, edge: Edge) = {
    ldebug("addEdgeEntry nodeId: " + nodeId + "; edge: " + edge)

    val colKey = edgeEntryKey(nodeId, edge)

    val col = HFactory.createColumn(colKey, "", new CompositeSerializer(), StringSerializer.get())
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.addInsertion(nodeId, "edges", col)
    mutator.execute()
  }


  private def delEdgeEntry(nodeId: String, edge: Edge) = {
    ldebug("delEdgeEntry nodeId: " + nodeId + "; edge: " + edge)

    val colKey = edgeEntryKey(nodeId, edge)

    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.addDeletion(nodeId, "edges", colKey, new CompositeSerializer())
    mutator.execute()
  }


  private def incVertexEdgeType(nodeId: String, edgeType: String, position: Int) = {
    ldebug("incVertexEdgeType nodeId: " + nodeId + "; edgeType: " + edgeType + "; position: " + position)

    val relId = ID.relationshipId(edgeType, position)

    val col = HFactory.createCounterColumn(relId, 1L, StringSerializer.get())
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.insertCounter(nodeId, "vertexedgetype", col)
    mutator.execute()
  }


  private def decVertexEdgeType(nodeId: String, edgeType: String, position: Int): Boolean = {
    ldebug("decVertexEdgeType nodeId: " + nodeId + "; edgeType: " + edgeType + "; position: " + position)

    val relId = ID.relationshipId(edgeType, position)

    // get current count
    val query = HFactory.createCounterColumnQuery(backend.ksp, StringSerializer.get(), StringSerializer.get())
    query.setColumnFamily("vertexedgetype").setKey(nodeId).setName(relId)
    val counter = query.execute().get()
    if (counter == null) return false
    val count = counter.getValue()

    // decrement or delete
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    if (count <= 1) {
      mutator.addDeletion(nodeId, "vertexedgetype", relId, StringSerializer.get())
      mutator.execute()
    }
    else {
      val col = HFactory.createCounterColumn(relId, -1L, StringSerializer.get())
      mutator.insertCounter(nodeId, "vertexedgetype", col)
      mutator.execute()
    }

    true
  }

  private def incDegree(vertexId: String) = {
    ldebug("incDegree " + vertexId)

    val col = HFactory.createCounterColumn("degree", 1L, StringSerializer.get())
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.insertCounter(vertexId, "degrees", col)
    mutator.execute()
  }


  private def decDegree(vertexId: String) = {
    ldebug("decDegree " + vertexId)

    val col = HFactory.createCounterColumn("degree", -1L, StringSerializer.get())
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.insertCounter(vertexId, "degrees", col)
    mutator.execute()
  }


  private def delDegree(vertexId: String) = {
    ldebug("delDegree " + vertexId)

    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.addDeletion(vertexId, "degrees", "degree", StringSerializer.get())
    mutator.execute()
  }

  private def incInstances(edgeType: String) = {
    ldebug("incInstances " + edgeType)

    val col = HFactory.createCounterColumn("instances", 1L, StringSerializer.get())
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.insertCounter(edgeType, "instances", col)
    mutator.execute()
  }


  private def decInstances(edgeType: String) = {
    ldebug("decInstances " + edgeType)

    val col = HFactory.createCounterColumn("instances", -1L, StringSerializer.get())
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.insertCounter(edgeType, "instances", col)
    mutator.execute()
  }


  private def delInstances(edgeType: String) = {
    ldebug("delInstances " + edgeType)

    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.addDeletion(edgeType, "instances", "instances", StringSerializer.get())
    mutator.execute()
  }


  def relExistsOnVertex(id: String, edge: Edge): Boolean = {
    ldebug("relExistsOnVertex id: " + id + "; edge: " + edge)

    val ckey = edgeEntryKey(id, edge)
    val res = backend.tpEdges.queryColumns(id, Arrays.asList(ckey))
    res.hasResults()
  }

  
  def relExists(edge: Edge): Boolean = {
    ldebug("relExists: " + edge)

    val id = edge.participantIds(0)
    relExistsOnVertex(id, edge)
  }


  def getOrNull(id: String): Vertex = {
    ldebug("getOrNull: " + id)

    try {
      get(id)
    }
    catch {
      case e: KeyNotFound => {
        ldebug("KeyNotFound " + id)
        null
      }
    }
  }

  def getEdgeType(id: String): EdgeType = {
    ldebug("getEdgeType: " + id)
  	get(id) match {
  		case x: EdgeType => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected EdgeType)")
  	}
  }

  def getTextNode(id: String): TextNode = {
  	ldebug("getTextNode: " + id)
    get(id) match {
  		case x: TextNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected TextNode)")
  	}
  }

  def getURLNode(id: String): URLNode = {
    ldebug("getURLNode: " + id)
  	get(id) match {
  		case x: URLNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected URLNode)")
  	}
  }

  def getSourceNode(id: String): SourceNode = {
  	ldebug("getSourceNode: " + id)
    get(id) match {
  		case x: SourceNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected SourceNode)")
  	}
  }

  def getUserNode(id: String): UserNode = {
    ldebug("getUserNode: " + id)
    get(id) match {
      case x: UserNode => x
      case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected UserNode)")
    }
  }


  def createAndConnectVertices(edgeType: String, participants: Array[Vertex]) = {
    ldebug("createAndConnectVertices edgeType: " + edgeType + "; participants: " + participants)
    for (v <- participants) {
      if (!exists(v.id)) {
        put(v)
      }
    }

    val ids = for (v <- participants) yield v.id
    addrel(edgeType.replace(" ", "_"), ids.toList)
  }

  def createTextNode(namespace: String="", text: String="", summary: String="") = TextNode(this, namespace, text, summary)

  def createEdgeType(id: String="", label: String="") = EdgeType(this, id, label)

  def createSourceNode(id: String="") = SourceNode(this, id)

  def createURLNode(url: String="", userId: String = "", title: String="") = URLNode(this, url, userId, title)

  def createUserNode(id: String="", username: String="", name: String="",
  email: String="", pwdhash: String="", role: String="", session: String="",
  sessionTs: Long= -1, lastSeen: Long= -1, summary: String="") = UserNode(this, id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, summary)

  def createRuleNode(store: VertexStore, id: String="", rule: String="") = RuleNode(this, id, rule)
}


object VertexStore {
  def apply(clusterName: String, keyspaceName: String) = new VertexStore(clusterName, keyspaceName)

  def main(args : Array[String]) : Unit = {
    val store = new VertexStore()

    val edges = store.neighborEdges("user/telmo_menezes")
    for (e <- edges) println(e)

    var snode = store.getUserNode("user/telmo_menezes").updateSummary
    println(snode.summary)
  }
}
