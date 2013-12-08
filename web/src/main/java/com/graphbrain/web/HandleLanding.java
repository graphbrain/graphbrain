package com.graphbrain.web;

import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityRoute;

import java.util.HashMap;
import java.util.Map;

public class HandleLanding extends VelocityRoute {

    public HandleLanding(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("title", "Welcome");
        attributes.put("navBar", new NavBar(null, "home").html());
        attributes.put("cssAndJs", new CssAndJs().cssAndJs());
        attributes.put("loggedIn", false);
        //attributes.put("html", html);


        return modelAndView(attributes, "velocity/template/landing.wm");
    }
}