package com.graphbrain.eco.nodes;

import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

class FilterFun extends FunNode {

    enum FilterFunType {
        FilterMin, FilterMax
    }

    private FilterFunType fun;

    public FilterFun(FilterFunType fun, ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
        this.fun = fun;
    }

    public FilterFun(FilterFunType fun, ProgNode[] params) {
        this(fun, params, -1);
    }

    @Override
    public String label() {
        switch(fun) {
            case FilterMin: return "filter-min";
            case FilterMax: return "filter-max";
        }
        // error
        return "?";
    }

    @Override
    public NodeType ntype(){return NodeType.Boolean;}

    @Override
    public void booleanValue(Contexts ctxts) {
        params[0].numberValue(ctxts);

        Context bestCtxt = null;

        double bestVal = 0;
        switch(fun) {
            case FilterMin:
                bestVal = Double.POSITIVE_INFINITY;
                break;
            case FilterMax:
                bestVal = Double.NEGATIVE_INFINITY;
                break;
        }

        for (Context c : ctxts.getCtxts()) {
            double value = c.getRetNumber(params[0]);

            switch(fun) {
                case FilterMin:
                    if (value < bestVal) {
                        bestCtxt = c;
                        bestVal = value;
                    }
                    break;
                case FilterMax:
                    if (value > bestVal) {
                        bestCtxt = c;
                        bestVal = value;
                    }
                    break;
            }
        }

        for (Context c : ctxts.getCtxts()) {
            if (bestCtxt == null) {
                c.setRetBoolean(this, false);
            }
            else {
                c.setRetBoolean(this, c == bestCtxt);
            }
        }
    }
}