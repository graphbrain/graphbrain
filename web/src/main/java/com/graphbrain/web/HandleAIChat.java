package com.graphbrain.web;

import spark.Request;
import spark.Response;
import spark.Route;

public class HandleAIChat extends Route {

    public HandleAIChat(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {
        /*
        val userNode = WebServer.getUser(cookies)
        val sentence = params("sentence")(0)
        val rootId = params("rootId")(0)
        val root = WebServer.graph.get(rootId)
        responderActor ! AIChatResponderActor.Sentence(sentence, root, userNode, req)

        WebServer.log(req, cookies, "AI CHAT sentence: " + sentence + "; rootId: " + rootId)
        */

        return "";
    }
}