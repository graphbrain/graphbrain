======================
Command-line interface
======================

GraphBrain provides a command-line interface that can be used to execute a variety of tasks.

Here's an overview of the interface::

   gbrain [-h] [--backend BACKEND] [--hg HG] [--infile INFILE]
               [--outfile OUTFILE] [--startdate STARTDATE] [--enddate ENDDATE]
               [--source SOURCE] [--log LOG] [--comments] [--fields FIELDS]
               [--model_type MODEL_TYPE] [--show_namespaces]
               command

   positional arguments:
     command               command to execute

   optional arguments:
     -h, --help            show help message
     --backend BACKEND     hypergraph backend (leveldb, null)
     --hg HG               hypergraph name
     --infile INFILE       input file
     --outfile OUTFILE     output file
     --startdate STARTDATE
                           start date
     --enddate ENDDATE     end date
     --source SOURCE       source can have multiple meanings.
     --log LOG             logging level.
     --comments            include comments
     --fields FIELDS       field names
     --model_type MODEL_TYPE
                           machine learning model type
     --show_namespaces     show namespaces

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

reddit_reader
-------------

TBD

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

Data retrieval
==============

reddit_retriever
----------------

TBD

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

reader_tests
------------

TBD

interactive_edge_builder
------------------------

TBD


generate_hypergen_cases
-----------------------

TBD

learn_hypergen
--------------

TBD

test_hypergen
-------------

TBD

extract_hypergen_test_sentences
-------------------------------

TBD

Tools
=====

extract_json_fields
-------------------

TBD

all2json
--------

TBD