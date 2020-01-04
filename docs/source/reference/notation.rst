============================
Semantic Hypergraph Notation
============================

Atom structure
==============

.. image:: /_static/atom-structure.png
    :align: center
    :alt: atom structure


Hyperedge types
===============

+------+---------------+------------------------------+--------------------------------+
| Code | Type          | Purpose                      | Example                        |
+======+===============+==============================+================================+
+ **Atomic or non-atomic**                                                             +
+------+---------------+------------------------------+--------------------------------+
| c    | concept       | Define atomic concepts       | **apple/c**                    |
+------+---------------+------------------------------+--------------------------------+
| p    | predicate     | Build relations              | (**is/p** berlin/c nice/c)     |
+------+---------------+------------------------------+--------------------------------+
| m    | modifier      | Modify a concept             | (**red/m** shoes/c)            |
+------+---------------+------------------------------+--------------------------------+
+ **Atomic only**                                                                      +
+------+---------------+------------------------------+--------------------------------+
| a    | auxiliary     | Modify a predicate           | (**not/a** is/p)               |
+------+---------------+------------------------------+--------------------------------+
| b    | builder       | Build concepts from concepts | (**of/b** capital/c germany/c) |
+------+---------------+------------------------------+--------------------------------+
| w    | meta-modifier | Modify a modifier            | (**very/w** large/m)           |
+------+---------------+------------------------------+--------------------------------+
| x    | subpredicate  | Auxiliary predicate          | (**by/x** john/c)              |
+------+---------------+------------------------------+--------------------------------+
| t    | trigger       | Build specifications         | (**in/t** 1994/c)              |
+------+---------------+------------------------------+--------------------------------+
+ **Non-atomic only**                                                                  +
+------+---------------+------------------------------+--------------------------------+
| r    | relation      | Express facts, statements,   | **(is/p berlin/c nice/c)**     |
+      +               +                              +                                +
|      |               | questions, orders, ...       |                                |
+------+---------------+------------------------------+--------------------------------+
| d    | dependent     | Relation argument            | **(by/x scientists/c)**        |
+------+---------------+------------------------------+--------------------------------+
| s    | specifier     | Relation specification       | **(in/t 1976/c)**              |
+      +               +                              +                                +
|      |               | (e.g. condition, time, ...)  |                                |
+------+---------------+------------------------------+--------------------------------+


Type inference rules
====================

+---------------+----------------+
| Element types | Resulting type |
+===============+================+
| (m  c)        | c              |
+---------------+----------------+
| (b  c  c+)    | c              |
+---------------+----------------+
| (w  m)        | m              |
+---------------+----------------+
| (a  p)        | p              |
+---------------+----------------+
| (p  [crds]+)  | r              |
+---------------+----------------+
| (x  [cr]+)    | d              |
+---------------+----------------+
| (t  [cr]+)    | s              |
+---------------+----------------+

We use the notation of regular expressions: the symbol ``+`` is used to denote an arbitrary number of entities with the type that precedes it, while square brackets indicate several possibilities (for instance, ``[cr]+`` means "at least one of any of both ``c`` or ``r`` types).


Subtypes
========

Concept
-------

+------+---------------+----------+
| Code | Subtype       | Example  |
+======+===============+==========+
| cc   | common        | apple/cc |
+------+---------------+----------+
| cp   | proper        | mary/cp  |
+------+---------------+----------+
| cn   | number        | 27/cn    |
+------+---------------+----------+
| ci   | pronoun       | she/ci   |
+------+---------------+----------+
| cw   | interrogative | who/cw   |
+------+---------------+----------+

Predicate
---------

+------+---------------+---------+
| Code | Subtype       | Example |
+======+===============+=========+
| pd   | declarative   | is/pd   |
+------+---------------+---------+
| p?   | interrogative | is/p?   |
+------+---------------+---------+
| p!   | imperative    | go/p!   |
+------+---------------+---------+
| pc   | conceptual    | go/pc   |
+------+---------------+---------+
| pm   | meta          | and/pm  |
+------+---------------+---------+

Builder
-------

+------+-------------+---------+
| Code | Subtype     | Example |
+======+=============+=========+
| bp   | possessive  | 's/bp   |
+------+-------------+---------+
| br   | relational  | in/br   |
+------+-------------+---------+
| b+   | enumerative | and/b+  |
+------+-------------+---------+

Auxiliary
---------

+------+----------+---------+
| Code | Subtype  | Example |
+======+==========+=========+
| an   | negation | not/an  |
+------+----------+---------+

Modifier
--------

+------+-------------+----------+
| Code | Subtype     | Example  |
+======+=============+==========+
| ma   | adjective   | green/ma |
+------+-------------+----------+
| mp   | possessive  | my/mp    |
+------+-------------+----------+
| md   | determinant | the/md   |
+------+-------------+----------+
| mn   | number      | 100/mn   |
+------+-------------+----------+

Trigger
-------

+------+-------------+-------------+
| Code | Subtype     | Example     |
+======+=============+=============+
| t?   | conditional | if/tc       |
+------+-------------+-------------+
| tt   | temporal    | when/tt     |
+------+-------------+-------------+
| tl   | local       | where/tl    |
+------+-------------+-------------+
| tm   | modal       | modal/tm    |
+------+-------------+-------------+
| t>   | causal      | because/t>  |
+------+-------------+-------------+
| t=   | comparative | like/t=     |
+------+-------------+-------------+
| tc   | concessive  | although/tc |
+------+-------------+-------------+


Type-specific additional information
====================================

The type part of the atom can include subparts after the type specifier. The meaning of these subsequent subparts is type-specific.

Concept
-------

When present, the first additional information subpart for concepts indicates number, with the following codes:

* **s**: singular, example: apple/cc.s
* **p**: plural, example: apples/cc.p

Predicate
---------

When present, the first additional information subpart for predicates is used to specify the role played in a relation by each of its parameters, with the following codes:

* **s**: subject
* **p**: passive subject
* **a**: agent
* **c**: subject complement
* **o**: direct object
* **i**: indirect object
* **x**: specifier
* **t**: parataxis
* **j**: interjection
* **r**: clausal complement

These codes are used to build strings, where each character corresponds to the parameter of the relation in the equivalent position. For example, consider the hyperedge:

(is/pd.sc (the/md sky/cc.s) blue/ca.s)

The *sc* subpart indicates that the first parameter ("the sky") plays the role of subject, and the second one ("blue"), plays the role of subject complement.

When present, the second additional information subpart for predicates is used to specify the features of the verb underlying the predicate. The following 7 features are specified:

* **tense**: past (<), present (|) or future (>)
* **verb form**: finite (f) or infinitive (i)
* **aspect**: perfect (f) or progressive (g)
* **mood**
* **person**: first (1), second (2) or third (3)
* **number**: singular (s) or plural (p)
* **verb type**

A string is built in the above order to specify the verb features of a predicate. Any feature can be left unspecified, by using a dash character (-). For example, consider the hyperedge:

(**is/p?.cs.|f--3s-** (what/mw time/cc.s) it/ci)

The predicate specifies four verb features: present tense (|), finite form (f), third person (3) and singular number (s).

Auxiliary
---------

When present, the first additional information subpart for auxiliaries is used to specify the features of the verb underlying the auxiliary. The notation is exactly the same as the one used for predicates, but in predicates this corresponds to the second additional information subpart. For example, consider the non-atomic predicate:

(have/av.|f----- (been/av.<pf---- tracking/pd.sox.|pg----))

Builder
-------

When present, the first additional information subpart for builders is used to distinguish the main concepts from the auxiliary ones, with the following codes:

* **m**: main concept
* **a**: auxiliary concept

These codes are used to build strings, where each character corresponds to the parameter of the builder in the equivalent position. For example, consider the hyperedge:

(of/br.ma founder/cc.s psychoanalysis/cc.s)

The *ma* subpart indicates that the first concept following the builder should be considered a main concept, and the next one auxiliary. This means that "founder of psychoanalysis" is a type of "founder". In other words, auxiliary concepts serve the role of making the main ones more specific.

Namespaces
==========

Namespaces serve two functions:

1. To identify the language or symbolic space to which an atom belongs;
2. To distinguish atoms that have different meanings, but would otherwise correspond to the exact same string.

In the first case, we can specify that an atom corresponds to an English word like this:

sky/cp.s/en

Or to a German word like this:

himmel/cp.s/de

Or that it is a special atom defined by Graphbrain:

+/b/.

In the second case, another subparts can be added to provide a distinction. For example, suppose we want to distinguish Cambridge (UK) from Cambridge (Mass., USA). We could use:

cambridge/cp.s/en.1

cambridge/cp.s/en.2

Special atoms
=============

+-------+-----------------------+-------------------------------+
| Atom  | Purpose               | Example                       |
+=======+=======================+===============================+
| +/b/. | Define compound nouns | (+/b/. alan/cp.s turing/cp.s) |
+-------+-----------------------+-------------------------------+
| :/b/. | Tangential concept    |                               |
+-------+-----------------------+-------------------------------+
