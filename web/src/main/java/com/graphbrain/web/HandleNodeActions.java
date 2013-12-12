package com.graphbrain.web;

import com.graphbrain.db.Edge;
import com.graphbrain.db.UserNode;
import spark.Request;
import spark.Response;
import spark.Route;

public class HandleNodeActions extends Route {

    public HandleNodeActions(String route) {
        super(route);
    }

    public Object handle(Request request, Response response) {

        String op = request.queryParams("op");

        if (op.equals("remove")) {
            removeLinkOrNode(request);
        }

        HandleNode hn = new HandleNode("");
        return hn.handle(request, response);
    }

    private void removeLinkOrNode(Request request) {
        UserNode userNode = WebServer.getUser(request);
        String edgeString = request.queryParams("edge");

        Edge edge = Edge.fromId(edgeString);

        WebServer.graph.remove(edge, userNode.id);

        // force consesnsus re-evaluation of affected edge
        //WebServer.consensusActor ! edge

        WebServer.log(request, "REMOVE EDGE: " + edgeString);
    }
}
