package com.graphbrain.eco;

import com.graphbrain.eco.nodes.WWRule;
import com.graphbrain.eco.nodes.WVRule;
import com.graphbrain.eco.nodes.ProgNode;
import com.graphbrain.db.Vertex;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Prog {

    private List<ProgNode> exprs;

    public Prog(List<ProgNode> exprs) {
        this.exprs = exprs;
    }

    public Prog() {
        this(new LinkedList<ProgNode>());
    }

    public List<Contexts> wv(String s, int depth) {
        return wv(s, depth, null);
    }

    public List<Contexts> wv(String s, int depth, Context globals) {
        return wv(Words.fromString(s), depth, globals);
    }

    public List<Contexts> wv(Words w, int depth, Context globals) {
        List<Contexts> ctxtsList = new LinkedList<>();

        for (ProgNode e : exprs) {
            if (e instanceof WVRule) {
                WVRule wv = (WVRule)e;
                Contexts ctxts = new Contexts(wv, this, w, depth);
                ctxts.setGlobals(globals);
                wv.eval(ctxts);
                if (ctxts.getCtxts().size() > 0) {
                    ctxtsList.add(ctxts);
                    return ctxtsList;
                }
            }
        }

        return ctxtsList;
    }

    public List<Contexts> ww(Words w, int depth, Context globals) {
        List<Contexts> ctxtsList = new LinkedList<>();

        for (ProgNode e : exprs) {
            if (e instanceof WWRule) {
                WWRule ww = (WWRule)e;
                Contexts ctxts = new Contexts(ww, this, w, depth);
                ctxts.setGlobals(globals);
                ww.eval(ctxts);
                if (ctxts.getCtxts().size() > 0) {
                    ctxtsList.add(ctxts);
                    return ctxtsList;
                }
            }
        }

        return ctxtsList;
    }

    public Vertex parse(String sentence) {
        List<Contexts> ctxtList = wv(sentence, 0);

        double maxStrength = Double.NEGATIVE_INFINITY;
        for (Contexts ctxts : ctxtList) {
            for (Context c : ctxts.getCtxts()) {
                double strength = c.getNumber("_strength");
                if (strength > maxStrength)
                    maxStrength = strength;
            }
        }

        for (Contexts ctxts : ctxtList) {
            for (Context c : ctxts.getCtxts()) {
                double strength = c.getNumber("_strength");

                if (strength == maxStrength) {
                    //System.out.println(ctxts.sentence);
                    return c.getTopRetVertex();
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        for (ProgNode e : exprs) {
            sb.append(e.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    private static boolean emptyLine(String line) {
        return ((line.equals("")) || (line.charAt(0) == ';'));
    }

    public static Prog load(String path) {
        try {
            String str = FileUtils.readFileToString(new File(path));
            return fromString(str);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Prog fromString(String str) {
        return fromStringArray(str.split("\\r?\\n"));
    }

    public static Prog fromStringArray(String[] strArr) {
        String exprStr = "";
        List<ProgNode> exprList = new LinkedList<>();

        for(String line : strArr) {
            if (emptyLine(line)) {
                if (!exprStr.isEmpty()) {
                    Parser p = new Parser(exprStr);
                    exprList.add(p.getExpr());
                }
                exprStr = "";
            }
            else {
                exprStr += line;
            }
        }

        if (!exprStr.isEmpty()) {
            Parser p = new Parser(exprStr);
            exprList.add(p.getExpr());
        }

        return new Prog(exprList);
    }

    public static Prog fromNode(ProgNode e) {
        List<ProgNode> l = new LinkedList<>();
        l.add(e);
        return new Prog(l);
    }
}