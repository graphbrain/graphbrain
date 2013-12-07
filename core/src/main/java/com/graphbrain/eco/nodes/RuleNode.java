package com.graphbrain.eco.nodes;

public abstract class RuleNode extends FunNode {

    private String name;

    public RuleNode(ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);

        ProgNode[] pattern = ((PatFun)params[0]).params;
        StringBuilder sb = new StringBuilder(50);
        for (int i = 0; i < pattern.length; i++) {
            if (i > 0) {
                sb.append("-");
            }
            sb.append(pattern[i].toString());
        }
        name = sb.toString();
    }

    public RuleNode(ProgNode[] params) {
        this(params, -1);
    }

    public String getName() {
        return name;
    }
}