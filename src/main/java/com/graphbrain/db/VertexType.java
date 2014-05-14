package com.graphbrain.db;

public enum VertexType {
    Edge, EdgeType, Entity, URL, User, Prog, Text;

	public static VertexType getType(String id) {
		if (id.contains(" ")) {
			return Edge;
		}

		String[] parts = ID.parts(id);
		int nparts = ID.numberOfParts(id);
        switch (parts[0]) {
            case "u":
                if (nparts == 1)
                    return Entity;
                else if (nparts == 2)
                    return User;
                else if (parts[2].equals("h"))
                    return URL;
                else if (parts[2].equals("r"))
                    return EdgeType;
                else if (parts[2].equals("n"))
                    if (nparts <= 4)
                        return Entity;
                    else if (parts[3].equals("r"))
                        return EdgeType;
                    else
                        return Entity;
                else
                    return Entity;
            case "r":
                if (nparts == 1)
                    return Entity;
                else
                    return EdgeType;
            case "n":
                if (nparts <= 2)
                    return Entity;
                else if (parts[1].equals("r"))
                    return EdgeType;
                else
                    return Entity;
            case "h":
                if (nparts == 1)
                    return Entity;
                else
                    return URL;
            case "p":
                if (nparts == 1)
                    return Entity;
                else
                    return Prog;
            case "t":
                if (nparts == 1)
                    return Entity;
                else
                    return Text;
            default:
                return Entity;
        }
    }
}
