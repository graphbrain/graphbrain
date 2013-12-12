package com.graphbrain.web;

import com.graphbrain.db.*;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.LinkedList;
import java.util.List;

public class HandleDisambig extends Route {

    enum DisambigRequestType {
        DISAMBIG, CREATE, CHANGE
    }

    private DisambigRequestType type;

    public HandleDisambig(DisambigRequestType type, String route) {
        super(route);
        this.type = type;
    }

    @Override
    public Object handle(Request request, Response response) {
        switch(type) {
            case DISAMBIG:
                String text = request.queryParams("text");
                String mode = request.queryParams("mode");
                String rel = request.queryParams("rel");
                String participants = request.queryParams("participants");
                String spos = request.queryParams("pos");

                SearchInterface si = new SearchInterface(WebServer.graph);
                String[] results = si.query(text.toLowerCase());

                List<String[]> resultsList = new LinkedList<String[]>();
                for (String id : results)
                    resultsList.add(new String[]{id, WebServer.graph.description(id)});

                JSONObject json = new JSONObject();
                json.put("count", results.length);
                json.put("results", resultsList);
                json.put("mode", mode);
                json.put("text", text);
                json.put("rel", rel);
                json.put("participants", participants);
                json.put("pos", spos);
                return json.toString();

            case CREATE:
                UserNode userNode = WebServer.getUser(request);

                mode = request.queryParams("mode");
                text = request.queryParams("text");
                rel = request.queryParams("rel");
                participants = request.queryParams("participants");
                int pos = Integer.parseInt(request.queryParams("pos"));

                String[] participantIds = participants.split(" ");

                // undo previous connection
                if (mode.equals("change")) {
                    WebServer.graph.remove(Edge.fromParticipants(rel, participantIds), userNode.id);
                    // force consesnsus re-evaluation of affected edge
                    //Edge edge = Edge.fromParticipants(rel, participantIds);
                    //WebServer.consensusActor ! edge
                }

                // define new node
                si = new SearchInterface(WebServer.graph);
                results = si.query(text.toLowerCase());
                int number = results.length + 1;
                EntityNode newNode = EntityNode.fromNsAndText("" + number, text);

                // create revised edge
                Vertex[] participantNodes = new Vertex[participantIds.length + 1];
                for (int i = 0; i < participantIds.length; i++)
                    participantNodes[i + 1] = WebServer.graph.get(participantIds[i]);
                participantNodes[pos] = newNode;
                participantNodes[0] = new EdgeType(rel);
                WebServer.graph.createAndConnectVertices(participantNodes, userNode.id);

                // force consesnsus re-evaluation of affected edge
                //val edge = Edge.fromParticipants(rel, participantNodes.map(_.id))
                //WebServer.consensusActor ! edge

                WebServer.log(request, "DISAMBIG_CREATE mode: " + mode + "; text: " + text + "; rel: " + rel + "; participants:" + participants + "; pos: " + pos);

                return "";

            case CHANGE:
                userNode = WebServer.getUser(request);

                mode = request.queryParams("mode");
                rel = request.queryParams("rel");
                participants = request.queryParams("participants");
                pos = Integer.parseInt(request.queryParams("pos"));
                String changeTo = request.queryParams("changeto");

                participantIds = participants.split(" ");

                // undo previous connection
                if (mode.equals("change")) {
                    WebServer.graph.remove(Edge.fromParticipants(rel, participantIds), userNode.id);
                    // force consesnsus re-evaluation of affected edge
                    //val edge = Edge.fromParticipants(rel, participantIds)
                    //WebServer.consensusActor ! edge
                }

                // create revised edge
                participantIds[pos] = changeTo;
                WebServer.graph.put(Edge.fromParticipants(rel, participantIds), userNode.id);

                // force consesnsus re-evaluation of affected edge
                //Edge edge = Edge.fromParticipants(rel, participantIds);
                //WebServer.consensusActor ! edge

                WebServer.log(request, "DISAMBIG_CHANGE mode: " + mode + "; rel: " + rel + "; participants:" + participants + "; pos: " + pos + "; changeTo: " + changeTo);

                return "";
        }
        // should not happen
        return "";
    }
}