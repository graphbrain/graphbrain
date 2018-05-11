======================
Command-line interface
======================

GraphBrain provides a command-line interface that can be used to execute a variety of tasks.

Here's an overview of the interface::

   gbrain [-h] [--backend BACKEND] [--hg HG] [--infile INFILE]
               [--outfile OUTFILE] [--startdate STARTDATE] [--enddate ENDDATE]
               [--source SOURCE] [--log LOG] [--comments] [--fields FIELDS]
               [--model_type MODEL_TYPE] [--model_file MODEL_FILE]
               [--show_namespaces] [--lang LANG]
               command

   positional arguments:
     command                  command to execute

   optional arguments:
     -h, --help               show help message
     --backend BACKEND        hypergraph backend (leveldb, null)
     --hg HG                  hypergraph name
     --infile INFILE          input file
     --outfile OUTFILE        output file
     --startdate STARTDATE    start date
     --enddate ENDDATE        end date
     --source SOURCE          source can have multiple meanings
     --log LOG                logging level
     --comments               include comments
     --fields FIELDS          field names
     --model_type MODEL_TYPE  machine learning model type
     --model_file MODEL_FILE  machine learning model file
     --show_namespaces        show namespaces
     --lang LANG              language

The only obligatory argument, command, is used to specify the task to perform. Each command uses a subset of the
optional arguments. Presented below are the details for each command.

Hypergraphs
===========

create
------

TBD

info
----

TBD

Knowledge extraction
====================

These are commands that extract knowledge from various source into hypergraphs.

reddit_reader
-------------

Applies a GraphBrain reader to the text of posts and comments extracted from Reddit::

   gbrain --hg <target hypergraph> --infile <reddit json file> [--comments] reddit_reader

The input file is a json file produced by the ``reddit_retriever`` command.
Comments are only processed if the optional ``--comments`` argument is used.

wordnet
-------

TBD

wikidata
--------

TBD

dbpedia
-------

TBD

dbpedia_wordnet
---------------

TBD

Knowledge inference
===================

These commands correspond to various knowledge inference strategies that can be performed on hypergraphs.

generate_synonyms
-----------------

Finds synonyms in the hypergraph and connects them to synonym set identifiers::

   gbrain --hg <hypergraph> generate_synonyms


Data retrieval
==============

These are comands that retrieve data from external sources. This data can then be imported into hypergraphs through the
use of appropriate knowledge extraction commands.

reddit_retriever
----------------

Extracts posts and comments from Reddit, including metadata such as authors and timestamps::

   gbrain --source <subreddit> --outfile <reddit json file> --startdate <date> --enddate <date> reddit_retriever

``--source`` is used to specify the subreddit from where to retrieve posts and comments.
The output is a json file, that can then be used by the ``reddit_reader`` command.
``--startdate`` and ``--enddate`` are used to specify the time interval for data retrieval, in the format *yyyymmdd*.

For example, to retrieve data from http://reddit.com/r/worldnews, between 1-Jan-2017 and 15-Feb-2017::

   gbrain --source worldnews --outfile worldnews.json --startdate 20170101 --enddate 20170215 reddit_retriever

Interfaces
==========

ui
--

TBD


shell
-----

TBD

Reader
======

These commands are used to create datasets, perform learning and test the performance of the reader.
The reader is an AI module that consists of a pipline of stages that transform text in natural language into
hypergraphs.

reader_tests
------------

Tests the reader by applying it to a file containing example sentences::

   gbrain --infile <example sentences file> [--lang <language>] [--model_file <file>] [--show_namespaces] reader_tests

If the optional ``--show_namespaces`` option is specified, the resulting hyperedges will contain symbols qualified
with their namespaces, otherwise no namespaces will be shown.

interactive_edge_builder
------------------------

Extracts posts and comments from Reddit, including metadata such as authors and timestamps::

   gbrain --outfile <sentence transformations file> [--lang <language>] interactive_edge_builder

This command opens an interactive session that allows the user to provide sentences and then manually perform the
appropriate transformations from the parse tree of these sentences into an initial hyperedge. For each sentence that
is manually parsed, a case is generated and appended to the output file.

The command ``generate_hypergen_cases`` can then be used to generate a training dataset from the ouput of this command.

A sentence parses dataset with a number of cases is provided with GraphBrain, at
``datasets/training_data/hyperedge_generator/parses.txt``.

generate_hypergen_cases
-----------------------

Generate training datasets from sentence parse transformations file created with ``interactive_edge_builder``.  This
command breaks down each transformation into atomic cases, with respective feature values and expected output::

   gbrain --infile <sentence transformations file> --outfile <training cases file> generate_hypergen_cases

The output of this command can then be used to train a machine learning model using the command ``learn_hypergen``.

Only 75% os the sentence parse transformations are used to generate the training cases. The remining 25% are reserved
as a test dataset, to be used by ``test_hypergen``.

learn_hypergen
--------------

Trains a machine learning model for the hypergen reader stage using a training cases file produced by
``generate_hypergen_cases``::

   gbrain --infile <training cases file> [--output <model_file>] [--model_type <model type>] learn_hypergen

The optional ``--model_type`` parameter can be used to specify the type of machine learning model to use. Currently
there are two options available: ``rf`` for random forest and ``nn`` for neural network. If not specified, random
forest is assumed. If ``--output`` is not specified, the default file name for the model type is used.

test_hypergen
-------------

Tests a machine learning model for the hypergen reader stage using 25% of the examples in a sentence parse
transformations file::

   gbrain --infile <sentence transformations file> [--lang <language>] [--model_file <file>] [--model_type <model type>] test_hypergen

The optional ``--model_type`` parameter can be used to specify the type of machine learning model to use. Currently
there are two options available: ``rf`` for random forest and ``nn`` for neural network. If not specified, random
forest is assumed.


Tools
=====

all2json
--------

TBD