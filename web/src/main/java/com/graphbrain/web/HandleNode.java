package com.graphbrain.web;

import com.graphbrain.db.*;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityRoute;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class HandleNode extends VelocityRoute {

    public HandleNode(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {

        String id = request.splat()[0];

        WebServer.log(request, "NODE id: " + id);

        UserNode userNode = WebServer.getUser(request);
        Vertex node;
        try {
            node = WebServer.graph.get(URLDecoder.decode(id, "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        String errorMsg = "";

        System.out.println("node: " + node);
        System.out.println("userNode: " + userNode);

        String js = "var data = " + VisualGraph.generate(node.id, userNode) + ";\n" +
                    "var errorMsg = \"" + errorMsg + "\";\n";

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("title", node.label());
        attributes.put("navBar", new NavBar(userNode, "node").html());
        attributes.put("cssAndJs", new CssAndJs().cssAndJs());
        attributes.put("loggedIn", false);
        attributes.put("js", js);


        return modelAndView(attributes, "velocity/template/node.wm");
    }
}