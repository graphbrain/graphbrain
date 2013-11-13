package com.graphbrain.db

object VertexType extends Enumeration {
  type VertexType = Value
	val Edge, EdgeType, Entity, URL, User, Source, Context, Prog, Text = Value
	
	def getType(id: String): VertexType = {
		if (id.contains(" ")) {
			return Edge
		}
		
		val parts = ID.parts(id)
		val nparts = ID.numberOfParts(id)
		if (parts(0) == "user")
			if (nparts == 1)
        Entity
			else if (nparts == 2)
				User
			else if (parts(2) == "url")
				URL
      else if (parts(2) == "rtype")
        EdgeType
      else if (parts(2) == "neg")
        if (nparts <= 4)
          Entity
        else if (parts(3) == "rtype")
          EdgeType
        else
          Entity
			else if (parts(2) == "context")
				if (nparts == 4)
					Context
				else
          Entity
			else
        Entity
		else if (parts(0) == "rtype")
			if (nparts == 1)
        Entity
			else
				EdgeType
    else if (parts(0) == "neg")
      if (nparts <= 2)
        Entity
      else if (parts(1) == "rtype")
        EdgeType
      else
        Entity
		else if (parts(0) == "url")
			if (nparts == 1)
        Entity
			else
				URL
		else if (parts(0) == "prog")
			if (nparts == 1)
        Entity
			else
				Prog
		else if (parts(0) == "source")
			if (nparts == 1)
        Entity
			else
				Source
    else if (parts(0) == "text")
      if (nparts == 1)
        Entity
      else
        Text
		else
      Entity
  }
}