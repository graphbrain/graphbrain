package com.graphbrain.db;

public enum VertexType {
    Edge, EdgeType, Entity, URL, User, Prog, Text;
	
	public static VertexType getType(String id) {
		if (id.contains(" ")) {
			return Edge;
		}
		
		String[] parts = ID.parts(id);
		int nparts = ID.numberOfParts(id);
		if (parts[0].equals("user"))
			if (nparts == 1)
                return Entity;
			else if (nparts == 2)
				return User;
	        else if (parts[2].equals("url"))
				return URL;
            else if (parts[2].equals("r"))
                return EdgeType;
            else if (parts[2].equals("neg"))
                if (nparts <= 4)
                    return Entity;
                else if (parts[3].equals("r"))
                    return EdgeType;
                else
                    return Entity;
			else
                return Entity;
		else if (parts[0].equals("r"))
			if (nparts == 1)
                return Entity;
			else
				return EdgeType;
        else if (parts[0].equals("neg"))
            if (nparts <= 2)
                return Entity;
            else if (parts[1].equals("r"))
                return EdgeType;
            else
                return Entity;
	    else if (parts[0].equals("url"))
			if (nparts == 1)
                return Entity;
			else
				return URL;
		else if (parts[0].equals("prog"))
			if (nparts == 1)
                return Entity;
			else
				return Prog;
        else if (parts[0].equals("text"))
            if (nparts == 1)
                return Entity;
            else
                return Text;
		else
            return Entity;
    }
}