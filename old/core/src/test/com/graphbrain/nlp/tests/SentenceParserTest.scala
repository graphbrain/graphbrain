package com.graphbrain.nlp.tests

import org.scalatest.FunSuite
import com.graphbrain.db._
import com.graphbrain.nlp.{SentenceParser, GraphResponse}
import scala.Some
import com.graphbrain.db.EdgeType


class SentenceParserTest extends FunSuite {

  val sentenceParser = new SentenceParser()
  val store = new Graph()

  val toadNodeGlobal = EntityNode.fromNsAndText("1", "toad")
  val booksNodeGlobal1 = EntityNode.fromNsAndText("1", "books")
  val booksNodeGlobal2 = EntityNode.fromNsAndText("2", "books")
  val booksUserOwned = EntityNode.fromNsAndText("user/chihchun_chen/p/1", "books")
  val todoUserOwned = EntityNode.fromNsAndText("user/chihchun_chen/p/1", "todo")
  val washTheCarUserOwned = EntityNode.fromNsAndText("user/chihchun_chen/p/1", "wash the car")
  val todoGlobal = EntityNode.fromNsAndText("1", "todo")
  val todoGlobal2 = EntityNode.fromNsAndText("2", "todo")
  val washTheCarGlobal = EntityNode.fromNsAndText("1", "wash the car")
  val jobGlobal = EntityNode.fromNsAndText("1", "job")


  val toadsNodeGlobal = EntityNode.fromNsAndText("1", "toads")

  val excellenceNodeGlobal = EntityNode.fromNsAndText("1", "excellent book")
  val toadNodeUser = EntityNode.fromNsAndText("usergenerated/chihchun_chen", "toad")
  val toadTitleUser = EntityNode.fromNsAndText("usergenerated/chihchun_chen", "Chih-Chun is a toad")
  val globalNameNode1 = EntityNode.fromNsAndText("1", "ChihChun")
  val globalNameNode2 = EntityNode.fromNsAndText("1", "Chih-Chun")
  val globalNameNode3 = EntityNode.fromNsAndText("1", "Chih-Chun is a toad")
  val globalNameNode4 = EntityNode.fromNsAndText("1", "Telmo")

  val programmerGlobalNode = EntityNode.fromNsAndText("1", "programmer")
  val userNode = UserNode.create("chihchun_chen", "Chih-Chun Chen", "chihchun@example.com", "password")
  val graphbrainGlobalNode = EntityNode.fromNsAndText("1", "GraphBrain")
  val graphbrainUserNode = EntityNode.fromNsAndText("usergenerated/chihchun_chen", "graphbrain")
  val gbURLNode = URLNode.fromUrl("http://graphbrain.com(nice)", "")
  
  val sentence1 = "ChihChun is a toad"
  val sentence2 = "I am always a programmer at graphbrain"
  val sentence3 = "Chih-Chun is always a programmer at graphbrain"
  val sentence4 = "Chih-Chun likes http://graphbrain.com(nice)"
  val sentence5 = "\"Chih-Chun is a toad\" is an excellent book"
  val sentence6 = "Chih-Chun's books are about toads."
  val sentence7 = "Chih-Chun has TODO: wash the car."
  val sentence8 = "Telmo has TODO: wash the car."
  val sentence9 = "Programmer is a job."
 
  val isA = ID.reltype_id("is_a")
  val isAn = ID.reltype_id("is_an")
  val amA = ID.reltype_id("am_a")
  val likes = ID.reltype_id("likes")
  val isALemma = ID.text_id("be_a", 1)
  val isAPOS = ID.reltype_id("vbz_dt")
  val amAlwaysA_AtLemma = ID.text_id("be_always_a~at")
  val amAlwaysA_AtPOS = ID.reltype_id("vbp_rb_dt~in")
  val amAlwaysARelTypeID = ID.reltype_id("am_always_a~at")
  val isAlwaysARelTypeID = ID.reltype_id("is_always_a~at")
  val instanceOf_ownedByID = ID.reltype_id("instance_of~owned_by")
  val areAboutID = ID.reltype_id("are_about")
  val hasOfTypeRelTypeID = ID.reltype_id("has~of_type")



  test("Chih-Chun likes http://graphbrain.com(nice)"){  
    val response = sentenceParser.parseSentenceGeneral(sentence4, root = toadNodeGlobal)
    response(0) match {
      case r: GraphResponse => 
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2
        println(nodes(0)._1.id)
        println(globalNameNode2.id)
        println(nodes(1)._1.id)
        println(relType.id)
        println(likes)
        assert(nodes(0)._1.id == globalNameNode2.id)
        assert(nodes(1)._1.id == gbURLNode.id)
        assert(relType.id == likes)

      case _ => 
    } 
  }

  test("ChihChun is a toad: Parse 2-role with root node no user"){	
    val response = sentenceParser.parseSentenceGeneral(sentence1, root = toadNodeGlobal)
    response(0) match {
      case r: GraphResponse => 
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2
        println(nodes(0)._1.id)
        println(globalNameNode1.id)
        println(nodes(1)._1.id)
        println(relType.id)
        println(isA)
        assert(nodes(0)._1.id == globalNameNode1.id)
        assert(nodes(1)._1.id == toadNodeGlobal.id)
        assert(relType.id == isA)

      case _ => 
    }	
  }

    test("Quote: \"ChihChun is a toad\" is an excellent book: Parse 2-role with root node no user"){  
    val response = sentenceParser.parseSentenceGeneral(sentence5, root = toadNodeGlobal)
    response(0) match {
      case r: GraphResponse => 
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2
        println(nodes(0)._1.id)
        println(globalNameNode3.id)
        println(nodes(1)._1.id)
        println(excellenceNodeGlobal.id)
        println(relType.id)
        println(isAn)
        assert(nodes(0)._1.id == globalNameNode3.id)
        assert(nodes(1)._1.id == excellenceNodeGlobal.id)
        assert(relType.id == isAn)

      case _ => 
    } 
  }


  test("ChihChun is a toad: Parse 2-role with user node (user/chihchun_chen) no root") {
    val response = sentenceParser.parseSentenceGeneral(sentence1, user = Some(userNode))
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2
        println(nodes(0)._1.id)
        println(userNode.id)
        println(nodes(1)._1.id)
        println(toadNodeUser.id)
        println(relType.id)
        println(isA)
        assert(nodes(0)._1.id == userNode.id)
        assert(nodes(1)._1.id == toadNodeGlobal.id)
        assert(relType.id == isA)
      case _ =>

    }
  }

  test("Chih-Chun is a toad: Parse 2-role with user node (user/chihchun_chen) and global root (1/toad)") {

    val response = sentenceParser.parseSentenceGeneral(sentence1, root = toadNodeGlobal, user = Some(userNode))
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2
        println(nodes(0)._1.id)
        println(userNode.id)
        println(nodes(1)._1.id)
        println(toadNodeGlobal.id)
        println(relType.id)
  
        assert(nodes(0)._1.id == userNode.id)
        //Should override the fact that user has node in namespace if root is the global node
        assert(nodes(1)._1.id == toadNodeGlobal.id)
        assert(relType.id == isA)
      case _ =>
    }
  }

  test("Chih-Chun is a toad: 2-role lemmas and POS") {

    val response =  sentenceParser.parseSentenceGeneral(sentence1)
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relTypeVertex = parses(0)._2
        //val relTypeVertex = EdgeType(id = ID.reltype_id(relTypeText), label = relTypeText)
        relTypeVertex match {
          case r: EdgeType => 
            println(r.id)
            println(r.getLabel())
            val reltypeLemPOS = sentenceParser.relTypeLemmaAndPOS(r, sentence1)

            val lemma = reltypeLemPOS._2._1
            println(lemma.id)
            println(isALemma)

            val pos = reltypeLemPOS._2._2
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
    val response = sentenceParser.parseSentenceGeneral(sentence2, user = Some(userNode))
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2

        println(nodes(0)._1.id)
        println(nodes(1)._1.id)
        println(nodes(2)._1.id)
        println(relType.id)

        assert(nodes.length == 3)
        assert(nodes(0)._1.id == userNode.id)
        assert(nodes(1)._1.id == programmerGlobalNode.id)
        assert(nodes(2)._1.id == graphbrainGlobalNode.id)
        assert(relType.id == isAlwaysARelTypeID)
      case _ =>

    }
  }




  test("Chih-Chun is always a programmer at graphbrain: Parse 3-role with irrelevant root node (1/toad) no user"){
    val response = sentenceParser.parseSentenceGeneral(sentence3, root = toadNodeUser)
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2

        println(nodes(0)._1.id)
        println(nodes(1)._1.id)
        println(nodes(2)._1.id)
        println(relType.id)

        assert(nodes.length == 3)
        assert(nodes(0)._1.id == globalNameNode2.id)
        assert(nodes(1)._1.id == programmerGlobalNode.id)
        assert(nodes(2)._1.id == graphbrainGlobalNode.id)
        assert(relType.id == isAlwaysARelTypeID)
      case _ =>

    }
  }

  test("Chih-Chun is always a programmer at graphbrain: Parse 3-role with relevant user root node (chihchun_chen/1/graphbrain) no user"){
    val response = sentenceParser.parseSentenceGeneral(sentence3, root = graphbrainUserNode)
    response(0) match {
      case r:GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2

        println(nodes(0)._1.id)
        println(nodes(1)._1.id)
        println(nodes(2)._1.id)
        println(relType.id)

        assert(nodes.length == 3)
        assert(nodes(0)._1.id == globalNameNode2.id)
        assert(nodes(1)._1.id == programmerGlobalNode.id)
        assert(nodes(2)._1.id == graphbrainUserNode.id)
        assert(relType.id == isAlwaysARelTypeID)
      case _ => 
    }
  }

  

  test("Chih-Chun is always a programmer at graphbrain: Parse 3-role with user node (user/chihchun_chen) and no root") {
    val response = sentenceParser.parseSentenceGeneral(sentence3, user = Some(userNode))
    response(0) match {
      case r: GraphResponse =>
        val parses = r.hypergraphList
        val nodes = parses(0)._1
        val relType = parses(0)._2

        println(nodes(0)._1.id)
        println(nodes(1)._1.id)
        println(nodes(2)._1.id)
        println(relType.id)

        assert(nodes.length == 3)
        assert(nodes(0)._1.id == userNode.id)
        assert(nodes(1)._1.id == programmerGlobalNode.id)
        assert(nodes(2)._1.id == graphbrainGlobalNode.id)
        assert(relType.id == isAlwaysARelTypeID)
      case _ =>
    }
  }


  test("Chih-Chun's books are about toads: No user given") {
    val response = sentenceParser.parseSentenceGeneral(sentence6, user = None)
    response(0) match {
      case g: GraphResponse => 
        val parse = g.hypergraphList(0)
        parse match {
          case (nodes: List[(Vertex, Option[(List[Vertex], Vertex)])], relType: Vertex) =>

          val nodeEntry1 = nodes(0)
          val nodeEntry2 = nodes(1)
          println("node2 obtained: " + nodeEntry2._1.id)
          println("node2 expected: " + toadsNodeGlobal.id)
          assert(nodeEntry2._1.id==toadsNodeGlobal.id)

          nodeEntry1 match {
            case (nd: EntityNode, None) => println("Node: " + nd.id)
            case (nd: EntityNode, Some(aux: (List[Vertex], Vertex))) =>
              println("Node 1 with aux: " + nd.id)
              assert(nd.id==booksNodeGlobal2.id)
              aux match {
                case (a:List[EntityNode], ed:EdgeType) =>
                  assert(a.length==2)
                  println("aux0: " + a(0).id)
                  println("aux1: " + a(1).id)
                  assert(a(0).id==booksNodeGlobal1.id)
                  assert(a(1).id==globalNameNode2.id)
                  println("auxEdge: " + ed.id)
                  assert(ed.id==instanceOf_ownedByID)

                case _ => println("mismatch")
              }
              case _ => println("No match")
            }
            println("Rel: " + relType.id)
            assert(relType.id==areAboutID) 
          
        }
      
    }
  }
    

  test("Chih-Chun's books are about toads: User given") {
    val response = sentenceParser.parseSentenceGeneral(sentence6, user = Some(userNode))
    response(0) match {
      case g: GraphResponse => 
        val parse = g.hypergraphList(0)
        parse match {
          case (nodes: List[(Vertex, Option[(List[Vertex], Vertex)])], relType: Vertex) =>

          val nodeEntry1 = nodes(0)
          val nodeEntry2 = nodes(1)
          println("node2 obtained: " + nodeEntry2._1.id)
          println("node2 expected: " + toadsNodeGlobal.id)
          assert(nodeEntry2._1.id==toadsNodeGlobal.id)
          
          nodeEntry1 match {
            case (nd: EntityNode, None) => println("Node: " + nd.id)
            case (nd: EntityNode, Some(aux: (List[Vertex], Vertex))) =>
              println("Node with aux: " + nd.id)
              assert(nd.id==booksUserOwned.id)
              aux match {
                case (a:List[Vertex], ed:EdgeType) => 
                  assert(a.length==2)
                  println("aux0: " + a(0).id)
                  println("aux1: " + a(1).id)
                  assert(a(0).id==booksNodeGlobal1.id)
                  assert(a(1).id==userNode.id)
                  println("auxEdge: " + ed.id)
                  assert(ed.id==instanceOf_ownedByID)

                case _ => println("mismatch")
              }
              case _ => println("No match")
            }
            println("Rel: " + relType.id)
            assert(relType.id==areAboutID) 
          
        }
      
    }
  }


  test("Chih-Chun has TODDO: wash the car: User given") {
    val response = sentenceParser.parseSentenceGeneral(sentence7, user = Some(userNode))
    response(0) match {
      case g: GraphResponse => 
        val parse = g.hypergraphList(0)
        parse match {
          case (nodes: List[(Vertex, Option[(List[Vertex], Vertex)])], relType: Vertex) =>

          val nodeEntry1 = nodes(0)
          val nodeEntry2 = nodes(1)
          val nodeEntry3 = nodes(2)
          println("node1 obtained: " + nodeEntry1._1.id)
          println("node1 expected: " + userNode.id)
          assert(nodeEntry1._1.id==userNode.id)
          
          nodeEntry2 match {
            case (nd: EntityNode, None) => println("Node: " + nd.id)
            case (nd: EntityNode, Some(aux: (List[Vertex], Vertex))) =>
              println("Node with aux: " + nd.id)
              assert(nd.id==washTheCarUserOwned.id)
              aux match {
                case (a:List[Vertex], ed:EdgeType) => 
                  assert(a.length==2)
                  println("aux0: " + a(0).id)
                  println("aux0 expected: " + todoUserOwned.id)
                  println("aux1: " + a(1).id)
                  println("aux1 expected: " + userNode.id)
                  assert(a(0).id==todoUserOwned.id)
                  assert(a(1).id==userNode.id)
                  println("auxEdge: " + ed.id)
                  assert(ed.id==instanceOf_ownedByID)

                case _ => println("mismatch")
              }
              case _ => println("No match")
            }
          nodeEntry3 match {
            case (nd: EntityNode, None) => println("Node: " + nd.id)
            case (nd: EntityNode, Some(aux: (List[Vertex], Vertex))) =>
              println("Node with aux: " + nd.id)
              assert(nd.id==todoUserOwned.id)
              aux match {
                case (a:List[Vertex], ed:EdgeType) => 
                  assert(a.length==2);
                  println("aux0: " + a(0).id)
                  println("aux1: " + a(1).id)
                  assert(a(0).id==todoGlobal.id)
                  assert(a(1).id==userNode.id)
                  println("auxEdge: " + ed.id)
                  assert(ed.id==instanceOf_ownedByID)

                case _ => println("mismatch")
              }
              case _ => println("No match")
            }


            println("Rel: " + relType.id)
            assert(relType.id==hasOfTypeRelTypeID) 
          
        }
      
    }
    
  }


  test("Chih-Chun has TODDO: wash the car: Not user") {
    val response = sentenceParser.parseSentenceGeneral(sentence8, user = Some(userNode))
    response(0) match {
      case g: GraphResponse => 
        val parse = g.hypergraphList(0)
        parse match {
          case (nodes: List[(Vertex, Option[(List[Vertex], Vertex)])], relType: Vertex) =>

          val nodeEntry1 = nodes(0)
          val nodeEntry2 = nodes(1)
          val nodeEntry3 = nodes(2)
          println("node1 obtained: " + nodeEntry1._1.id)
          println("node1 expected: " + globalNameNode4.id)
          assert(nodeEntry1._1.id==globalNameNode4.id)
          
          nodeEntry2 match {
            case (nd: EntityNode, None) => println("Node: " + nd.id)
            case (nd: EntityNode, Some(aux: (List[Vertex], Vertex))) =>
              println("Node with aux: " + nd.id)
              assert(nd.id==washTheCarGlobal.id)
              aux match {
                case (a:List[Vertex], ed:EdgeType) => 
                  assert(a.length==2)
                  println("aux0: " + a(0).id)
                  println("aux0 expected: " + todoGlobal2.id)
                  println("aux1: " + a(1).id)
                  println("aux1 expected: " + globalNameNode4.id)
                  assert(a(0).id==todoGlobal2.id)
                  assert(a(1).id==globalNameNode4.id)
                  println("auxEdge: " + ed.id)
                  assert(ed.id==instanceOf_ownedByID)

                case _ => println("mismatch")
              }
              case _ => println("No match")
            }
          nodeEntry3 match {
            case (nd: EntityNode, None) => println("Node: " + nd.id)
            case (nd: EntityNode, Some(aux: (List[Vertex], Vertex))) =>
              println("Node with aux: " + nd.id)
              assert(nd.id==todoGlobal2.id)
              aux match {
                case (a:List[Vertex], ed:EdgeType) => 
                  assert(a.length==2)
                  println("aux0: " + a(0).id)
                  println("aux0 expected: " + todoGlobal.id)
                  println("aux1: " + a(1).id)
                  println("aux1 expected: " + globalNameNode4.id)
                  assert(a(0).id==todoGlobal.id)
                  assert(a(1).id==globalNameNode4.id)
                  println("auxEdge: " + ed.id)
                  assert(ed.id==instanceOf_ownedByID)

                case _ => println("mismatch")
              }
              case _ => println("No match")
            }


            println("Rel: " + relType.id)
            assert(relType.id==hasOfTypeRelTypeID) 
          
        }
      
    }    
  }
  
  test("It is a job.: Root programmer given") {
    val response = sentenceParser.parseSentenceGeneral(sentence9, root = programmerGlobalNode, user = Some(userNode));
    response(0) match {
      case g: GraphResponse => 
        val parse = g.hypergraphList(0)
        parse match {
          case (nodes: List[(Vertex, Option[(List[Vertex], Vertex)])], relType: Vertex) =>
          val nodeEntry1 = nodes(0)
          val nodeEntry2 = nodes(1)
          println("node1 obtained: " + nodeEntry1._1.id)
          println("node1 expected: " + programmerGlobalNode.id)
          assert(nodeEntry1._1.id==programmerGlobalNode.id)
          println("node2 obtained: " + nodeEntry2._1.id)
          println("node2 expected: " + jobGlobal.id)
          assert(nodeEntry2._1.id==jobGlobal.id)
          println("Rel: " + relType.id)
          assert(relType.id==isA)
        }
    }
  }





}