package com.graphbrain.eco;

import com.graphbrain.db.Vertex;
import java.util.*;

public class VariableContainer {

    protected Map<String, NodeType> varTypes;
    protected Map<String, String> stringVars;
    protected Map<String, Double> numberVars;
    protected Map<String, Boolean> booleanVars;
    protected Map<String, Words> wordsVars;
    protected Map<String, Vertex> vertexVars;

    public VariableContainer() {
        varTypes = new HashMap<>();
        stringVars = new HashMap<>();
        numberVars = new HashMap<>();
        booleanVars = new HashMap<>();
        wordsVars = new HashMap<>();
        vertexVars = new HashMap<>();
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
}