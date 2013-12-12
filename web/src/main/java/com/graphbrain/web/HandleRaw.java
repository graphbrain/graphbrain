package com.graphbrain.web;

import com.graphbrain.db.Edge;
import com.graphbrain.db.UserNode;
import com.graphbrain.db.Vertex;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityRoute;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HandleRaw extends VelocityRoute {

    public HandleRaw(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {

        String id = request.splat()[0];

        UserNode userNode = WebServer.getUser(request);
        Vertex vertex;
        try {
            vertex = WebServer.graph.get(URLDecoder.decode(id, "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        WebServer.log(request, "RAW id: " + id);

        String html = "<h2>Vertex: " + vertex.id + "</h2>";

        html += vertex.raw();

        String userId = null;

        if (userNode != null)
            userId = userNode.id;

        Set<Edge> edges = WebServer.graph.edges(vertex.id, userId);
        for (Edge e : edges)
            html += e.id + "<br />";

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("title", vertex.toString());
        attributes.put("navBar", new NavBar(userNode, "raw").html());
        attributes.put("cssAndJs", new CssAndJs().cssAndJs());
        attributes.put("loggedIn", false);
        attributes.put("js", "");
        attributes.put("html", html);


        return modelAndView(attributes, "velocity/template/raw.wm");
    }
}