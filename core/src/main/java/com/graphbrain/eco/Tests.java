package com.graphbrain.eco;

import java.util.LinkedList;
import java.util.List;

public class Tests {

    List<String[]> tests;

    public Tests(String testData) {

        String[] lines = testData.split("\\r?\\n");
                //.map(_.trim).filter(_ != "")

        tests = new LinkedList<String[]>();

        for (String l : lines) {
            String line = l.trim();

            if (!line.isEmpty()) {
                String[] test = line.split("\\|");
                if (test.length == 2) {
                    test[0] = test[0].trim();
                    test[1] = test[1].trim();
                    tests.add(test);
                }
            }
        }
    }

    public List<String[]> getTests() {
        return tests;
    }
}
