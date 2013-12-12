package com.graphbrain.web;

import com.graphbrain.db.Graph;
import com.graphbrain.db.UserNode;
import spark.Request;

import java.text.DateFormat;
import java.util.Locale;

import static spark.Spark.*;

public class WebServer {

    public static Graph graph = new Graph();

    public static void main(String[] args) {

        staticFileLocation("/");

        get(new HandleLanding("/"));

        get(new HandleNode("/node/*"));
        post(new HandleNodeActions("/node/*"));

        get(new HandleRaw("/raw/*"));

        post(new HandleUser(HandleUser.HandleUserType.SIGNUP, "/signup"));
        post(new HandleUser(HandleUser.HandleUserType.CHECK_USERNAME, "/checkusername"));
        post(new HandleUser(HandleUser.HandleUserType.CHECK_EMAIL, "/checkemail"));
        post(new HandleUser(HandleUser.HandleUserType.LOGIN, "/login"));

        post(new HandleSearch("/search"));

        post(new HandleRelations("/rel"));

        post(new HandleUndoFact("/undo_fact"));

        get(new HandleAllUsers("/allusers"));

        get(new HandleEco(HandleEco.HandleEcoType.PARSER, "/eco"));
        post(new HandleEco(HandleEco.HandleEcoType.PARSER, "/eco"));
        get(new HandleEco(HandleEco.HandleEcoType.CODE, "/eco/code"));
        post(new HandleEco(HandleEco.HandleEcoType.CODE, "/eco/code"));
        get(new HandleEco(HandleEco.HandleEcoType.EDIT_TESTS, "/eco/edittests"));
        post(new HandleEco(HandleEco.HandleEcoType.EDIT_TESTS, "/eco/edittests"));
        get(new HandleEco(HandleEco.HandleEcoType.RUN_TESTS, "/eco/runtests"));
        post(new HandleEco(HandleEco.HandleEcoType.RUN_TESTS, "/eco/runtests"));
    }

    public static UserNode getUser(Request req) {
        String username = null;

        if (req.cookies().containsKey("username")) {
            username = req.cookie("username");
        }

        String session = null;

        if (req.cookies().containsKey("session")) {
            session = req.cookie("session");
        }

        if ((username == null) || (session == null)) {
            return null;
        }

        UserNode userNode = graph.getUserNodeByUsername(username);
        if (userNode == null) {
            return null;
        }
        else {
            if (userNode.checkSession(session)) {
                return userNode;
            }
            else {
                return null;
            }
        }
    }

    public static void log(Request req, String msg) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.ENGLISH);

        String ip = "";
        if (req != null)
            ip = req.ip();

        UserNode userNode = getUser(req);
        String username;
        if (userNode == null)
            username = "null";
        else
            username = userNode.getUsername();

        StringBuilder sb = new StringBuilder(100);
        sb.append("[");
        sb.append(df.format(new java.util.Date()));
        sb.append("] ");
        sb.append(ip);
        sb.append(" ");
        sb.append(username);
        sb.append(" - ");
        sb.append(msg);

        System.out.println(sb.toString());
        //logger.info(logLine);
    }
}