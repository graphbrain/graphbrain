======================
Command-line interface
======================

GraphBrain provides a command-line interface that can be used to execute a variety of tasks. You can accesst it either by using python to run the graphbrain root module as a script::

   python -m graphbrain ...

or using the provided command::

   graphbrain ...

All cases below work with both.

Here's an overview of the interface::

   graphbrain [-h] [--backend BACKEND] [--hg HG] [--infile INFILE]
              [--outfile OUTFILE] [--log LOG] [--fields FIELDS]
              [--model_type MODEL_TYPE] [--model_file MODEL_FILE]
              [--show_namespaces] [--lang LANG]
              command

   positional arguments:
     command               command to execute

   optional arguments:
     -h, --help            show this help message and exit
     --backend BACKEND     hypergraph backend (leveldb, null)
     --hg HG               hypergraph name
     --infile INFILE       input file
     --outfile OUTFILE     output file
     --log LOG             logging level.
     --fields FIELDS       field names
     --model_type MODEL_TYPE
                           machine learning model type
     --model_file MODEL_FILE
                           machine learning model file
     --show_namespaces     show namespaces
     --lang LANG           language

The only obligatory argument, command, is used to specify the task to perform. Each command uses a subset of the
optional arguments. Presented below are the details for each command.

Hypergraphs
===========

create
------

Creates an empty hypergraph::

   graphbrain --hg <hypergraph> create


info
----

Displays simple information about an hypergraph::

   graphbrain --hg <hypergraph> info


hg2json
-------

Export hypergraph to a json file::

   graphbrain --hg <hypergraph> --outfile <target json file> hg2json


Knowledge inference
===================

These commands correspond to various knowledge inference strategies that can be performed on hypergraphs.

generate_synonyms
-----------------

Finds synonyms in the hypergraph and connects them to synonym set identifiers::

   graphbrain --hg <hypergraph> generate_synonyms
