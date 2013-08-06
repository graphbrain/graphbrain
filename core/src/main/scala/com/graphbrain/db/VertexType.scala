package com.graphbrain.core

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
				return Text
			else if (nparts == 2)
				return User
			else if (parts(2) == "url")
				return URL
			else if (parts(2) == "context")
				if (nparts == 4)
					return Context
				else
					return Text
			else
				return Text
		else if (parts(0) == "rtype")
			if (nparts == 1)
				return Text
			else
				return EdgeType
		else if (parts(0) == "url")
			if (nparts == 1)
				return Text
			else
				return URL
		else if (parts(0) == "rule")
			if (nparts == 1)
				return Text
			else
				return Rule
		else if (parts(0) == "source")
			if (nparts == 1)
				return Text
			else
				return Source
		else
			return Text
  }
}