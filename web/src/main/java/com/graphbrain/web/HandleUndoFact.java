package com.graphbrain.web;

import com.graphbrain.db.Edge;
import com.graphbrain.db.SearchInterface;
import com.graphbrain.db.UserNode;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HandleUndoFact extends Route {

    public HandleUndoFact(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {
        UserNode userNode = WebServer.getUser(request);

        String rel = request.queryParams("rel");
        String participants = request.queryParams("participants");

        String[] participantIds = participants.split(" ");

        // undo connection
        WebServer.graph.remove(Edge.fromParticipants(rel, participantIds), userNode.id);
        // force consesnsus re-evaluation of affected edge
        //val edge = Edge.fromParticipants(rel, participantIds)
        //WebServer.consensusActor ! edge

        return "";
    }
}