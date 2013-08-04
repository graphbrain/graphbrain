package com.graphbrain.db;

import static com.graphbrain.db.ID.*;

public enum IdFamily {
	Global, User, UserSpace, EType, URL, UserURL, Rule, Source, Context;

	public static IdFamily family(String id) {
		String[] parts = parts(id);
		int nparts = numberOfParts(id);
		if (parts[0].equals("user"))
			if (nparts == 1)
				return Global;
			else if (nparts == 2)
				return User;
			else if (parts[2].equals("url"))
				return UserURL;
			else if (parts[2].equals("context"))
				if (nparts == 4)
					return Context;
				else
					return UserSpace;
			else
				return UserSpace;
		else if (parts[0].equals("rtype"))
			if (nparts == 1)
				return Global;
			else
				return EType;
		else if (parts[0].equals("url"))
			if (nparts == 1)
				return Global;
			else
				return URL;
		else if (parts[0].equals("rule"))
			if (nparts == 1)
				return Global;
			else
				return Rule;
		else if (parts[0].equals("source"))
			if (nparts == 1)
				return Global;
			else
				return Source;
		else
			return Global;
  }
}