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
        varTypes = new HashMap<String, NodeType>();
        stringVars = new HashMap<String, String>();
        numberVars = new HashMap<String, Double>();
        booleanVars = new HashMap<String, Boolean>();
        wordsVars = new HashMap<String, Words>();
        vertexVars = new HashMap<String, Vertex>();
        retStringMap = new IdentityHashMap<ProgNode, String>();
        retNumberMap = new IdentityHashMap<ProgNode, Double>();
        retBooleanMap = new IdentityHashMap<ProgNode, Boolean>();
        retWordsMap = new IdentityHashMap<ProgNode, Words>();
        retVertexMap = new IdentityHashMap<ProgNode, Vertex>();
        topRet = null;
        subContexts = new LinkedList<Context>();
    }

    private Context() {}

    public Context copy() {
        Context c = new Context();

        c.parent = parent;
        c.varTypes = new HashMap<String, NodeType>(varTypes);
        c.stringVars = new HashMap<String, String>(stringVars);
        c.numberVars = new HashMap<String, Double>(numberVars);
        c.booleanVars = new HashMap<String, Boolean>(booleanVars);
        c.wordsVars = new HashMap<String, Words>(wordsVars);
        c.vertexVars = new HashMap<String, Vertex>(vertexVars);
        c.retStringMap = new IdentityHashMap<ProgNode, String>(retStringMap);
        c.retNumberMap = new IdentityHashMap<ProgNode, Double>(retNumberMap);
        c.retBooleanMap = new IdentityHashMap<ProgNode, Boolean>(retBooleanMap);
        c.retWordsMap = new IdentityHashMap<ProgNode, Words>(retWordsMap);
        c.retVertexMap = new IdentityHashMap<ProgNode, Vertex>(retVertexMap);
        c.topRet = topRet;
        c.subContexts = new LinkedList<Context>(subContexts);

        return c;
    }

    /*
    def merge(ctxt: Context) = {
        ctxt.varTypes.foreach(kv => varTypes(kv._1) = kv._2)

        ctxt.stringVars.foreach(kv => stringVars(kv._1) = kv._2)
        ctxt.numberVars.foreach(kv => numberVars(kv._1) = kv._2)
        ctxt.booleanVars.foreach(kv => booleanVars(kv._1) = kv._2)
        ctxt.wordsVars.foreach(kv => wordsVars(kv._1) = kv._2)
        ctxt.vertexVars.foreach(kv => vertexVars(kv._1) = kv._2)

        ctxt.retStringMap.foreach(kv => retStringMap(kv._1) = kv._2)
        ctxt.retNumberMap.foreach(kv => retNumberMap(kv._1) = kv._2)
        ctxt.retBooleanMap.foreach(kv => retBooleanMap(kv._1) = kv._2)
        ctxt.retWordsMap.foreach(kv => retWordsMap(kv._1) = kv._2)
        ctxt.retVertexMap.foreach(kv => retVertexMap(kv._1) = kv._2)
        ctxt.retVerticesMap.foreach(kv => retVerticesMap(kv._1) = kv._2)

        topRet = ctxt.topRet

        subContexts = ctxt.subContexts
    }

    def mergeGlobals(ctxt: Context) = {
        ctxt.varTypes.foreach(kv => varTypes(kv._1) = kv._2)

        ctxt.stringVars.filter(v => v._1(0) == '_').foreach(kv => stringVars(kv._1) = kv._2)
        ctxt.numberVars.filter(v => v._1(0) == '_').foreach(kv => numberVars(kv._1) = kv._2)
        ctxt.booleanVars.filter(v => v._1(0) == '_').foreach(kv => booleanVars(kv._1) = kv._2)
        ctxt.wordsVars.filter(v => v._1(0) == '_').foreach(kv => wordsVars(kv._1) = kv._2)
        ctxt.vertexVars.filter(v => v._1(0) == '_').foreach(kv => vertexVars(kv._1) = kv._2)
    }
    */

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
}