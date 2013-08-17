package com.graphbrain.db

object VertexType extends Enumeration {
  type VertexType = Value
	val Edge, EdgeType, Text, URL, User, Source, Context, Rule = Value
	
	def getType(id: String): VertexType = {
		if (id.contains(" ")) {
			return Edge
		}
		
		val parts = ID.parts(id)
		val nparts = ID.numberOfParts(id)
		if (parts(0) == "user")
			if (nparts == 1)
				Text
			else if (nparts == 2)
				User
			else if (parts(2) == "url")
				URL
      else if (parts(2) == "rtype")
        EdgeType
      else if (parts(2) == "neg")
        if (nparts <= 4)
          Text
        else if (parts(3) == "rtype")
          EdgeType
        else
          Text
			else if (parts(2) == "context")
				if (nparts == 4)
					Context
				else
					Text
			else
				Text
		else if (parts(0) == "rtype")
			if (nparts == 1)
				Text
			else
				EdgeType
    else if (parts(0) == "neg")
      if (nparts <= 2)
        Text
      else if (parts(1) == "rtype")
        EdgeType
      else
        Text
		else if (parts(0) == "url")
			if (nparts == 1)
				Text
			else
				URL
		else if (parts(0) == "rule")
			if (nparts == 1)
				Text
			else
				Rule
		else if (parts(0) == "source")
			if (nparts == 1)
				Text
			else
				Source
		else
			Text
  }
}