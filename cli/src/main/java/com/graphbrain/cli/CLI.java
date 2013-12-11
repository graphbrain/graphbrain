package com.graphbrain.cli;

import com.graphbrain.db.Graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CLI {

    public static Graph graph = new Graph();

    private Map<String, Command> commands;

    public CLI() {
        commands = new HashMap<String, Command>();
        commands.put("show", new Show());
        commands.put("edges", new Edges());
        commands.put("create", new Create());
    }

    private List<String> tokenize(String str) {
        List<String> tokens = new LinkedList<String>();

        int pos = 0;
        String curStr = "";
        int parens = 0;

        while (pos < str.length()) {
            char c = str.charAt(pos);

            if (c == ' ') {
                if (parens == 0) {
                    if (!curStr.isEmpty()) {
                        tokens.add(curStr);
                        curStr = "";
                    }
                }
                else {
                    curStr += c;
                }
            }
            else {
                if (c == '(') {
                    parens++;
                }
                else if (c == ')') {
                    parens--;
                }

                curStr += c;
            }
            pos++;
        }
        if (!curStr.isEmpty()) {
            tokens.add(curStr);
        }

        return tokens;
    }

    private void parseCommand(String cmd) {
        List<String> tokens = tokenize(cmd);

        String command = "";
        String[] params = new String[tokens.size() - 1];

        int pos = 0;
        for (String t : tokens) {
            if (pos == 0) {
                command = t;
            }
            else {
                params[pos - 1] = t;
            }
            pos++;
        }

        if (command.equals("exit") || command.equals("quit")) {
            System.exit(0);
        }

        if (commands.containsKey(command)) {
            commands.get(command).run(params);
        }
        else {
            System.out.println("unknown command: " + command);
        }
    }

    public void run() {
        System.out.println("Welcome to the GraphBrain command line interface.");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");

            String cmd = "";

            try {
                cmd = br.readLine();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
                System.exit(1);
            }

            parseCommand(cmd);
        }
    }

    public static void main(String[] args) {
        CLI cli = new CLI();
        cli.run();
    }
}