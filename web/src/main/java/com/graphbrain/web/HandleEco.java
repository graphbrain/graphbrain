package com.graphbrain.web;

import com.graphbrain.db.ProgNode;
import com.graphbrain.db.TextNode;
import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.Prog;
import com.graphbrain.eco.Text;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityRoute;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HandleEco extends VelocityRoute {
    public HandleEco(String route) {
        super(route);
    }

    @Override
    public Object handle(Request request, Response response) {
        String text = "";

        if (request.requestMethod().equals("post")) {
            text = request.queryParams("text");
        }
        return renderParser(request, text);
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

    private ModelAndView renderParser(Request request, String parseText) {

        String text;

        if (parseText.isEmpty()) {
            if (request.cookies().containsKey("parse_text"))
                text = request.cookie("parse_test");
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

        return modelAndView(attributes, "velocity/template/ecoparse.wm");
    }

    /*
    private def renderCode(req: HttpRequest[Any]) = {
        val code = getCode

        Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
        Scalate(req, "ecocode.ssp", ("title", "Code"), ("code", code))(WebServer.engine)
    }

    private def renderRunTests(req: HttpRequest[Any], run: Boolean) = {
        var visualCtxtList = List[VisualContext]()

        if (run) {
        val testData = getTests
        val tests = new Tests(testData)

        val p = Prog.fromString(getCode)

        for (t: Array[String] <- tests.getTests.toArray) {
        val ctxtsList = p.wv(t(0), 0)
        for (ctxts: Contexts <- ctxtsList.toArray) {
        for (ctxt <- ctxts.getCtxts) {
        visualCtxtList ::= new VisualContext(ctxt, t(1))
        }
        }
        }
        }

        Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
        Scalate(req, "ecoruntests.ssp", ("title", "Run Tests"), ("ctxtList", visualCtxtList.reverse))(WebServer.engine)
        }

private def renderEditTests(req: HttpRequest[Any]) = {
        val tests = getTests

        Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
        Scalate(req, "ecoedittests.ssp", ("title", "Edit Tests"), ("tests", tests))(WebServer.engine)
        }
        */
}