package com.graphbrain.web;

import com.graphbrain.db.SearchInterface;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HandleSearch extends Route {

    public HandleSearch(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {
        String query = request.queryParams("q");
        SearchInterface si = new SearchInterface(WebServer.graph);
        String[] results = si.query(query);

        List<List<String>> resultsList = new LinkedList<List<String>>();

        for (String id : results)
            resultsList.add(Arrays.asList(id, WebServer.graph.description(id)));

        JSONObject json = new JSONObject();
        json.put("count", results.length);
        json.put("results", resultsList);

        WebServer.log(request, "SEARCH query: " + query + "; results: " + results.length);

        return json.toString();
    }
}