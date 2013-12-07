package com.graphbrain.web;

import com.graphbrain.db.Graph;
import static spark.Spark.*;

public class WebServer {

    public static Graph graph = new Graph();

    public static void main(String[] args) {

        staticFileLocation("/");
        get(new HandleEco("/eco"));
        post(new HandleEco("/eco"));
    }
}