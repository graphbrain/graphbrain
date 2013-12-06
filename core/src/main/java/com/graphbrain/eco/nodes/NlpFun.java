package com.graphbrain.eco.nodes;

import com.graphbrain.eco.*;
import com.graphbrain.eco.NodeType;

public class NlpFun extends FunNode {

    public enum NlpFunType {
        IS_POS, IS_POSPRE, ARE_POS, ARE_POSPRE, CONTAINS_POS, CONTAINS_POSPRE, IS_LEMMA
    }

    private NlpFunType fun;

    public NlpFun(NlpFunType fun, ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
        this.fun = fun;
    }

    public NlpFun(NlpFunType fun, ProgNode[] params) {
        this(fun, params, -1);
    }

    @Override
    public String label() {
        switch(fun) {
            case IS_POS: return "is-pos";
            case IS_POSPRE: return "is-pos-pre";
            case ARE_POS: return "are-pos";
            case ARE_POSPRE: return "are-pos-pre";
            case CONTAINS_POS: return "contains-pos";
            case CONTAINS_POSPRE: return "contains-pos-pre";
            case IS_LEMMA: return "is-lemma";
        }
        // error
        return "?";
    }

    @Override
    public NodeType ntype(){return NodeType.Boolean;}

    private boolean matchPoses(Word word, boolean pre, String[] poses) {
        for (String pos : poses)
            if (pre)
                if (word.getPos().startsWith(pos))
                    return true;
            else
                if (word.getPos().equals(pos))
                    return true;

        return false;
    }

    private boolean matchPoses(Words words, boolean pre, Context ctxt) {
        String[] poses = new String[params.length - 1];
        for (int i = 1; i < params.length; i++) {
            poses[i - 1] = ctxt.getRetString(params[i]);
        }

        for (Word word : words.getWords())
            if (!matchPoses(word, pre, poses))
                return false;

        return true;
    }

    private boolean containsPoses(Words words, boolean pre, Context ctxt) {
        String[] poses = new String[params.length - 1];
        for (int i = 1; i < params.length; i++) {
            poses[i - 1] = ctxt.getRetString(params[i]);
        }

        for (Word word : words.getWords())
            if (matchPoses(word, pre, poses))
                return true;

        return false;
    }

    @Override
    public void booleanValue(Contexts ctxts) {
        params[0].wordsValue(ctxts);

        for (int i = 1; i < params.length; i++) {
            params[i].stringValue(ctxts);
        }

        for (Context c : ctxts.getCtxts()) {
            Words words = c.getRetWords(params[0]);

            boolean ret;
            switch(fun) {
                case IS_POS:
                    ret = words.length() == 1 && matchPoses(words, false, c);
                    c.setRetBoolean(this, ret);
                    break;
                case IS_POSPRE:
                    ret = words.length() == 1 && matchPoses(words, true, c);
                    c.setRetBoolean(this, ret);
                    break;
                case ARE_POS:
                    ret = words.length() != 0 && matchPoses(words, false, c);
                    c.setRetBoolean(this, ret);
                    break;
                case ARE_POSPRE:
                    ret = words.length() != 0 && matchPoses(words, true, c);
                    c.setRetBoolean(this, ret);
                    break;
                case CONTAINS_POS:
                    ret = words.length() != 0 && containsPoses(words, false, c);
                    c.setRetBoolean(this, ret);
                    break;
                case CONTAINS_POSPRE:
                    ret = words.length() != 0 && containsPoses(words, true, c);
                    c.setRetBoolean(this, ret);
                    break;
                case IS_LEMMA:
                    ret = words.length() == 1 && words.getWords()[0].getLemma().equals(c.getRetString(params[1]));
                    c.setRetBoolean(this, ret);
                    break;
            }
        }
    }
}