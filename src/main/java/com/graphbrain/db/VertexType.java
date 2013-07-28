package com.graphbrain.db;

import static com.graphbrain.db.ID.numberOfParts;
import static com.graphbrain.db.ID.parts;

public enum VertexType {
	Text, URL, EdgeType, User, Source, Context, Rule;
	
	public static VertexType type(String id) {
		String[] parts = parts(id);
		int nparts = numberOfParts(id);
		if (parts[0].equals("user"))
			if (nparts == 1)
				return Text;
			else if (nparts == 2)
				return User;
			else if (parts[2].equals("url"))
				return URL;
			else if (parts[2].equals("context"))
				if (nparts == 4)
					return Context;
				else
					return Text;
			else
				return Text;
		else if (parts[0].equals("rtype"))
			if (nparts == 1)
				return Text;
			else
				return EdgeType;
		else if (parts[0].equals("url"))
			if (nparts == 1)
				return Text;
			else
				return URL;
		else if (parts[0].equals("rule"))
			if (nparts == 1)
				return Text;
			else
				return Rule;
		else if (parts[0].equals("source"))
			if (nparts == 1)
				return Text;
			else
				return Source;
		else
			return Text;
  }
}