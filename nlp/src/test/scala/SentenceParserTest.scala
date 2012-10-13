package com.graphbrain.nlp;

import org.scalatest.FunSuite;
import java.net.URLDecoder;
import scala.collection.immutable.HashMap
import scala.util.Sorting
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.OpLogging
import com.graphbrain.hgdb.TextNode
import com.graphbrain.hgdb.Edge
import com.graphbrain.hgdb.URLNode
import com.graphbrain.hgdb.UserNode
import com.graphbrain.hgdb.EdgeType
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.ID
import com.graphbrain.hgdb.UserOps
import com.graphbrain.hgdb.UserManagement




class SentenceParserTest extends FunSuite {

  val sentenceParser = new SentenceParser()
  val store = new VertexStore("gb") with UserManagement with UserOps

  val toadNodeGlobal = store.createTextNode(namespace="1", text="toad")
  val toadNodeUser = store.createTextNode(namespace="usergenerated/chihchun_chen", text="toad")
  val globalNameNode1 = store.createTextNode(namespace="1", text="ChihChun")
  val globalNameNode2 = store.createTextNode(namespace = "1", text = "Chih-Chun")

  val programmerGlobalNode = store.createTextNode(namespace = "1", text = "programmer")
  val userNode = UserNode(store = store, id="user/chihchun_chen", username="chihchun_chen", name="Chih-Chun Chen")
  val graphbrainGlobalNode = store.createTextNode(namespace = "1", text = "GraphBrain")
  val graphbrainUserNode = store.createTextNode(namespace = "usergenerated/chihchun_chen", text = "graphbrain")
  
  val sentence1 = "ChihChun is a toad"
  val sentence2 = "I am always a programmer at graphbrain"
  val sentence3 = "Chih-Chun is always a programmer at graphbrain"
  val rootNode2 = store.createTextNode(namespace = "1", text = "graphbrain")
  val sentence4 = "Chih-Chun is always a programmer at http://graphbrain.com"
 
  val isA = "rtype/1/is_a"
  val amA = "rtype/1/am_a"
  val isALemma = ID.text_id("be_a", 1)
  val isAPOS = ID.reltype_id("vbz_dt")
  val amAlwaysA_AtLemma = ID.text_id("be_always_a~at")
  val amAlwaysA_AtPOS = ID.reltype_id("vbp_rb_dt~in")
  val amAlwaysARelTypeID = "rtype/1/am_always_a~at"
  val isAlwaysARelTypeID = "rtype/1/is_always_a~at"

  test("ChihChun is a toad: Parse 2-role with root node no user"){	
    val response = sentenceParser.parseSentenceGeneral(sentence1, root = toadNodeGlobal)
    response(0) match {
      case r: GraphResponse => 
        val parses = r.hypergraphList;
        val nodes = parses(0)._1
        val relType = parses(0)._2
        println(nodes(0).id)
        println(globalNameNode1.id)
        println(nodes(1).id)
        println(relType.id)
        println(isA)
        assert(nodes(0).id == globalNameNode1.id)
        assert(nodes(1).id == toadNodeGlobal.id)
        assert(relType.id == isA)

      case _ => 
    }	
  }

  test("ChihChun is a toad: Parse 2-role with user node (user/chihchun_chen) no root") {
    val response = sentenceParser.parseSentenceGeneral(sentence1, user = Some(userNode));
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2
        println(nodes(0).id)
        println(userNode.id)
        println(nodes(1).id)
        println(toadNodeUser.id)
        println(relType.id)
        println(isA)
        assert(nodes(0).id == userNode.id)
        assert(nodes(1).id == toadNodeGlobal.id)
        assert(relType.id == isA)
      case _ =>

    }
  }

  test("Chih-Chun is a toad: Parse 2-role with user node (user/chihchun_chen) and global root (1/toad)") {

    val response = sentenceParser.parseSentenceGeneral(sentence1, root = toadNodeGlobal, user = Some(userNode));
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2
        println(nodes(0).id)
        println(userNode.id)
        println(nodes(1).id)
        println(toadNodeGlobal.id)
        println(relType.id)
  
        assert(nodes(0).id == userNode.id)
        //Should override the fact that user has node in namespace if root is the global node
        assert(nodes(1).id == toadNodeGlobal.id)
        assert(relType.id == isA)
      case _ =>
    }
  }

  test("Chih-Chun is a toad: 2-role lemmas and POS") {

    val response =  sentenceParser.parseSentenceGeneral(sentence1);
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1;
        val relTypeVertex = parses(0)._2;
        //val relTypeVertex = EdgeType(id = ID.reltype_id(relTypeText), label = relTypeText)
        relTypeVertex match {
          case r: EdgeType => 
            println(r.id)
            println(r.label)
            val reltypeLemPOS = sentenceParser.relTypeLemmaAndPOS(r, sentence1)

            val lemma = reltypeLemPOS._2._1;
            println(lemma.id)
            println(isALemma)

            val pos = reltypeLemPOS._2._2;
            println(pos.id)
            println(isAPOS)
            assert(lemma.id == isALemma)
            assert(pos.id == isAPOS)
          case _ => 
            assert(false)
        }
      case _ =>
    }
  	
  }

  test("I am always a programmer at graphbrain: Parse 3-role with user node (user/chihchun_chen) and no root") {
    val response = sentenceParser.parseSentenceGeneral(sentence2, user = Some(userNode));
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList;
        val nodes = parses(0)._1
        val relType = parses(0)._2

        println(nodes(0).id)
        println(nodes(1).id)
        println(nodes(2).id)
        println(relType.id)

        assert(nodes.length == 3)
        assert(nodes(0).id == userNode.id)
        assert(nodes(1).id == programmerGlobalNode.id)
        assert(nodes(2).id == graphbrainGlobalNode.id)
        assert(relType.id == isAlwaysARelTypeID)
      case _ =>

    }
  }




  test("Chih-Chun is always a programmer at graphbrain: Parse 3-role with irrelevant root node (1/toad) no user"){
    val response = sentenceParser.parseSentenceGeneral(sentence3, root = toadNodeUser);
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2

        println(nodes(0).id)
        println(nodes(1).id)
        println(nodes(2).id)
        println(relType.id)

        assert(nodes.length == 3)
        assert(nodes(0).id == globalNameNode2.id)
        assert(nodes(1).id == programmerGlobalNode.id)
        assert(nodes(2).id == graphbrainGlobalNode.id)
        assert(relType.id == isAlwaysARelTypeID)
      case _ =>

    }
  }

  test("Chih-Chun is always a programmer at graphbrain: Parse 3-role with relevant user root node (chihchun_chen/1/graphbrain) no user"){
    val response = sentenceParser.parseSentenceGeneral(sentence3, root = graphbrainUserNode);
    response(0) match {
      case r:GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2

        println(nodes(0).id)
        println(nodes(1).id)
        println(nodes(2).id)
        println(relType.id)

        assert(nodes.length == 3)
        assert(nodes(0).id == globalNameNode2.id)
        assert(nodes(1).id == programmerGlobalNode.id)
        assert(nodes(2).id == graphbrainUserNode.id)
        assert(relType.id == isAlwaysARelTypeID)
      case _ => 
    }
  }

  

  test("Chih-Chun is always a programmer at graphbrain: Parse 3-role with user node (user/chihchun_chen) and no root") {
    val response = sentenceParser.parseSentenceGeneral(sentence3, user = Some(userNode));
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2

        println(nodes(0).id)
        println(nodes(1).id)
        println(nodes(2).id)
        println(relType.id)

        assert(nodes.length == 3)
        assert(nodes(0).id == userNode.id)
        assert(nodes(1).id == programmerGlobalNode.id)
        assert(nodes(2).id == graphbrainGlobalNode.id)
        assert(relType.id == isAlwaysARelTypeID)
      case _ =>
    }
  }



}