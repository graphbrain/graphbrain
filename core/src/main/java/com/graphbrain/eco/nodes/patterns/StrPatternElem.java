package com.graphbrain.eco.nodes.patterns;

public class StrPatternElem extends PatternElem {

    private String str;

    public StrPatternElem(String str) {
        super();
        this.str = str;
    }

    @Override
    protected int priority() {
        return 1000 * str.length();
    }

    @Override
    protected void onSetSentence() {
        if (prevElem == null) {
            startMin = 0;
            startMax = 0;
        }
        else {
            startMin = prevElem.endMin + 1;
            startMax = prevElem.endMax + 1;
        }

        endMin = startMin;
        endMax = startMax;
    }

    private boolean step() {
        if (start < 0) {
            start = curStartMin();
            return true;
        }
        else {
            start += 1;
            return (start <= curStartMax()) && (start <= curEndMax());
        }
    }

    @Override
    public boolean onNext() {
        // check if gap to fill is larger than one word
        if (curStartMax() < curEndMin())
            return false;

        boolean found = false;

        while(!found) {
            if (!step())
                return false;

            if (sentence.getWords()[start].getWord().toLowerCase().equals(str.toLowerCase())) {
                found = true;
            }
        }

        end = start;
        return true;
    }

    @Override
    public String toString() {
        if (sentence == null)
            return "\"" + str + "\"";
        else
            return "\"" + str + "\"" + " = '" + sentence.slice(start, end) + "'";
    }
}