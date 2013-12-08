package com.graphbrain.web;

import com.graphbrain.db.UserNode;

public class NavBar {

    private UserNode user;
    private String page;

    public NavBar(UserNode user, String page) {
        this.user = user;
        this.page = page;
    }

    public String userId() {
        if (user == null)
            return "";
        else
            return user.id;
    }

    public String tools() {
        if (page.equals("node")) {
            return "<li><button class=\"btn\" id=\"ai-chat-button\" data-toggle=\"button\"><i class=\"icon-asterisk icon-black\"></i> Talk to AI</button></li>";
        }
        else {
            return "";
        }
    }

    private String userStuff() {
        if (user == null) {
            return "<div class=\"pull-right\">"
                + "<ul class=\"nav\">"
                + "<li><a class=\"signupLink\" href=\"#\">Sign Up</a></li>"
                + "<li><a id=\"loginLink\" href=\"#\">Login</a></li>"
                + "</ul>"
                + "</div>";
        }
        else {
                return "<div class=\"pull-right\">"
                    + "<ul class=\"nav\">"
                    + tools()
                    + "<li class=\"dropdown\">"
                    + "<a href=\"#\" id=\"current-context\" class=\"dropdown-toggle\" data-toggle=\"dropdown\"><i class=\"icon-globe icon-black\"></i> Global Context <b class=\"caret\"></b></a>"
                    + "<ul id=\"contexts-dropdown\" class=\"dropdown-menu\">"
                    + "<li><a id=\"createContextLink\" href=\"#\">Create Context</a></li>"
                    + "<li class=\"divider\"></li>"
                    + "<li><a href=\"/node/user/" + user.getUsername() +  ">Global Context</a></li>"
                    + "</ul>"
                    + "</li>"
                    + "<li class=\"dropdown\">"
                    + "<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\"><i class=\"icon-user icon-black\"></i> "
                    + user.getName()
                    + " <b class=\"caret\"></b></a>"
                    + "<ul class=\"dropdown-menu\">"
                    + "<li><a href=\"/about\">About GraphBrain</a></li>"
                    + "<li><a href=\"/node/user/\""
                    + user.getUsername()
                    +  "\">Home</a></li>"
                    + "<li><a href=\"#\" id=\"logoutLink\">Logout</a></li>"
                    + "</ul>"
                    + "</li>"
                    + "</ul>"
                    + "</div>";
        }
    }

    public String html() {
        if ((page.equals("comingsoon")) || (page.equals("home"))) {
            return "";
        }
        else {
            return "<div class=\"navbar navbar-fixed-top\">"
                + "<div class=\"navbar-inner\">"
                + "<div class=\"container-fluid\">"
                + "<a class=\"btn btn-navbar\" data-toggle=\"collapse\" data-target=\".nav-collapse\">"
                + "<span class=\"icon-bar\"></span>"
                + "<span class=\"icon-bar\"></span>"
                + "<span class=\"icon-bar\"></span>"
                + "</a>"
                + "<a href=\"/\"><img src=\"/images/GB_logo_XS.png\" class=\"brand\" alt=\"graphbrain\" /></a>"
                + "<div class=\"nav-collapse\">"
                + "<ul class=\"nav\">"
                + "<li><form class=\"navbar-search\" id=\"search-field\">"
                + "<input type=\"text\" id=\"search-input-field\" placeholder=\"Search\" />"
                + "</form></li>"
                + "</ul>"
                + "</div>"
                + userStuff()
                + "</div>"
                + "</div>"
                + "<div id=\"alert\" class=\"alert\" style=\"visibility:hidden; margin:0px\">"
                + "<div id=\"alertMsg\"></div>"
                + "</div>"
                + "</div>";
        }
    }
}