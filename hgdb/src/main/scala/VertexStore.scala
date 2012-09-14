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


class VertexStore(clusterName: String, keyspaceName: String, ip: String="localhost", port: Int=9160) {
  val backend = new CassandraBackend(clusterName, keyspaceName, ip, port)

  def get(id: String): Vertex = {
    IdFamily.family(id) match {
      case Global => {
        val res = backend.tpGlobal.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val degree = res.getLong("degree")
        val text = res.getString("text")
        TextNode(ID.namespace(id), text, degree)        
      }
      case User => {
        val res = backend.tpUser.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val degree = res.getLong("degree")
        val username = res.getString("username")
        val name = res.getString("name")
        val email = res.getString("email")
        val pwdhash = res.getString("pwdhash")
        val role = res.getString("role")
        val session = res.getString("session")
        val sessionTs = res.getLong("sessionTs")
        val lastSeen = res.getLong("lastSeen")
        UserNode(id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, degree)
      }
      case UserSpace => {
        val res = backend.tpUserSpace.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val degree = res.getLong("degree")
        val text = res.getString("text")
        TextNode(ID.namespace(id), text, degree)        
      }
      case EType => {
        val res = backend.tpEdgeType.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val degree = res.getLong("degree")
        val label = res.getString("label")
        val instances = res.getLong("instances")
        EdgeType(id, label, instances, degree)
      }
      case URL => {
        val res = backend.tpGlobal.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val degree = res.getLong("degree")
        val url = res.getString("url")
        val title = res.getString("title")
        URLNode(id, url, title, degree)
      }
      case UserURL => {
        val res = backend.tpUserSpace.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val degree = res.getLong("degree")
        val url = res.getString("url")
        val title = res.getString("title")
        URLNode(id, url, title, degree)
      }
      case Rule => {
        val res = backend.tpGlobal.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val degree = res.getLong("degree")
        val rule = res.getString("rule")
        RuleNode(id, rule, degree)
      }
      case Source => {
        val res = backend.tpGlobal.queryColumns(id)
        if (!res.hasResults()) throw new KeyNotFound("vertex with key: " + id + " not found.")
        val degree = res.getLong("degree")
        SourceNode(id)
      }
    }
  }


  def rawget(id: String): String = backend.rawget(id)


  def put(vertex: Vertex): Vertex = {
    val id = vertex.id
    vertex match {
      case t: TextNode => {
        val template = if (ID.isInUserSpace(id)) backend.tpUserSpace else backend.tpGlobal
        val updater = template.createUpdater(id)
        updater.setLong("degree", t.degree)
        updater.setString("text", t.text)
        template.update(updater)
      }
      case et: EdgeType => {
        val template = backend.tpEdgeType
        val updater = template.createUpdater(id)
        updater.setLong("degree", et.degree)
        updater.setString("label", et.label)
        updater.setLong("instances", et.instances)
        template.update(updater)
      }
      case r: RuleNode => {
        val template = backend.tpGlobal
        val updater = template.createUpdater(id)
        updater.setLong("degree", r.degree)
        updater.setString("rule", r.rule)
        template.update(updater)
      }
      case u: URLNode => {
        val template = if (ID.isInUserSpace(id)) backend.tpUserSpace else backend.tpGlobal
        val updater = template.createUpdater(id)
        updater.setLong("degree", u.degree)
        updater.setString("url", u.url)
        updater.setString("title", u.title)
        template.update(updater)
      }
      case s: SourceNode => {
        val template = backend.tpGlobal
        val updater = template.createUpdater(id)
        updater.setLong("degree", s.degree)
        template.update(updater)
      }
      case u: UserNode => {
        val template = backend.tpUser
        val updater = template.createUpdater(id)
        updater.setLong("degree", u.degree)
        updater.setString("username", u.username)
        updater.setString("name", u.name)
        updater.setString("email", u.email)
        updater.setString("pwdhash", u.pwdhash)
        updater.setString("role", u.role)
        updater.setString("session", u.session)
        updater.setLong("sessionTs", u.sessionTs)
        updater.setLong("lastSeen", u.lastSeen)
        template.update(updater)
      }
    }
    vertex
  }


  def update(vertex: Vertex): Vertex = put(vertex)


  def exists(id: String): Boolean = {
    try {
      val v = get(id)
    }
    catch {
      case _ => return false
    }
    true
  }


  def remove(vertex: Vertex): Vertex = {
    val id = vertex.id
    
    // TODO: remove other associated stuff

    // remove associated edges
    val nEdges = neighborEdges(id)
    for (e <- nEdges) delrel(e)

    vertex match {
      case t: TextNode => {
        val template = if (ID.isInUserSpace(id)) backend.tpUserSpace else backend.tpGlobal
        template.deleteRow(id)
      }
      case et: EdgeType => backend.tpEdgeType.deleteRow(id)
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


  def addrel(edge: Edge): Unit = {
    // TODO: update vertexedgetype
    
    for (p <- edge.participantIds) addEdgeEntry(p, edge)
  }


  def addrel(edgeType: String, participants: List[String]): Unit = addrel(Edge(edgeType, participants))


  def delrel(edge: Edge): Unit = {
    // TODO: update vertexedgetype
    
    for (p <- edge.participantIds) delEdgeEntry(p, edge)
  }


  def delrel(edgeType: String, participants: List[String]): Unit = delrel(Edge(edgeType, participants))


  def neighborEdges(nodeId: String): Set[Edge] = {
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
        //println(edge)
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
    val nedges = neighborEdges(nodeId)
    nodesFromEdgeSet(nedges) + nodeId
  }


  private def edgeEntryKey(nodeId: String, edge: Edge) = {
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
    val colKey = edgeEntryKey(nodeId, edge)

    val col = HFactory.createColumn(colKey, "", new CompositeSerializer(), StringSerializer.get())
    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.addInsertion(nodeId, "edges", col)
    mutator.execute()
  }


  private def delEdgeEntry(nodeId: String, edge: Edge) = {
    val colKey = edgeEntryKey(nodeId, edge)

    val mutator = HFactory.createMutator(backend.ksp, StringSerializer.get())
    mutator.addDeletion(nodeId, "edges", colKey, new CompositeSerializer())
    mutator.execute()
  }


  def relExistsOnVertex(id: String, edge: Edge): Boolean = {
    val ckey = edgeEntryKey(id, edge)
    val res = backend.tpEdges.queryColumns(id, Arrays.asList(ckey))
    res.hasResults()
  }


  def relExists(edge: Edge): Boolean = {
    val id = edge.participantIds(0)
    relExistsOnVertex(id, edge)
  }


  def getOrNull(id: String): Vertex = {
    try {
      get(id)
    }
    catch {
      case e: KeyNotFound => null
    }
  }

  def getEdgeType(id: String): EdgeType = {
  	get(id) match {
  		case x: EdgeType => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected EdgeType)")
  	}
  }

  def getTextNode(id: String): TextNode = {
  	get(id) match {
  		case x: TextNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected TextNode)")
  	}
  }

  def getURLNode(id: String): URLNode = {
  	get(id) match {
  		case x: URLNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected URLNode)")
  	}
  }

  def getSourceNode(id: String): SourceNode = {
  	get(id) match {
  		case x: SourceNode => x
  		case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected SourceNode)")
  	}
  }

  def getUserNode(id: String): UserNode = {
    get(id) match {
      case x: UserNode => x
      case v: Vertex => throw WrongVertexType("on vertex: " + id + " (expected UserNode)")
    }
  }


  def createAndConnectVertices(edgeType: String, participants: Array[Vertex]) = {
    for (v <- participants) {
      if (!exists(v.id)) {
        put(v)
      }
    }

    val ids = for (v <- participants) yield v.id
    addrel(edgeType.replace(" ", "_"), ids.toList)
  }
}


object VertexStore {
  def apply(clusterName: String, keyspaceName: String) = new VertexStore(clusterName, keyspaceName)

  def main(args : Array[String]) : Unit = {
    val store = new VertexStore("experiment", "experiment")
    store.delrel("am", List("test", "hey", "pipi"))
    store.neighborEdges("test")

    val nb = store.neighbors("test")
    //for (n <- nb) println(n)

    println(store.relExistsOnVertex("test", Edge("am", List("test", "hey", "pipi"))))
  }
}
