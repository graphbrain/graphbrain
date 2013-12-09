package com.graphbrain.web;

import com.graphbrain.db.UserNode;
import spark.Request;
import spark.Response;
import spark.Route;

public class HandleUser extends Route {
    public enum HandleUserType {
        SIGNUP, CHECK_USERNAME, CHECK_EMAIL, LOGIN
    }

    private HandleUserType type;

    public HandleUser(HandleUserType type, String route) {
        super(route);
        this.type = type;
    }

    @Override
    public Object handle(Request request, Response response) {
        switch(type) {
            case SIGNUP:
                String name = request.queryParams("name");
                String username = request.queryParams("username");
                String email = request.queryParams("email");
                String password = request.queryParams("password");
                WebServer.graph.createUser(username, name, email, password, "user");

                WebServer.log(request, "SIGNUP name: " + name + "; username: " + username + "; email:" + email);

                return "ok";
            case CHECK_USERNAME:
                username = request.queryParams("username");
                if (WebServer.graph.usernameExists(username)) {
                    return "exists " + username;
                }
                else {
                    return "ok " + username;
                }
            case CHECK_EMAIL:
                email = request.queryParams("email");
                if (WebServer.graph.emailExists(email)) {
                    return "exists " + email;
                }
                else {
                    return "ok " + email;
                }
            case LOGIN:
                String login = request.queryParams("login");
                password = request.queryParams("password");
                UserNode user = WebServer.graph.attemptLogin(login, password);
                if (user == null) {
                    WebServer.log(request, "FAILED LOGIN login: " + login + " passwd: " + password);
                    return "failed";
                }
                else {
                    WebServer.log(request, "LOGIN login: " + login);
                    return user.getUsername() + " " + user.getSession();
                }
            default:
                return "error";
        }
    }
}