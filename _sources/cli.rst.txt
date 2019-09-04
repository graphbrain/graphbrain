======================
Command-line interface
======================

GraphBrain provides a command-line interface that can be used to execute a variety of tasks. You can access it either by using python to run the graphbrain root module as a script::

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

Run an agent::

   graphbrain --hg <hypergraph> --agent <agent name> run
