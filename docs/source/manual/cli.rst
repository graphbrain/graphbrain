======================
Command-line interface
======================

Graphbrain provides a command-line interface that can be used to execute a variety of tasks. You can access it either by using python to run the graphbrain root module as a script::

   python -m graphbrain ...

or using the provided command::

   graphbrain ...

All cases below work with both.

Here's an overview of the interface::

   usage: graphbrain [-h] [--agent AGENT] [--corefs] [--fields FIELDS] [--hg HG]
                     [--indir INDIR] [--infile INFILE] [--lang LANG]
                     [--outdir OUTDIR] [--outfile OUTFILE] [--parser PARSER]
                     [--pattern PATTERN] [--sequence SEQUENCE]
                     [--show_namespaces] [--system SYSTEM] [--text TEXT]
                     [--training_data TRAINING_DATA] [--url URL]
                     command

   positional arguments:
     command               command to execute

   optional arguments:
     -h, --help            show this help message and exit
     --agent AGENT         agent name
     --corefs              perform coreference resolution
     --fields FIELDS       field names
     --hg HG               hypergraph db
     --indir INDIR         input directory
     --infile INFILE       input file
     --lang LANG           language
     --outdir OUTDIR       output directory
     --outfile OUTFILE     output file
     --parser PARSER       parser
     --pattern PATTERN     edge pattern
     --sequence SEQUENCE   sequence name
     --show_namespaces     show namespaces
     --system SYSTEM       agent system file
     --text TEXT           text identifier
     --training_data TRAINING_DATA
                           training data for ML tasks
     --url URL             url

The only obligatory argument, command, is used to specify the task to perform. Each command uses a subset of the optional arguments. Presented below are the details for each command.

Commands
========

create
------

Creates an empty hypergraph database::

   graphbrain --hg <hypergraph_database> create


export
------

Exports a hypergraph database to a JSON file::

   graphbrain --hg <hypergraph_database> --outfile <json_file> export

import
------

Imports a hypergraph database from a JSON file::

   graphbrain --hg <hypergraph_database> --infile <json_file> import

run
---

Run a knowledge agent::

   graphbrain --hg <hypergraph_database> --agent <agent name> run

A knowledge agent is a program that manipulates an hypergraph in some way. It can be introspective, working only on the current contents of the hypergraph to derive new knowledge. For example, the *taxonomy* agent infers simple taxonomies from concepts. It can infer that 'black cat' is a type of 'cat' or that 'city of Berlin' is a type of 'city'. You can run it like this::

   graphbrain --hg <hypergraph_database> --agent taxonomy run

It produces new hyperedges such as::

   (type_of/P/. city/C (of/B city/C berlin/C))

Certain agents use outside sources to introduce knowledge into hypergraphs. For example, the *txt_parser* agent receives as input a simple text file and converters each sentence that it detects in it into an hyperedge. You can run it like this::

   graphbrain --infile some_test_file.txt --hg <hypergraph_database> --agent txt_parser run

You can find the full list of agents that are distributed with Graphbrain here:

https://graphbrain.net/reference/agents.html