package com.graphbrain.eco;

import com.graphbrain.eco.nodes.ProgNode;
import com.graphbrain.db.Vertex;

import java.util.*;

public class Context {

    private Contexts parent;

    Map<String, NodeType> varTypes;
    Map<String, String> stringVars;
    Map<String, Double> numberVars;
    Map<String, Boolean> booleanVars;
    Map<String, Words> wordsVars;
    Map<String, Vertex> vertexVars;

    Map<ProgNode, String> retStringMap;
    Map<ProgNode, Double> retNumberMap;
    Map<ProgNode, Boolean> retBooleanMap;
    Map<ProgNode, Words> retWordsMap;
    Map<ProgNode, Vertex> retVertexMap;

    ProgNode topRet;

    List<Context> subContexts;

    public Context(Contexts parent) {
        this.parent = parent;

        varTypes = new HashMap<>();
        stringVars = new HashMap<>();
        numberVars = new HashMap<>();
        booleanVars = new HashMap<>();
        wordsVars = new HashMap<>();
        vertexVars = new HashMap<>();

        retStringMap = new IdentityHashMap<>();
        retNumberMap = new IdentityHashMap<>();
        retBooleanMap = new IdentityHashMap<>();
        retWordsMap = new IdentityHashMap<>();
        retVertexMap = new IdentityHashMap<>();
        topRet = null;
        subContexts = new LinkedList<>();

        Context globals = parent.getGlobals();
        if (globals != null) {
            copyGlobalsFrom(globals);
        }
    }

    private Context() {}

    public Context copy() {
        Context c = new Context();

        c.parent = parent;

        c.varTypes = new HashMap<>(varTypes);
        c.stringVars = new HashMap<>(stringVars);
        c.numberVars = new HashMap<>(numberVars);
        c.booleanVars = new HashMap<>(booleanVars);
        c.wordsVars = new HashMap<>(wordsVars);
        c.vertexVars = new HashMap<>(vertexVars);

        c.retStringMap = new IdentityHashMap<>(retStringMap);
        c.retNumberMap = new IdentityHashMap<>(retNumberMap);
        c.retBooleanMap = new IdentityHashMap<>(retBooleanMap);
        c.retWordsMap = new IdentityHashMap<>(retWordsMap);
        c.retVertexMap = new IdentityHashMap<>(retVertexMap);
        c.topRet = topRet;
        c.subContexts = new LinkedList<>(subContexts);

        return c;
    }

    public void copyGlobalsFrom(Context globals) {
        for (String key : globals.stringVars.keySet()) {
            if (key.charAt(0) == '_') {
                setString(key, globals.stringVars.get(key));
            }
        }
        for (String key : globals.numberVars.keySet()) {
            if (key.charAt(0) == '_') {
                setNumber(key, globals.numberVars.get(key));
            }
        }
        for (String key : globals.booleanVars.keySet()) {
            if (key.charAt(0) == '_') {
                setBoolean(key, globals.booleanVars.get(key));
            }
        }
        for (String key : globals.wordsVars.keySet()) {
            if (key.charAt(0) == '_') {
                setWords(key, globals.wordsVars.get(key));
            }
        }
        for (String key : globals.vertexVars.keySet()) {
            if (key.charAt(0) == '_') {
                setVertex(key, globals.vertexVars.get(key));
            }
        }
    }

    public String getRetString(ProgNode p) {return retStringMap.get(p);}
    public double getRetNumber(ProgNode p) {return retNumberMap.get(p);}
    public boolean getRetBoolean(ProgNode p) {return retBooleanMap.get(p);}
    public Words getRetWords(ProgNode p) {return retWordsMap.get(p);}
    public Vertex getRetVertex(ProgNode p) {return retVertexMap.get(p);}

    public String getTopRetString() {return retStringMap.get(topRet);}
    public double getTopRetNumber() {return retNumberMap.get(topRet);}
    public boolean getTopRetBoolean() {return retBooleanMap.get(topRet);}
    public Words getTopRetWords() {return retWordsMap.get(topRet);}
    public Vertex getTopRetVertex() {return retVertexMap.get(topRet);}

    public void setRetString(ProgNode p, String value) {
        retStringMap.put(p, value);
        topRet = p;
    }
    public void setRetNumber(ProgNode p, double value) {
        retNumberMap.put(p, value);
        topRet = p;
    }
    public void setRetBoolean(ProgNode p, boolean value) {
        retBooleanMap.put(p, value);
        topRet = p;
    }
    public void setRetWords(ProgNode p, Words value) {
        retWordsMap.put(p, value);
        topRet = p;
    }
    public void setRetVertex(ProgNode p, Vertex value) {
        retVertexMap.put(p, value);
        topRet = p;
    }

    private void setType(String variable, NodeType value) {
        varTypes.put(variable, value);
    }

    public NodeType getType(String variable) {
        if (varTypes.containsKey(variable))
            return varTypes.get(variable);
        else
            return NodeType.Unknown;
    }

    public void setString(String variable, String value) {
        setType(variable, NodeType.String);
        stringVars.put(variable, value);
    }
    public void setNumber(String variable, double value) {
        setType(variable, NodeType.Number);
        numberVars.put(variable, value);
    }
    public void setBoolean(String variable, boolean value) {
        setType(variable, NodeType.Boolean);
        booleanVars.put(variable, value);
    }
    public void setWords(String variable, Words value) {
        setType(variable, NodeType.Words);
        wordsVars.put(variable, value);
    }
    public void setVertex(String variable, Vertex value) {
        setType(variable, NodeType.Vertex);
        vertexVars.put(variable, value);
    }

    public String getString(String variable) {return stringVars.get(variable);}
    public double getNumber(String variable) {return numberVars.get(variable);}
    public boolean getBoolean(String variable) {return booleanVars.get(variable);}
    public Words getWords(String variable) {return wordsVars.get(variable);}
    public Vertex getVertex(String variable) {return vertexVars.get(variable);}

    public void addSubContext(Context subCtxt) {
        subContexts.add(subCtxt);
    }

    public void printCallStack(int indent) {
        for (int i = 0; i <= indent; i++) {
            System.out.print("....");
        }

        System.out.println(parent.getRule().getName() + " [" + parent.getSentence() + "]");

        for (Context sctxts : subContexts)
            sctxts.printCallStack(indent + 1);
    }

    public void printCallStack() {
        printCallStack(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("context: ");
        for (String v : stringVars.keySet()) {
            sb.append(v);
            sb.append(" = ");
            sb.append(stringVars.get(v));
        }
        sb.append("; ");
        for (String v : numberVars.keySet()) {
            sb.append(v);
            sb.append(" = ");
            sb.append(numberVars.get(v));
        }
        sb.append("; ");
        for (String v : booleanVars.keySet()) {
            sb.append(v);
            sb.append(" = ");
            sb.append(booleanVars.get(v));
        }
        sb.append("; ");
        for (String v : wordsVars.keySet()) {
            sb.append(v);
            sb.append(" = ");
            sb.append(wordsVars.get(v));
        }
        sb.append("; ");
        for (String v : vertexVars.keySet()) {
            sb.append(v);
            sb.append(" = ");
            sb.append(vertexVars.get(v));
        }
        sb.append("; ");

        return sb.toString();
    }

    public Contexts getParent() {
        return parent;
    }

    public List<Context> getSubContexts() {
        return subContexts;
    }
}