package com.graphbrain.web;

import com.graphbrain.db.Graph;
import static spark.Spark.*;

public class WebServer {

    public static Graph graph = new Graph();

    public static void main(String[] args) {

        staticFileLocation("/");

        get(new HandleLanding("/"));

        get(new HandleEco(HandleEco.HandleEcoType.PARSER, "/eco"));
        post(new HandleEco(HandleEco.HandleEcoType.PARSER, "/eco"));
        get(new HandleEco(HandleEco.HandleEcoType.CODE, "/eco/code"));
        post(new HandleEco(HandleEco.HandleEcoType.CODE, "/eco/code"));
        get(new HandleEco(HandleEco.HandleEcoType.EDIT_TESTS, "/eco/edittests"));
        post(new HandleEco(HandleEco.HandleEcoType.EDIT_TESTS, "/eco/edittests"));
        get(new HandleEco(HandleEco.HandleEcoType.RUN_TESTS, "/eco/runtests"));
        post(new HandleEco(HandleEco.HandleEcoType.RUN_TESTS, "/eco/runtests"));
    }
}