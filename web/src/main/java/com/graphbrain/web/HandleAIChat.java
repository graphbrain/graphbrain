package com.graphbrain.web;

import com.graphbrain.db.Edge;
import com.graphbrain.db.UserNode;
import com.graphbrain.db.Vertex;
import com.graphbrain.db.VertexType;
import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.Prog;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HandleAIChat extends Route {

    private Prog prog;

    public HandleAIChat(String route) {
        super(route);

        File file = new File("eco/chat.eco");
        String progStr = "";
        try {
            progStr = FileUtils.readFileToString(file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        prog = Prog.fromString(progStr, WebServer.graph);
    }

    @Override
    public Object handle(Request request, Response response) {
        UserNode user = WebServer.getUser(request);
        String sentence = request.queryParams("sentence");
        Vertex root = WebServer.graph.get(request.queryParams("rootId"));

        prog.setVertex("$user", user);
        prog.setVertex("$root", root);

        WebServer.log(request, "user said: " + sentence);

        Vertex v = null;

        List<Contexts> ctxtsList = prog.wv(sentence, 0);

        for (Contexts ctxts : ctxtsList) {
            for (Context ctxt : ctxts.getCtxts()) {
                v = ctxt.getTopRetVertex();
            }
        }

        WebServer.graph.put(v, user.id);

        String gotoId = root.id;
        if (v != null && v.type() == VertexType.Edge) {
            Edge e = (Edge)v;
            gotoId = e.getIds()[1];
        }

        JSONObject json = new JSONObject();
        json.put("sentence", v.id);
        json.put("newedges", new String[]{v.id});
        json.put("goto", gotoId);

        return json.toString();
    }
}