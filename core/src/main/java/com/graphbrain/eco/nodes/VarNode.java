package com.graphbrain.eco.nodes;

import com.graphbrain.eco.*;
import com.graphbrain.eco.NodeType;

public class VarNode extends ProgNode {

    private String name;
    private String[] possiblePOS;
    private String[] necessaryPOS;
    private String[] forbiddenPOS;

    public VarNode(String name,
        String[] possiblePOS,
        String[] necessaryPOS,
        String[] forbiddenPOS,
        int lastTokenPos) {

        super(lastTokenPos);

        this.name = name;
        this.possiblePOS = possiblePOS;
        this.necessaryPOS = necessaryPOS;
        this.forbiddenPOS = forbiddenPOS;
    }

    public VarNode(String name,
                   String[] possiblePOS,
                   String[] necessaryPOS,
                   String[] forbiddenPOS) {

        this(name, possiblePOS, necessaryPOS, forbiddenPOS, -1);
    }

    public VarNode(String varStr, int lastTokenPos) {
        super(lastTokenPos);

        String[] parts = varStr.split(":");
        name = parts[0];

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
            switch(ntype(c)) {
                case Boolean:
                    c.setRetBoolean(this, c.getBoolean(name));
                    break;
                case String:
                    c.setRetString(this, c.getString(name));
                    break;
                case Number:
                    c.setRetNumber(this, c.getNumber(name));
                    break;
                case Words:
                    c.setRetWords(this, c.getWords(name));
                    break;
                case Vertex:
                    c.setRetVertex(this, c.getVertex(name));
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