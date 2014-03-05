package com.graphbrain.eco.nodes;

import com.graphbrain.eco.*;
import com.graphbrain.eco.NodeType;

public class VarNode extends ProgNode {

    private String name;
    private String[] possiblePOS;
    private String[] necessaryPOS;
    private String[] forbiddenPOS;
    boolean external;

    public VarNode(String varStr, int lastTokenPos) {
        super(lastTokenPos);

        String[] parts = varStr.split(":");
        name = parts[0];
        external = name.charAt(0) == '$';

        String[] constStrs;
        if (parts.length > 1)
            constStrs = parts[1].split("\\|");
        else
            constStrs = new String[]{};

        int count = 0;
        for (String c : constStrs) {
            if (c.charAt(0) != '-')
                count++;
        }

        possiblePOS = new String[count];

        int pos = 0;
        for (String c : constStrs) {
            if (c.charAt(0) != '-') {
                if (c.charAt(0) == '+')
                    possiblePOS[pos] = c.substring(1);
                else
                    possiblePOS[pos] = c;

                pos++;
            }
        }


        count = 0;
        for (String c : constStrs) {
            if (c.charAt(0) == '+')
                count++;
        }

        necessaryPOS = new String[count];

        pos = 0;
        for (String c : constStrs) {
            if (c.charAt(0) == '+') {
                necessaryPOS[pos] = c.substring(1);
                pos++;
            }
        }

        count = 0;
        for (String c : constStrs) {
            if (c.charAt(0) == '-')
                count++;
        }

        forbiddenPOS = new String[count];

        pos = 0;
        for (String c : constStrs) {
            if (c.charAt(0) == '-') {
                forbiddenPOS[pos] = c.substring(1);
                pos++;
            }
        }
    }

    @Override
    public NodeType ntype(Context ctxt){return ctxt.getType(name);}

    @Override
    public void eval(Contexts ctxts) {
        for (Context c : ctxts.getCtxts()) {
            VariableContainer vc;
            NodeType nt;
            if (external) {
                vc = ctxts.getProg();
                nt = ctxts.getProg().getType(name);
            }
            else {
                vc = c;
                nt = ntype(c);
            }

            switch(nt) {
                case Boolean:
                    c.setRetBoolean(this, vc.getBoolean(name));
                    break;
                case String:
                    c.setRetString(this, vc.getString(name));
                    break;
                case Number:
                    c.setRetNumber(this, vc.getNumber(name));
                    break;
                case Words:
                    c.setRetWords(this, vc.getWords(name));
                    break;
                case Vertex:
                    c.setRetVertex(this, vc.getVertex(name));
                    break;
            }
        }
    }

    @Override
    public String toString() {return name;}

    public String getName() {
        return name;
    }

    public String[] getPossiblePOS() {
        return possiblePOS;
    }

    public String[] getNecessaryPOS() {
        return necessaryPOS;
    }

    public String[] getForbiddenPOS() {
        return forbiddenPOS;
    }
}