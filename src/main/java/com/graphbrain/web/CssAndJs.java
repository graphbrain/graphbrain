package com.graphbrain.web;

import java.util.Random;

public class CssAndJs {

    public static Random rand = new Random();

    public static String randomVersion() {
        return "" + rand.nextInt(999999999);
    }

    public CssAndJs() {
        super();
    }

    public static String version= "130113";

    public String analyticsJs() {
        return "<script type=\"text/javascript\">\n"
            + "var _gaq = _gaq || [];\n"
            + "_gaq.push(['_setAccount', 'UA-30917836-1']);\n"
            + "_gaq.push(['_trackPageview']);\n"
            + "(function() {\n"
            + "var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\n"
            + "ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';\n"
            + "var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);\n"
            + "})();\n"
            + "</script>\n";
    }

    public String cssAndJs() {
        return "<link href=\"/css/main.css?" + randomVersion() + " type=\"text/css\" rel=\"Stylesheet\" />"
            + "<script src=\"/js/jquery-1.7.2.min.js\" type=\"text/javascript\" ></script>"
            + "<script src=\"/js/jquery-ui-1.8.18.custom.min.js\" type=\"text/javascript\" ></script>"
            + "<script src=\"/js/bootstrap.min.js\" type=\"text/javascript\" ></script>"
            + "<script src=\"/js/gb.js?" + randomVersion() + " type=\"text/javascript\" ></script>";
    }
}