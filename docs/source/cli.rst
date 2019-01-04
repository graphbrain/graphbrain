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

Reader
======

These commands are used to create datasets, perform learning and test the performance of the reader.
The reader is an AI module that consists of a pipline of stages that transform text in natural language into
hypergraphs.

reader_tests
------------

Tests the reader by applying it to a file containing example sentences::

   graphbrain --infile <example sentences file> [--lang <language>] [--model_file <file>] [--show_namespaces] reader_tests

If the optional ``--show_namespaces`` option is specified, the resulting hyperedges will contain symbols qualified
with their namespaces, otherwise no namespaces will be shown.

interactive_edge_builder
------------------------

This command opens an interactive session that allows the user to provide sentences and then manually perform the
appropriate transformations from the parse tree of these sentences into an initial hyperedge::

   graphbrain --outfile <sentence transformations file> [--lang <language>] interactive_edge_builder

For each sentence that is manually parsed, a case is generated and appended to the output file.

The command ``generate_hypergen_cases`` can then be used to generate a training dataset from the ouput of this command.

A sentence parses dataset with a number of cases is provided with GraphBrain, at
``datasets/training_data/hyperedge_generator/parses.txt``.

generate_hypergen_cases
-----------------------

Generate training datasets from sentence parse transformations file created with ``interactive_edge_builder``.  This
command breaks down each transformation into atomic cases, with respective feature values and expected output::

   graphbrain --infile <sentence transformations file> --outfile <training cases file> generate_hypergen_cases

The output of this command can then be used to train a machine learning model using the command ``learn_hypergen``.

Only 75% os the sentence parse transformations are used to generate the training cases. The remining 25% are reserved
as a test dataset, to be used by ``test_hypergen``.

learn_hypergen
--------------

Trains a machine learning model for the hypergen reader stage using a training cases file produced by
``generate_hypergen_cases``::

   graphbrain --infile <training cases file> [--output <model file>] [--model_type <model type>] learn_hypergen

The optional ``--model_type`` parameter can be used to specify the type of machine learning model to use. Currently
there are two options available: ``rf`` for random forest and ``nn`` for neural network. If not specified, random
forest is assumed. If ``--output`` is not specified, the default file name for the model type is used.

test_hypergen
-------------

Tests a machine learning model for the hypergen reader stage using 25% of the examples in a sentence parse
transformations file::

   graphbrain --infile <sentence transformations file> [--lang <language>] [--model_file <file>] [--model_type <model type>] test_hypergen

The optional ``--model_type`` parameter can be used to specify the type of machine learning model to use. Currently
there are two options available: ``rf`` for random forest and ``nn`` for neural network. If not specified, random
forest is assumed.
