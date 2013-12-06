package com.graphbrain.eco;

import com.graphbrain.db.Vertex;

import java.util.LinkedList;
import java.util.List;

public class Text {

    private List<String> sentences;

    public Text(String text) {
        String[] lines = text.split("\\.");

        sentences = new LinkedList<String>();
        for (String l : lines) {
            String sent = l.trim();
            if (!sent.isEmpty()) {
                sentences.add(sent);
            }
        }
    }

    public void parse(Prog prog) {
        for (String s : sentences) {
            Vertex vertex = prog.parse(s);
            System.out.println(s);
            System.out.println(vertex);
        }
    }

    public List<String> getSentences() {
        return sentences;
    }
}