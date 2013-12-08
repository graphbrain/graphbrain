package com.graphbrain.web;

import com.graphbrain.db.ProgNode;
import com.graphbrain.db.TextNode;
import com.graphbrain.db.Vertex;
import com.graphbrain.eco.*;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityRoute;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HandleEco extends VelocityRoute {
    public enum HandleEcoType {
        PARSER, CODE, EDIT_TESTS, RUN_TESTS
    }

    private HandleEcoType type;

    public HandleEco(HandleEcoType type, String route) {
        super(route);
        this.type = type;
    }

    @Override
    public Object handle(Request request, Response response) {
        switch(type) {
            case PARSER:
                String text = "";

                if (request.requestMethod().equals("post")) {
                    text = request.queryParams("text");
                }
                return renderParser(request, response, text);
            case CODE:
                if (request.requestMethod().equals("post")) {
                    WebServer.graph.put(new ProgNode("prog/prog", request.queryParams("code")));
                }
                return renderCode();
            case EDIT_TESTS:
                if (request.requestMethod().equals("post")) {
                    WebServer.graph.put(new TextNode("text/tests", request.queryParams("tests")));
                }
                return renderEditTests();
            case RUN_TESTS:
                return renderRunTests(request.requestMethod().equals("post"));
            default:
                return "error";
        }
    }

    private String getCode() {
        ProgNode prog = WebServer.graph.getProgNode("prog/prog");
        if (prog == null)
            return "";
        else
            return prog.getProg();
    }

    private String getTests() {
        TextNode tests = WebServer.graph.getTextNode("text/tests");
        if (tests == null)
            return "";
        else
            return tests.getText();
    }

    private ModelAndView renderParser(Request request, Response response, String parseText) {

        String text;

        if (parseText.isEmpty()) {
            if (request.cookies().containsKey("parse_text"))
                text = request.cookie("parse_text");
            else
                text = "";
        }
        else {
            text = parseText;
        }

        List<VisualContext> visualCtxtList = new LinkedList<VisualContext>();
        if (!text.isEmpty()) {
            Text t = new Text(text);

            Prog p = Prog.fromString(getCode());
            //System.out.println(p);

            for (String s : t.getSentences()) {
                List<Contexts> ctxtsList = p.wv(s, 0);
                for (Contexts ctxts : ctxtsList) {
                    for (Context ctxt : ctxts.getCtxts()) {
                        visualCtxtList.add(new VisualContext(ctxt));
                    }
                }
            }
        }

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("title", "Parse");
        attributes.put("text", text);
        attributes.put("ctxtList", visualCtxtList);

        response.cookie("parse_text", text);
        return modelAndView(attributes, "velocity/template/ecoparse.wm");
    }

    private ModelAndView renderCode() {
        String code = getCode();

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("title", "Code");
        attributes.put("code", code);

        return modelAndView(attributes, "velocity/template/ecocode.wm");
    }

    private ModelAndView renderEditTests() {
        String tests = getTests();

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("title", "Edit Tests");
        attributes.put("tests", tests);

        return modelAndView(attributes, "velocity/template/ecoedittests.wm");
    }

    private ModelAndView renderRunTests(boolean run) {
        List<VisualContext> visualCtxtList = new LinkedList<VisualContext>();

        if (run) {
            String testData = getTests();
            Tests tests = new Tests(testData);

            Prog p = Prog.fromString(getCode());

            for (String[] t : tests.getTests()) {
                List<Contexts> ctxtsList = p.wv(t[0], 0);
                for (Contexts ctxts : ctxtsList) {
                    for (Context ctxt : ctxts.getCtxts()) {
                        visualCtxtList.add(new VisualContext(ctxt, t[1]));
                    }
                }
            }
        }

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("title", "Run Tests");
        attributes.put("ctxtList", visualCtxtList);

        return modelAndView(attributes, "velocity/template/ecoruntests.wm");
    }
}