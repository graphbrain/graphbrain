package com.graphbrain

class Node(_id: String, val edges: Set[String]) extends Vertex(_id) {
  override val vtype = "node"

  override def toMap: Map[String, Any] = super.toMap ++ Map(("edges" -> edges))

  def addEdge(edge: Edge): Node = Node(_id, edges + edge._id)

  /*
  def neighbors(nodes: Map[String, Node] = Map[String, Node](), depth: Int = 0, maxDepth: Int = 2):
  	if (depth < maxDepth) {
  		nodes ++ 
  	}
  	else {
  		nodes
  	}

  	next_nodes = []

        if (depth < 2):
            if 'targs' in self.d:
                for n in self.d['targs'].keys():
                    if n not in nodeids:
                        nnode = Node().get_by_id(n)
                        if not nnode.d is None:
                            count += 1
                            if count > maxnodes:
                                return
                            nnode.parent = self.d['_id']
                            nodes.append(nnode)
                            nodeids.append(n)
                            next_nodes.append(nnode)

            if 'origs' in self.d:
                for n in self.d['origs']:
                    if n not in nodeids:
                        nnode = Node().get_by_id(n)
                        if not nnode.d is None:
                            count += 1
                            if count > maxnodes:
                                return
                            nnode.parent = self.d['_id']
                            nodes.append(nnode)
                            nodeids.append(n)
                            next_nodes.append(nnode)

            for n in next_nodes:
                n._neighbors(nodes, nodeids, depth + 1, count)
}
*/
}

object Node {
  def apply(_id: String, edges: Set[String] = Set[String]()) = new Node(_id, edges) 
}