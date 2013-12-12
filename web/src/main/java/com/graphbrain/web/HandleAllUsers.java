package com.graphbrain.web;

import com.graphbrain.db.UserNode;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityRoute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandleAllUsers extends VelocityRoute {

    public HandleAllUsers(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {

        UserNode userNode = WebServer.getUser(request);

        String html = "<h2>All Users</h2>";

        List<UserNode> users = WebServer.graph.allUsers();

        html += "<strong>Count:" + users.size() + "</strong><br /><br />";

        for (UserNode u : users)
            html += "<a href='/node/user/"
                    + u.getUsername() + "'>"
                    + u.getUsername() + "</a> "
                    + u.getName()
                    + " "
                    + u.getEmail()
                    + " "
                    + u.getPwdhash()
                    + "<br />";

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("title", "all users");
        attributes.put("navBar", new NavBar(userNode, "allusers").html());
        attributes.put("cssAndJs", new CssAndJs().cssAndJs());
        attributes.put("loggedIn", false);
        attributes.put("js", "");
        attributes.put("html", html);

        return modelAndView(attributes, "velocity/template/raw.wm");
    }
}