======================
Command-line interface
======================

Graphbrain provides a command-line interface that can be used to execute a variety of tasks. You can access it either by using python to run the graphbrain root module as a script::

   python -m graphbrain ...

or using the provided command::

   graphbrain ...

All cases below work with both.

Here's an overview of the interface::

   graphbrain [-h] [--hg HG] [--infile INFILE] [--outfile OUTFILE]
              [--fields FIELDS] [--show_namespaces] [--lang LANG]
              [--pattern PATTERN] [--agent AGENT]
              command

   positional arguments:
     command               command to execute

   optional arguments:
     -h, --help            show this help message and exit
     --hg HG               hypergraph db file path
     --infile INFILE       input file
     --outfile OUTFILE     output file
     --fields FIELDS       field names
     --show_namespaces     show namespaces
     --lang LANG           language
     --pattern PATTERN     hyperedge pattern
     --agent AGENT         agent name

The only obligatory argument, command, is used to specify the task to perform. Each command uses a subset of the optional arguments. Presented below are the details for each command.

Commands
========

create
------

Creates an empty hypergraph::

   graphbrain --hg <hypergraph> create


info
----

Displays simple information about an hypergraph::

   graphbrain --hg <hypergraph> info


run
---

Run a knowledge agent::

   graphbrain --hg <hypergraph> --agent <agent name> run

A knowledge agent is a program that manipulates an hypergraph in some way. It can be introspective, working only on the current contents of the hypergraph to derive new knowledge. For example, the *taxonomy* agent infers simple taxonomies from concepts. It can infer that 'black cat' is a type of 'cat' or that 'city of Berlin' is a type of 'city'. You can run it like this::

   graphbrain --hg <hypergraph> --agent taxonomy run

It produces new hyperedges such as::

   (type_of/p/. city/c (of/b city/c berlin/c))

Certain agents use outside sources to introduce knowledge into hypergraphs. For example, the *txt_parser* agent receives as input a simple text file and converters each sentence that it detects in it into an hyperedge. You can run it like this::

   graphbrain --infile some_test_file.txt --hg <hypergraph> --agent txt_parser run

You can find the full list of agents that are distributed with Graphbrain here:

https://graphbrain.net/reference/agents.html