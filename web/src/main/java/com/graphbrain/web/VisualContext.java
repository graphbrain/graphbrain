package com.graphbrain.web;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Words;
import com.graphbrain.eco.Word;

public class VisualContext {

    private Context ctxt;
    private String targetVertex;

    private String htmlWords;
    private String htmlContext;
    private String htmlVertex;

    public VisualContext(Context ctxt, String targetVertex) {
        this.ctxt = ctxt;
        this.targetVertex = targetVertex;

        htmlWords = renderWords(ctxt.getParent().getSentence());
        htmlContext = renderContext(ctxt);
        htmlVertex = ctxt.getTopRetVertex().toString();
    }

    public VisualContext(Context ctxt) {
        this(ctxt, "");
    }

    private String renderContext(Context ctxt) {
        return renderContext(ctxt, 0);
    }

    private String renderContext(Context ctxt, int indent) {
        String r = "";

        for (int i = 0; i < indent; i++) {
            r += "&nbsp;&nbsp;&nbsp;&nbsp;";
        }

        r += "<span class=\"text-success\">" + ctxt.getParent().getRule().getName() + "</span>";
        r += " (" + renderWords(ctxt.getParent().getSentence()) + ") <br />";

            for (Context sctxt : ctxt.getSubContexts())
                r += renderContext(sctxt, indent + 1);

        return r;
    }

    private String renderWord(Word word) {
        return word.getWord() + " <span class=\"text-primary\">[" + word.getPos() + "]</span>";
    }

    private String renderWords(Words words) {
        String r = "";
        boolean first = true;
        for (Word w : words.getWords()) {
            if (first)
                first = false;
            else
                r += " ";
            r += renderWord(w);
        }
        return r;
    }

    public boolean isTest() {
        return !targetVertex.isEmpty();
    }

    public boolean correct() {
        return targetVertex.equals(ctxt.getTopRetVertex().toString());
    }

    public String getHtmlWords() {
        return htmlWords;
    }

    public String getHtmlContext() {
        return htmlContext;
    }

    public String getHtmlVertex() {
        return htmlVertex;
    }
}
