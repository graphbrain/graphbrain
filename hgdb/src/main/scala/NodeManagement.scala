package com.graphbrain.hgdb


trait NodeManagement extends VertexStoreInterface {
  
  def createBrain(name: String, user: UserNode, access:String): String = {
    val id = ID.brainId(name, user.username)
    put(Brain(id=id, name=name, access=access))

    // add brain to user
    put(user.setBrains(user.brains + id))

    // create "brain -> user" relationship
    // make user "brain" node exists
    if (!exists("brain")) {
      put(TextNode("brain", "brain"))
    }
    addrel("brain", Array(user.id, id))

    // create "is -> brain" relationship
    addrel("is", Array(id, "brain"))

    id
  }

  def brainId(vertex: Vertex): String = {
    vertex match {
      case b: Brain => b.id
      case u: UserNode => u.id
      case v: Vertex => {
        val tokens = v.id.split("/")
        if (tokens.size < 3) {
          ""
        }
        else if (tokens(0) == "brain") {
          tokens(0) + "/" + tokens(1) + "/" + tokens(2)
        }
        else {
          ""
        }
      }
      case _ => ""
    }
  }

  def createAndConnectVertices(edgeType: String, participants: Array[Vertex]) = {
    for (v <- participants) {
      if (!exists(v.id)) {
        put(v)
      }
    }

    val ids = for (v <- participants) yield v.id
    addrel(edgeType, ids)
  }
}