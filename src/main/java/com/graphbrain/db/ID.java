package com.graphbrain.db;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

public class ID {

    public static String hash(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return Long.toHexString(h);
    }

    public static String buildId(String[] parts) {
        StringBuilder sb = new StringBuilder(50);
        for (int i = 0; i < parts.length; i ++) {
            if (i > 0) {
                sb.append("/");
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }

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
        String[] nsParts = Arrays.copyOfRange(p, 0, p.length - 1);
        return buildId(nsParts);
    }

    public static String lastPart(String id) {
        String[] p = parts(id);
        if (VertexType.getType(id) == VertexType.URL) {
            int start = 1;
            if (isUserNode(id)) {
                start = 3;
            }
            String last = "";
            for (int i = start; i < p.length; i++) {
                if (i > start) {
                    last += "/";
                }
                last += p[i];
            }
            return last;
        }
        else {
            return p[p.length - 1];
        }
    }

    public static String humanReadable(String id) {
        return lastPart(id).toLowerCase().replace('_', ' ');
    }

    public static boolean isUserNode(String idOrNs) {
        return (parts(idOrNs)[0].equals("u")) && (numberOfParts(idOrNs) == 2);
    }

    public static boolean isInUserSpace(String idOrNs) {
        return (parts(idOrNs)[0].equals("u")) && (numberOfParts(idOrNs) > 2);
    }

    public static boolean isContextNode(String idOrNs) {
        return (numberOfParts(idOrNs) == 4) && (parts(idOrNs)[2].equals("context"));
    }

    public static boolean isInContextSpace(String idOrNs) {
        return (numberOfParts(idOrNs) > 3) && (parts(idOrNs)[2].equals("context"));
    }

    public static boolean isPersonal(String idOrNs) {
        return (parts(idOrNs)[0].equals("u"))
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
        if (isInUserSpace(idOrNs)) {
            return idOrNs;
        }
        else if (isUserNode(idOrNs)) {
            return idOrNs;
        }
        else {
            if ((numberOfParts(userid) > 0) && (parts(userid)[0].equals("user"))) {
                return userid + "/" + idOrNs;
            }
            else {
                return "user/" + userid + "/" + idOrNs;
            }
        }
    }

    public static String userToGlobal(String idOrNs) {
        if (isInUserGlobalSpace(idOrNs) && (!isInContextSpace(idOrNs))) {
            String[] idParts = parts(idOrNs);
            String[] globalParts = Arrays.copyOfRange(idParts, 2, idParts.length);

            if (globalParts.length == 1) {
                return globalParts[0];
            }
            else {
                return buildId(globalParts);
            }
        }
        else {
            return idOrNs;
        }
    }

    public static String ownerId(String idOrNs) {
        String[] tokens = parts(idOrNs);
        if (tokens[0].equals("user")) {
            return "user/" + tokens[1];
        }
        else {
            return "";
        }
    }

    public static String relationshipId(String edgeType, int position) {
        return edgeType + "/" + position;
    }

    public static String userIdFromUsername(String username) {
        return "user/" + username;
    }

    public static String emailId(String email) {
        return "email/" + email.toLowerCase();
    }

    public static String idFromUsername(String username) {
        String usernameNoSpaces = username.replace(' ', '_');
        try {
            return "user/" + URLEncoder.encode(usernameNoSpaces, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
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
        return "(" + rel + " " + node1ID + " " + node2ID + ")";
    }

    public static String reltype_id(String relT) {
        return reltype_id(relT, 1);
    }

    public static String reltype_id(String relT, int which) {
        return "r/" + which + "/" + sanitize(relT);
    }

    public static String rule_id(String rule_name) {
        return "rule/" + sanitize(rule_name);
    }

    public static String wikipedia_id(String wptitle) {
        String title = wptitle.toLowerCase().replace(" ", "_");
        return "wikipedia/" + title;
    }
}
