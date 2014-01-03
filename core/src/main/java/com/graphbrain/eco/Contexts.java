package com.graphbrain.eco;

import com.graphbrain.eco.nodes.RuleNode;

import java.util.LinkedList;
import java.util.List;

public class Contexts {
    private Words sentence;
    private RuleNode rule;
    private Prog prog;
    private int depth;
    private List<Context> ctxts;
    private List<Context> addCtxts;
    private List<Context> remCtxts;

    private Context globals;

    public Contexts(RuleNode rule, Prog prog, Words inSentence, int depth) {
        this.rule = rule;
        this.prog = prog;
        this.depth = depth;
        sentence = inSentence.removeFullStop();
        ctxts = new LinkedList<>();
        addCtxts = new LinkedList<>();
        remCtxts = new LinkedList<>();
        globals = null;
    }

    public void addContext(Context c) {addCtxts.add(c);}
    public void remContext(Context c) {remCtxts.add(c);}

    public void applyChanges() {
        for (Context c : addCtxts)
            ctxts.add(c);
        for (Context c : remCtxts)
            ctxts.remove(c);

        addCtxts.clear();
        remCtxts.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("Contexts: ");
        sb.append(sentence);
        for (Context c : ctxts)
            sb.append(c);

        return sb.toString();
    }

    public Words getSentence() {
        return sentence;
    }

    public RuleNode getRule() {
        return rule;
    }

    public Prog getProg() {
        return prog;
    }

    public int getDepth() {
        return depth;
    }

    public List<Context> getCtxts() {
        return ctxts;
    }

    public Context getGlobals() {
        return globals;
    }

    public void setGlobals(Context globals) {
        this.globals = globals;
    }
}