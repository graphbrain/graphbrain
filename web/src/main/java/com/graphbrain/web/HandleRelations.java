package com.graphbrain.web;

import com.graphbrain.db.UserNode;
import com.graphbrain.db.Vertex;
import spark.Request;
import spark.Response;
import spark.Route;

public class HandleRelations extends Route {

    public HandleRelations(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {
        UserNode userNode = WebServer.getUser(request);
        String rel = request.queryParams("rel");
        int pos = Integer.parseInt(request.queryParams("pos"));
        String rootId = request.queryParams("rootId");
        Vertex root = WebServer.graph.get(rootId);

        WebServer.log(request, "REL rootId: " + rootId + "; edgeType: " + rel + "; pos: " + pos);

        return VisualGraph.generate(root.id, userNode);
    }
}