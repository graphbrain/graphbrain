package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;
import com.graphbrain.eco.nodes.patterns.PatternElem;
import com.graphbrain.eco.nodes.patterns.StrPatternElem;
import com.graphbrain.eco.nodes.patterns.VarPatternElem;

import java.util.Arrays;

public class PatFun extends FunNode {

    private PatternElem[] elems;
    private PatternElem first;
    PatternElem prev;
    PatternElem next;

    public PatFun(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);

        elems = new PatternElem[params.length];

        int i = 0;
        for (ProgNode p : params) {
            if (p instanceof StringNode) {
                StringNode s = (StringNode)p;
                elems[i] = new StrPatternElem(s.getValue());
            }
            else if (p instanceof VarNode) {
                VarNode v = (VarNode)p;
                elems[i] = new VarPatternElem(v.getName(),
                        v.getPossiblePOS(),
                        v.getNecessaryPOS(),
                        v.getForbiddenPOS());
            }

            i++;
        }

        first = elems[0];

        // init elements
        for (i = 0; i < elems.length; i++) {
            if (i == 0)
                prev = null;
            else
                prev = elems[i - 1];
            if (i == elems.length - 1)
                next = null;
            else
                next = elems[i + 1];

            elems[i].init(i, elems.length, prev, next);
        }

        /*
        System.out.println("---------------------------");
        for (PatternElem x : elems) {
            System.out.println(x.toString());
        }
        */

        // order by priority
        Arrays.sort(elems);
    }

    public PatFun(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String label() {return "pat";}

    @Override
    public NodeType ntype() {return NodeType.Boolean;}

    @Override
    public void booleanValue(Contexts ctxts) {
        int words = ctxts.getSentence().getWords().length;
        int count = params.length;

        if (count > words)
            return;

        PatternElem e = first;
        while (e != null) {
            e.setSentence(ctxts.getSentence());
            e = e.getNextElem();
        }

        /*
        System.out.println("####################################");
        for (ProgNode p : params) System.out.print(p.toString() + "-");
        System.out.println("\n");
        */
        matches(ctxts, 0);

        ctxts.applyChanges();
    }

    private void matches(Contexts ctxts, int pos) {

        elems[pos].setFixed(true);

        elems[pos].rewind();
        while (elems[pos].next()) {
            if (pos == elems.length - 1) {
                // match found
                //printMatch();
                addContext(ctxts);
            }
            else {
                matches(ctxts, pos + 1);
            }
        }

        elems[pos].setFixed(false);
    }

    public void addContext(Contexts ctxts) {
        Context newContext = new Context(ctxts);

        for (PatternElem e : elems) {
            if (e instanceof VarPatternElem) {
                VarPatternElem v = (VarPatternElem)e;
                newContext.setWords(v.getName(), v.curWords());
            }
        }

        ctxts.addContext(newContext);
        newContext.setRetBoolean(this, true);
    }

    public void printMatch() {
        for (PatternElem e : elems) {
            System.out.println(e.toString());
        }
    }
}