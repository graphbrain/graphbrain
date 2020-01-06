================
Knowledge Agents
================

Parsers
=======

reddit_parser
-------------

**Type**: input

**Languages**: all

Takes a Reddit JSON corpus as input and converts each one of thread titles, and optionally thread comments to hyperedges, adding them to the hypergraph. Titles and comments are attributed to authors.

txt_parser
----------

**Type**: input

**Languages**: all

Takes a text file as input and converts each one of its sentences to hyperedges, adding them to the hypergraph.

This is a very simple but also useful, general-purpose agent.


Ontologies
==========

taxonomy
--------

**Type**: introspective

**Languages**: agnostic

Derives a taxonomy from concepts defined with builders of modifiers. For example, ``(of/br.ma founder/cc.s psychoanalysis/cc.s)`` is a type of ``founder/cc.s``, so the following hyperedge is added::

   (type_of/p/. (of/br.ma founder/cc.s psychoanalysis/cc.s) founder/cc.s)

Or, if we consider modifier-defined concepts such as ``(black/ma cat/cc.s)``::

   (type_of/p/. (black/ma cat/cc.s) cat/cc.s)


Coreference resolution
======================

corefs_dets
-----------

**Type**: introspective

**Languages**: English

Performs `coreference resolution <https://graphbrain.net/reference/special-relations.html#coreferences>`_ for specific cases where a determinant can be used with a concept or not (e.g.: "The Beatles" or just "Beatles").

corefs_names
------------

**Type**: introspective

**Languages**: agnostic

Performs `coreference resolution <https://graphbrain.net/reference/special-relations.html#coreferences>`_ for compound proper name concepts, for example detecting that "Barack Obama" and "Obama" refer to the same person but "Michelle Obama" refers to someone else).

corefs_onto
-----------

**Type**: introspective

**Languages**: agnostic

**Depends on**: taxonomy

Performs `coreference resolution <https://graphbrain.net/reference/special-relations.html#coreferences>`_ based on probabilistic reasoning over taxonomies. For example, detecting that "United States" and "United States of America" refer to the same entity.

corefs_unidecode
----------------

**Type**: introspective

**Languages**: agnostic

Performs `coreference resolution <https://graphbrain.net/reference/special-relations.html#coreferences>`_ for atoms that become equal after unidecode() is a applied to both labels. For example, it will create a coreference relation between ``über/c`` and ``uber/c``.


Actors, claims and conflicts
============================

actors
------

**Type**: introspective

**Languages**: agnostic

**Depends on**: coreference resolution

We define actors as specific entities that are capable of acting in some sense. This simple agent identifies hyperedges corresponding to actor by applying the following criteria:

1. The hyperedge or one of its coreferences appears at least two times as the subject of a declarative relation
2. The hyperedge is of type concept and subtype proper concept
3. If coreferences are used, the hyperedge is the main coreference

This agent transverses the entire hypergraph to identify actors, and then adds hyperedges like the following::

   (actor/p/. mary/cp.s/en)

The above simply means that ``mary/cp.s/en`` was identified as an actor.

claim_actors
------------

**Type**: introspective

**Languages**: agnostic

**Depends on**: actors, claims

Creates relations connecting claims to actors mentioned in the claim. For example, consider the sentence: "Mary says that John is nice." In this case, the claim "John is nice" (which is attributed to Mary), will be connected to John through a claim-actor relation. Claim-actor relations have the form::

   (claim-actor/p/. *main_actor* *actor* *claim* *main_edge*)

claims
------

**Type**: introspective

**Languages**: English

**Depends on**: coreference resolution

Identifies hyperedges that represent a claim. Claims are sentences such as: "North Korea says it's not afraid of US military strike". The claim is that "North Korea is not afraid of US military strike" and the author of the claim is "North Korea".

More specifically, claims are detected according to the following criteria:

1. Hyperedge is a relation with predicate of type ``pd``.
2. The deep predicate atom of the predicate hyperedge has a lemma belonging to a predetermined lists of verb lemmas that denote a claim (e.g.: "say", "claim").
3. The hyperedge has a subject and a clausal complement. The first is used to identify the actor making the claim, the second the claim itself.

Claim relations follow the format::

   (claim/p/. *actor* *claim* *edge*)

Furthermore, simple anaphora resolution on the claim is performed (e.g. in "Pink Panther says that she loves pink.", the hyperedge for "she" is replaced with the hyperedge for "Pink Panther" in the claim). In these cases, pronouns are used to guess gender or nature of actors. Actors can be classified as female::

   (female/p/. *actor*)

Or as a group::

   (group/p/. *actor*)

Or as male::

   (male/p/. *actor*)

Or as non-human::

   (non-human/p/. *actor*)

conflicts
---------

**Type**: introspective

**Languages**: English

**Depends on**: coreference resolution

Identifies hyperedges that represent a conflict. Conflicts are sentences such as: "Germany warns Russia against military engagement in Syria". The source of the expression of conflict here is "Germany", the target is "Russia" and the topic is "military engagement in Syria".

More specifically, claims are detected according to the following criteria:

1. Hyperedge is a relation with predicate of type ``pd``.
2. The deep predicate atom of the predicate hyperedge has a lemma belonging to a predetermined lists of verb lemmas that denote an expression of conflict (e.g.: "warn", "kill").
3. The hyperedge has a subject and an object. The first is used to identify the actor originating the expression of conflict and the second the actor which is the target of this expression.
4. [optional] Beyond subject and object, if any specifier arguments are present, and their trigger atoms belong to a predetermined list (e.g. "over", "against"), then topics of conflict are extracted from these specifiers.

Conflict relations follow the format::

   (conflict/p/. *actor_orig* *actor_targ* *edge*)

These conflict relations are connected to their topics by further relations with the format::

   (conflict-topic/p/. *actor_orig* *actor_targ* *concept* *edge*)
