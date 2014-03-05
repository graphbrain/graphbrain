package com.graphbrain.eco.nodes;

public abstract class ListNode extends ProgNode {

    protected ProgNode[] params;

    public ListNode(ProgNode[] params, int lastTokenPos) {
        super(lastTokenPos);
        this.params = params;
    }

    public ListNode(ProgNode[] params) {
        this(params, -1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        sb.append("(");
        for (int i = 0; i < params.length; i ++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(params[i].toString());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListNode) {
            ListNode l = (ListNode)obj;
            for (int i = 0; i < params.length; i ++) {
                if (!params[i].equals(l.params[i])) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }
}