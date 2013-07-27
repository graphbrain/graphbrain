package com.graphbrain.db;

import org.apache.commons.lang3.text.WordUtils;

public class ID {

	public static String sanitize(String str) {
		return str.toLowerCase().replace("/", "_").replace(" ", "_");
	}

	public static String[] parts(String id) {
		return id.split("/");
	}

	public static int numberOfParts(String id) {
		return parts(id).length;
	}

	public static String namespace(String id) {
		String[] p = parts(id);
		
		StringBuilder sb = new StringBuilder(50);
		for (int i = 0; i < p.length - 1; i ++) {
			if (i > 0) {
				sb.append("/");
			}
			sb.append(p[i]);
		}
		return sb.toString();
	}

	public static String lastPart(String id) {
		String[] p = parts(id);
		return p[p.length - 1];
	}

	public static String humanReadable(String id) {
		String str = lastPart(id).toLowerCase().replaceAll("_", " ");
		return WordUtils.capitalizeFully(str);
	}

	public static boolean isUserNode(String idOrNs) {
		return (parts(idOrNs)[0].equals("user")) && (numberOfParts(idOrNs) == 2);
	}

	public static boolean isInUserSpace(String idOrNs) {
		return (parts(idOrNs)[0].equals("user")) && (numberOfParts(idOrNs) > 2);
	}

	public static boolean isContextNode(String idOrNs) {
		return (numberOfParts(idOrNs) == 4) && (parts(idOrNs)[2].equals("context"));
	}

	public static boolean isInContextSpace(String idOrNs) {
		return (numberOfParts(idOrNs) > 3) && (parts(idOrNs)[2].equals("context"));
	}

	public static boolean isPersonal(String idOrNs) {
		return (parts(idOrNs)[0].equals("user"))
				&& (numberOfParts(idOrNs) > 3)
				&& (parts(idOrNs)[2].equals("p"));
	}

	public static boolean isInUserGlobalSpace(String idOrNs) {
		return isInUserSpace(idOrNs) && !isPersonal(idOrNs);
	}

	public static boolean isInSystemSpace(String idOrNs) {
		return parts(idOrNs)[0].equals("sys");
	}

	public static String globalToUser(String idOrNs, String userid) {
		if (isInUserSpace(idOrNs))
			return idOrNs;
		else if (isUserNode(idOrNs))
			return idOrNs;
		else if ((numberOfParts(userid) > 0) && (parts(userid)[0].equals("user"))) {
			return userid + "/" + idOrNs;
		}
		else {
			return "user/" + userid + "/" + idOrNs;
		}
	}

	public static String userToGlobal(String idOrNs) {
		if (isInUserGlobalSpace(idOrNs) && (!isInContextSpace(idOrNs))) {
			String[] p = parts(idOrNs);
			StringBuilder sb = new StringBuilder(50);
			if (p.length > 2) {
				for (int i = 2; i < p.length; i++) {
					if (i > 2) {
						sb.append("/");
					}
					sb.append(p[i]);
				}
			}
			return sb.toString();
		}
		else {
			return idOrNs;
		}
	}

	public static String removeContext(String idOrNs) {
		if (isInContextSpace(idOrNs)) {
			String[] p = parts(idOrNs);
      
			StringBuilder sb = new StringBuilder(50);
			for (int i = 0; i < p.length; i++) {
				if (i < 2 || i > 3) {
					if (i > 0) {
						sb.append("/");
					}
					sb.append(p[i]);
				}
			}
			return sb.toString();
		}
		else {
			return idOrNs;
		}
	}

	public static String setContext(String idOrNs, String contextId) {
		if (isUserNode(idOrNs) || isContextNode(idOrNs)) {
			if (contextId.equals(""))
				return idOrNs;
			else
				return contextId;
		}
		else {
			StringBuilder sb = new StringBuilder(50);
			sb.append(contextId);
			if (!contextId.equals("")) {
				sb.append("/");
			}
			sb.append(userToGlobal(removeContext(idOrNs)));
			return sb.toString();
		}
	}

	public static String ownerId(String idOrNs) {    
		String[] tokens = parts(idOrNs);
		if (tokens[0].equals("user"))
			return "user/" + tokens[1];  
		else
			return "";
	}

	public static String contextId(String idOrNs) {
		if (isInContextSpace(idOrNs)) {
			String[] p = parts(idOrNs);
			return p[0] + "/" + p[1] + "/" + p[2] + "/" + p[3];
		}
		else {
			return "";
		}
	}
	
	public static String buildContextId(String userId, String name) {
		return userId + "/context/" + ID.sanitize(name).toLowerCase();
	}

	public static String relationshipId(String edgeType, int position) {
		return edgeType + "/" + position;
	}

	public static String userIdFromUsername(String username) {
		return "user/" + username;
	}

	public static String urlId(String url) {
		return "url/" + url.toLowerCase().replaceAll("/+$", "");
	}
	
	public static String urlId(String url, String userId) {
		String auxId = urlId(url);
		if (userId.equals("")) {
			return auxId;
		}
		else {
			return globalToUser(auxId, userId);
		}
	}

	public static String usergenerated_id(String userName, String thing) {
		return userName + "/" + thing;
	}

	public static String personalOwned_id(String userName, String thing) {
		return personalOwned_id(userName, thing, 1);
	}
	
	public static String personalOwned_id(String userName, String thing, int which) {
		return "user/" + userName + "/p/" + which + "/" + sanitize(thing);
	}

	public static String text_id(String thing) {
		return text_id(thing, 1);
	}
	
	public static String text_id(String thing, int which) {
		return "" + which + "/" + sanitize(thing);
	}

	public static String relation_id(String relation) {
		return sanitize(relation);
	}

	public static String relation_id(String rel, String node1ID, String node2ID) {
		return rel + " " + node1ID + " " + node2ID;
	}

	public static String reltype_id(String relT) {
		return reltype_id(relT, 1);
	}
	
	public static String reltype_id(String relT, int which) {
		return "rtype/" + which + "/" + sanitize(relT);
	}
    
	public static String rule_id(String rule_name) {
		return "rule/" + sanitize(rule_name);
	}

	public static String wikipedia_id(String wptitle) {
		String title = wptitle.toLowerCase().replace(" ", "_");
		return "wikipedia/" + title;
	}
}