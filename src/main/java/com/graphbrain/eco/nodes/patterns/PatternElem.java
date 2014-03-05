package com.graphbrain.eco.nodes.patterns;

import com.graphbrain.eco.Words;

public abstract class PatternElem implements Comparable<PatternElem> {
    protected int elemPos;
    protected int elemCount;

    protected boolean fixed;
    protected boolean preProcessed;
    protected Words sentence;

    protected PatternElem prevElem;
    protected PatternElem nextElem;

    protected int start;
    protected int end;

    protected int startMin;
    protected int startMax;
    protected int endMin;
    protected int endMax;

    public PatternElem() {
        elemPos = -1;
        elemCount = -1;

        fixed = false;
        preProcessed = false;
        sentence = null;

        prevElem = null;
        nextElem = null;

        start = -1;
        end = -1;

        startMin = -1;
        startMax = -1;
        endMin = -1;
        endMax = -1;
    }

    public void init(int elemPos, int elemCount, PatternElem prev, PatternElem next) {
        this.elemPos = elemPos;
        this.elemCount = elemCount;
        this.prevElem = prev;
        this.nextElem = next;
    }

    protected abstract int priority();

    protected abstract void onSetSentence();

    public void setSentence(Words sentence) {
        this.sentence = sentence;
        preProcessed = false;
        onSetSentence();
    }

    public void rewind() {
        start = -1;
    }

    public void preProcess() {
        preProcessed = true;
        onPreProcess();
    }

    protected void onPreProcess() {}

    public boolean next() {
        if (!preProcessed)
            preProcess();

        return onNext();
    }

    public abstract boolean onNext();

    public int curStartMin() {
        if ((prevElem != null) && prevElem.fixed)
            return prevElem.end + 1;
        else
            return startMin;
    }

    public int curStartMax() {
        if ((prevElem != null) && prevElem.fixed)
            return prevElem.end + 1;
        else
            return startMax;
    }

    public int curEndMin() {
        if ((nextElem != null) && nextElem.fixed)
            return nextElem.start - 1;
        else
            return endMin;
    }

    public int curEndMax() {
        if ((nextElem != null) && nextElem.fixed)
            return nextElem.start - 1;
        else
            return endMax;
    }

    public int compareTo(PatternElem e) {
        return e.priority() - priority();
    }

    public PatternElem getNextElem() {
        return nextElem;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public String detailStr() {
        String str = "";
        str += "elemPos: " + elemPos;
        str += "; elemCount: " + elemCount;
        str += "; start: " + start;
        str += "; end: " + end;
        str += "; startMin: " + startMin;
        str += "; startMax: " + startMax;
        str += "; endMin: " + endMin;
        str += "; endMax: " + endMax;
        str += "; fixed: " + fixed;

        return str;
    }
}