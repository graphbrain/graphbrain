package com.graphbrain.eco.nodes;

import com.graphbrain.db.Edge;
import com.graphbrain.db.EntityNode;
import com.graphbrain.db.ID;
import com.graphbrain.db.Vertex;
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

    public VertexFun(VertexFunType fun, ProgNode[] params) {
        this(fun, params, -1);
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
    public NodeType ntype() {return NodeType.Vertex;}

    @Override
    public void vertexValue(Contexts ctxts) {
        switch(fun) {
            case BuildVert:
                for (ProgNode p : params) {
                    switch(p.ntype()) {
                        case Vertex:
                            p.vertexValue(ctxts);
                            break;
                        case String:
                            p.stringValue(ctxts);
                            break;
                        default: // error!
                    }
                }

                for (Context c : ctxts.getCtxts()) {
                    Vertex[] verts = new Vertex[params.length];
                    for (int i = 0; i < params.length; i++) {
                        ProgNode p = params[i];
                        switch(p.ntype()) {
                            case Vertex:
                                verts[i] = c.getRetVertex(p);
                                break;
                            case String:
                                verts[i] = Vertex.fromId(c.getRetString(p));
                                break;
                            default: // error
                        }
                    }
                    c.setRetVertex(this, new Edge(verts));
                }
                break;
            case RelVert:
                ProgNode p = params[0];

                switch(p.ntype()) {
                    case Words:
                        p.wordsValue(ctxts);
                        break;
                    case String:
                        p.stringValue(ctxts);
                        break;
                    default: //error
                }

                for (Context c : ctxts.getCtxts()) {
                    switch(p.ntype()) {
                        case Words:
                            c.setRetVertex(this, Vertex.fromId(ID.reltype_id(c.getRetWords(p).text())));
                            break;
                        case String:
                            c.setRetVertex(this, Vertex.fromId(ID.reltype_id(c.getRetString(p))));
                            break;
                        default: // error!
                    }
                }
                break;
            case TxtVert: {
                p = params[0];

                switch(p.ntype()) {
                    case Words:
                        p.wordsValue(ctxts);
                        break;
                    case String:
                        p.stringValue(ctxts);
                        break;
                    default: // error!
                }

                for (Context c : ctxts.getCtxts()) {
                    switch(p.ntype()) {
                        case Words:
                            c.setRetVertex(this, EntityNode.fromNsAndText("1", c.getRetWords(p).text()));
                            break;
                        case String:
                            c.setRetVertex(this, EntityNode.fromNsAndText("1", c.getRetString(p)));
                            break;
                        default: // error!
                    }
                }
                break;
            }
            case Flatten: {
                p = params[0];
                p.vertexValue(ctxts);

                for (Context c : ctxts.getCtxts()) {
                    Vertex v = c.getRetVertex(p);
                    c.setRetVertex(this, v.flatten());
                }
                break;
            }
        }
    }
}