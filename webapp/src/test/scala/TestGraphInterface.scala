import org.scalatest.FunSuite
import com.graphbrain.webapp.GraphInterface
import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.SimpleCaching
import com.graphbrain.hgdb.Node
import com.graphbrain.hgdb.Edge
import scala.collection.mutable.{Set => MSet}
import scala.collection.mutable.{Map => MMap}

class TestGraphInterface extends FunSuite {
  def createGraph(store: VertexStore) = {
    val a = Node("a"); store.update(a)
    val b = Node("b"); store.update(b)
    val c = Node("c"); store.update(c)
    val d = Node("d"); store.update(d)
    val e = Node("e"); store.update(e)
    val f = Node("f"); store.update(f)
    val g = Node("g"); store.update(g)
    val h = Node("h"); store.update(h)
    
    store.addrel("is", Array[String]("a", "b"))
    store.addrel("is", Array[String]("a", "c"))
    store.addrel("is", Array[String]("a", "d"))
    store.addrel("is", Array[String]("a", "e"))
    store.addrel("is", Array[String]("f", "b"))
    store.addrel("is", Array[String]("g", "b"))
  }
    
  /*
  test("supernodes") {
    val edge1 = Edge("is", Array[String]("a", "b"))
    val edge2 = Edge("is", Array[String]("a", "c"))
    val edge3 = Edge("is", Array[String]("a", "d"))
    val edge4 = Edge("is", Array[String]("d", "e"))
    val edgeSet = Set[Edge](edge1, edge2, edge3, edge4)
    val sn = GraphInterface.supernodes(edgeSet, "c")

    val expected = MSet(
      Map("id" -> "sn0", "key" -> ("", -1, ""), "nodes" -> Set("c")),
      Map("id" -> "sn1", "key" -> ("is", 0, "a"), "nodes" -> Set("b", "d")),
      Map("id" -> "sn2", "key" -> ("is", 1, "e"), "nodes" -> Set("d")))
    assert(sn == expected)
  }*/

  test("visualLink") {
    val store = new VertexStore("testhgdb") with SimpleCaching
    createGraph(store)
    val gi = new GraphInterface("a", store)

    println(">>> nodesJSON")
    println(gi.nodesJSON)
    println(".............")
    println(">>> snodesJSON")
    println(gi.snodesJSON)
    println(".............")
    println(">>> linksJSON")
    println(gi.linksJSON)

    //val expected = Set(("is", "a", "c"), ("is", "a", "sn1"))
    //assert(vl == expected)
  }
}