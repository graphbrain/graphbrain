package com.graphbrain.eco.nodes.patterns;

import com.graphbrain.eco.Words;

import java.util.Vector;

public class VarPatternElem extends PatternElem {
    private String name;
    private String[] possiblePOS;
    private String[] necessaryPOS;
    private String[] forbiddenPOS;

    private Vector<Integer[]> intervals;
    private int curInterval;
    private int intEnd;

    public VarPatternElem(String name,
        String[] possiblePOS,
        String[] necessaryPOS,
        String[] forbiddenPOS) {

        super();

        this.name = name;
        this.possiblePOS = possiblePOS;
        this.necessaryPOS = necessaryPOS;
        this.forbiddenPOS = forbiddenPOS;

        intervals = new Vector<Integer[]>();
        curInterval = -1;
        intEnd = -1;
    }

    public VarPatternElem(String name) {
        this(name, new String[]{}, new String[]{}, new String[]{});
    }

    @Override
    protected int priority() {
        return possiblePOS.length + necessaryPOS.length + forbiddenPOS.length;
    }

    private boolean possibleWord(int pos) {
        if (possiblePOS.length == 0)
            return true;

        String wordPOS = sentence.getWords()[pos].getPos();
        boolean b = false;

        for (String p : possiblePOS) {
            if (wordPOS.startsWith(p)) {
                b = true;
                break;
            }
        }

        return b;
    }

    @Override
    protected void onPreProcess() {
        intervals = new Vector<Integer[]>();

        int intStart = startMin;

        while (intStart <= startMax) {
            if (possibleWord(intStart)) {
                int intEnd = intStart;
                while ((intEnd <= endMax) && possibleWord(intEnd))
                    intEnd += 1;

                Integer[] interval = {intStart, intEnd - 1};
                intervals.add(interval);

                intStart = intEnd + 1;
            }
            else {
                intStart += 1;
            }
        }
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

        int slen = sentence.length();
        int remaining = elemCount - elemPos - 1;

        if (remaining == 0)
            endMin = slen - 1;
        else
            endMin = startMin;

        endMax = slen - 1 - remaining;
    }

    private boolean curIntervalValid() {
        if ((necessaryPOS.length == 0) && (forbiddenPOS.length == 0))
            return true;

        if (necessaryPOS.length > 0) {
            boolean necessary = true;

            for (String np : necessaryPOS) {
                boolean test = false;
                for (int i = start; i <= end; i++) {
                    if (sentence.getWords()[i].getPos().startsWith(np)) {
                        test = true;
                        break;
                    }
                }

                if (!test) {
                    necessary = false;
                    break;
                }
            }

            if (!necessary)
                return false;
        }

        if (forbiddenPOS.length > 0) {
            for (String fp : forbiddenPOS) {
                for (int i = start; i <= end; i++) {
                    if (sentence.getWords()[i].getPos().startsWith(fp)) {
                        return false;
                    }
                }
            }
        }
        else {
            return true;
        }

        return true;
    }

    @Override
    public boolean onNext() {
        boolean found = false;

        while(!found) {
            if (!step())
                return false;

            if (curIntervalValid())
                found = true;
        }

        return true;
    }

    private boolean setCurInterval(int n) {
        if (n >= intervals.size())
            return false;

        curInterval = n;
        intEnd = Math.min(curEndMax(), intervals.get(curInterval)[1]);

        start = Math.max(curStartMin(), intervals.get(curInterval)[0]);
        if (start > curStartMax())
            return false;

        end = Math.max(curEndMin(), start);

        return end <= intEnd;
    }

    private boolean step() {

        if (start < 0) {
            int firstInterval = 0;

            // look for first interval that can fit
            while ((firstInterval < intervals.size())
                    && (intervals.get(firstInterval)[1] < curStartMin())) {

                firstInterval += 1;
            }

            return setCurInterval(firstInterval);
        }
        else {
            end += 1;
            if (end > intEnd) {
                start += 1;
                if ((start > intervals.get(curInterval)[1]) || (start > curStartMax())) {
                    if (!setCurInterval(curInterval + 1))
                        return false;
                }
                /*
                else {
                    Math.max(curEndMin(), start);
                }*/
            }
            return true;
        }
    }

    public Words curWords() {
        return sentence.slice(start, end);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);

        sb.append(name);

        if ((possiblePOS.length > 0) || (necessaryPOS.length > 0) || (forbiddenPOS.length > 0)) {
            sb.append(":");

            boolean first = true;
            for (String x : possiblePOS) {
                if (first) {
                    first = false;
                }
                else {
                    sb.append("|");
                }
                sb.append(x);
            }

            first = true;
            for (String x : necessaryPOS) {
                if (first) {
                    first = false;
                }
                else {
                    sb.append("|");
                }
                sb.append("+");
                sb.append(x);
            }

            first = true;
            for (String x : forbiddenPOS) {
                if (first) {
                    first = false;
                }
                else {
                    sb.append("|");
                }
                sb.append("-");
                sb.append(x);
            }
        }

        if (sentence != null) {
            sb.append(" = '");
            sb.append(sentence.slice(start, end));
            sb.append("'");
        }

        return sb.toString();
    }

    public String getName() {
        return name;
    }
}