================
Knowledge Agents
================

actors
======

**Type**: introspective

**Languages**: all

We define actors as specific entities that are capable of acting in some sense. This simple agent identifies hyperedges corresponding to actor by applying the following criteria:

1. The hyperedge or one of its coreferences appears at least two times as the subject of a declarative relation
2. The hyperedge is of type concept and subtype proper concept
3. If coreferences are used, the hyperedge is the main coreference

This agent transverses the entire hypergraph to identify actors, and then adds hyperedges like the following:

(actor/p/. mary/cp.s/en)

The above simply means that mary/cp.s/en was identified as an actor.

claim_actors
============

TODO

claims
======

TODO

conflicts
=========

TODO

corefs_dets
===========

**Type**: introspective

**Languages**: English

Performs coreference resolution for specific cases where a determinant can be used with a concept or not (e.g.: "The Beatles" or just "Beatles").

corefs_names
============

TODO

corefs_onto
===========

TODO

corefs_unidecode
================

TODO

reddit_parser
=============

TODO

taxonomy
========

**Type**: introspective

**Languages**: all

Derives a taxonomy from concepts defined with builders of modifiers. For example, (of/br.ma founder/cc.s psychoanalysis/cc.s) is a type of founder/cc.s, so the following hyperedge is added:

(type_of/p/. (of/br.ma founder/cc.s psychoanalysis/cc.s) founder/cc.s)

Or, if we consider modifier-defined concepts such as (black/ma cat/cc.s):

(type_of/p/. (black/ma cat/cc.s) cat/cc.s)

txt_parser
==========

**Type**: input

**Languages**: all

Takes a text file as input and converts each one of its sentences to hyperedges, adding them to the hypergraph.

This is a very simple but also useful, general-purpose agent.
