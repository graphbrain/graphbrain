import org.scalatest.FunSuite
import com.graphbrain.webapp.GraphInterface
import com.graphbrain.hgdb.Edge
import scala.collection.mutable.{Set => MSet}
import scala.collection.mutable.{Map => MMap}

class TestGraphInterface extends FunSuite {
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
  }

  test("visualLink") {
    val edge1 = Edge("is", Array[String]("a", "b"))
    val edge2 = Edge("is", Array[String]("a", "c"))
    val edge3 = Edge("is", Array[String]("a", "d"))
    val edgeSet = Set[Edge](edge1, edge2, edge3)
    val sn = GraphInterface.supernodes(edgeSet, "c")
    val vl = GraphInterface.visualLinks(sn, edgeSet)
    
    val expected = Set(("is", "a", "c"), ("is", "a", "sn1"))
    assert(vl == expected)
  }
}