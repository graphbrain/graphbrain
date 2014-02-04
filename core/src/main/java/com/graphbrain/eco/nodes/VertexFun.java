package com.graphbrain.eco.nodes;

import com.graphbrain.db.*;
import com.graphbrain.eco.Context;
import com.graphbrain.eco.Contexts;
import com.graphbrain.eco.NodeType;

public class VertexFun extends FunNode {

    public enum VertexFunType {
        BuildVert, RelVert, TxtVert, Flatten
    }

    private VertexFunType fun;

    public VertexFun(VertexFunType fun, ProgNode[] params, int lastTokenPos) {
        super(params, lastTokenPos);
        this.fun = fun;
    }

    @Override
    public String label() {
        switch(fun) {
            case BuildVert: return "build";
            case RelVert: return "rel-vert";
            case TxtVert: return "txt-vert";
            case Flatten: return "flatten";
            default: return "?";
        }
    }

    @Override
    public NodeType ntype(Context ctxt) {return NodeType.Vertex;}

    @Override
    public void eval(Contexts ctxts) {
        switch(fun) {
            case BuildVert:
                for (ProgNode p : params) {
                    p.eval(ctxts);
                }

                for (Context c : ctxts.getCtxts()) {
                    Vertex[] verts = new Vertex[params.length];
                    for (int i = 0; i < params.length; i++) {
                        ProgNode p = params[i];
                        switch(p.ntype(c)) {
                            case Vertex:
                                verts[i] = c.getRetVertex(p);
                                break;
                            case String:
                                verts[i] = Vertex.fromId(c.getRetString(p));
                                break;
                            default:
                                // error
                                break;
                        }
                    }
                    c.setRetVertex(this, new Edge(verts));
                }
                break;
            case RelVert:
                ProgNode p = params[0];

                p.eval(ctxts);

                for (Context c : ctxts.getCtxts()) {
                    switch(p.ntype(c)) {
                        case Words:
                            c.setRetVertex(this, Vertex.fromId(EdgeType.buildId(c.getRetWords(p).text())));
                            break;
                        case String:
                            c.setRetVertex(this, Vertex.fromId(EdgeType.buildId(c.getRetString(p))));
                            break;
                        default: // error!
                    }
                }
                break;
            case TxtVert: {
                p = params[0];

                p.eval(ctxts);

                for (Context c : ctxts.getCtxts()) {
                    switch(p.ntype(c)) {
                        case Words:
                            c.setRetVertex(this, EntityNode.fromNsAndText("", c.getRetWords(p).text()));
                            break;
                        case String:
                            c.setRetVertex(this, EntityNode.fromNsAndText("", c.getRetString(p)));
                            break;
                        default: // error!
                    }
                }
                break;
            }
            case Flatten: {
                p = params[0];
                p.eval(ctxts);

                for (Context c : ctxts.getCtxts()) {
                    Vertex v = c.getRetVertex(p);
                    c.setRetVertex(this, v.flatten());
                }
                break;
            }
        }
    }
}