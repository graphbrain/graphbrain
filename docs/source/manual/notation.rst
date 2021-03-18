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
| C    | concept       | Define atomic concepts       | **apple/C**                    |
+------+---------------+------------------------------+--------------------------------+
| P    | predicate     | Build relations              | (**is/P** berlin/C nice/C)     |
+------+---------------+------------------------------+--------------------------------+
| M    | modifier      | Modify concepts, predicates, | (**red/M** shoes/C)            |
+      +               +                              +                                +
|      |               | modifiers or triggers        |                                |
+------+---------------+------------------------------+--------------------------------+
+ **Atomic only**                                                                      +
+------+---------------+------------------------------+--------------------------------+
| B    | builder       | Build concepts from concepts | (**of/B** capital/C germany/C) |
+------+---------------+------------------------------+--------------------------------+
| T    | trigger       | Build specifications         | (**in/T** 1994/C)              |
+------+---------------+------------------------------+--------------------------------+
| J    | conjunction   | Define sequences of concepts | (**and/J** meat/C potatoes/C)  |
+      +               +                              +                                +
|      |               | or relations                 |                                |
+------+---------------+------------------------------+--------------------------------+
+ **Non-atomic only**                                                                  +
+------+---------------+------------------------------+--------------------------------+
| R    | relation      | Express facts, statements,   | **(is/P berlin/C nice/C)**     |
+      +               +                              +                                +
|      |               | questions, orders, ...       |                                |
+------+---------------+------------------------------+--------------------------------+
| S    | specifier     | Relation specification       | **(in/T 1976/C)**              |
+      +               +                              +                                +
|      |               | (e.g. condition, time, ...)  |                                |
+------+---------------+------------------------------+--------------------------------+


Type inference rules
====================

+---------------+----------------+
| Element types | Resulting type |
+===============+================+
| (B  C  C+)    | C              |
+---------------+----------------+
| (M  C)        | C              |
+---------------+----------------+
| (M  M)        | M              |
+---------------+----------------+
| (M  P)        | P              |
+---------------+----------------+
| (M  T)        | T              |
+---------------+----------------+
| (P  [CRS]+)   | R              |
+---------------+----------------+
| (T  [CR]+)    | S              |
+---------------+----------------+
| (J  C+)       | C              |
+---------------+----------------+
| (J  R+)       | R              |
+---------------+----------------+

We use the notation of regular expressions: the symbol ``+`` is used to denote an arbitrary number of entities with the type that precedes it, while square brackets indicate several possibilities (for instance, ``[CR]+`` means "at least one of any of both ``C`` or ``R`` types).


Subtypes
========

Concept
-------

+------+---------------+----------+
| Code | Subtype       | Example  |
+======+===============+==========+
| Cc   | common        | apple/Cc |
+------+---------------+----------+
| Cp   | proper        | mary/Cp  |
+------+---------------+----------+
| Cn   | number        | 27/Cn    |
+------+---------------+----------+
| Ci   | pronoun       | she/Ci   |
+------+---------------+----------+
| Cw   | interrogative | who/Cw   |
+------+---------------+----------+

Predicate
---------

+------+---------------+---------+
| Code | Subtype       | Example |
+======+===============+=========+
| Pd   | declarative   | is/Pd   |
+------+---------------+---------+
| P?   | interrogative | is/P?   |
+------+---------------+---------+
| P!   | imperative    | go/P!   |
+------+---------------+---------+

Builder
-------

+------+-------------+---------+
| Code | Subtype     | Example |
+======+=============+=========+
| Bp   | possessive  | 's/Bp   |
+------+-------------+---------+
| Br   | relational  | in/Br   |
+------+-------------+---------+


Modifier
--------

+------+-------------+----------+
| Code | Subtype     | Example  |
+======+=============+==========+
| Ma   | adjective   | green/Ma |
+------+-------------+----------+
| Mp   | possessive  | my/Mp    |
+------+-------------+----------+
| Md   | determinant | the/Md   |
+------+-------------+----------+
| M#   | number      | 100/M#   |
+------+-------------+----------+
| Mn   | negation    | not/Mn   |
+------+-------------+----------+
| Mv   | verbal      | will/Mv  |
+------+-------------+----------+

Trigger
-------

+------+-------------+-------------+
| Code | Subtype     | Example     |
+======+=============+=============+
| T?   | conditional | if/Tc       |
+------+-------------+-------------+
| Tt   | temporal    | when/Tt     |
+------+-------------+-------------+
| Tl   | local       | where/Tl    |
+------+-------------+-------------+
| Tm   | modal       | modal/Tm    |
+------+-------------+-------------+
| T>   | causal      | because/T>  |
+------+-------------+-------------+
| T=   | comparative | like/T=     |
+------+-------------+-------------+
| Tc   | concessive  | although/Tc |
+------+-------------+-------------+


Type-specific additional information
====================================

The type part of the atom can include subparts after the type specifier. The meaning of these subsequent subparts is type-specific.

Concept
-------

When present, the first additional information subpart for concepts indicates number, with the following codes:

* **s**: singular, example: apple/Cc.s
* **p**: plural, example: apples/Cc.p

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

(is/Pd.sc (the/Md sky/Cc.s) blue/Ca.s)

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

(**is/P?.cs.|f--3s-** (what/Mw time/Cc.s) it/Ci)

The predicate specifies four verb features: present tense (|), finite form (f), third person (3) and singular number (s).

Modifier
--------

When the modifer is verbal, the first additional information subpart is used to specify the features of the underlying verb. The notation is exactly the same as the one used for predicates, but in predicates this corresponds to the second additional information subpart. For example, consider the non-atomic predicate:

(have/Mv.|f----- (been/Mv.<pf---- tracking/Pd.sox.|pg----))

Builder
-------

When present, the first additional information subpart for builders is used to distinguish the main concepts from the auxiliary ones, with the following codes:

* **m**: main concept
* **a**: auxiliary concept

These codes are used to build strings, where each character corresponds to the parameter of the builder in the equivalent position. For example, consider the hyperedge:

(of/Br.ma founder/Cc.s psychoanalysis/Cc.s)

The *ma* subpart indicates that the first concept following the builder should be considered a main concept, and the next one auxiliary. This means that "founder of psychoanalysis" is a type of "founder". In other words, auxiliary concepts serve the role of making the main ones more specific.

Namespaces
==========

Namespaces serve two functions:

1. To identify the language or symbolic space to which an atom belongs;
2. To distinguish atoms that have different meanings, but would otherwise correspond to the exact same string.

In the first case, we can specify that an atom corresponds to an English word like this:

sky/Cp.s/en

Or to a German word like this:

himmel/Cp.s/de

Or that it is a special atom defined by Graphbrain:

+/B/.

In the second case, another subparts can be added to provide a distinction. For example, suppose we want to distinguish Cambridge (UK) from Cambridge (Mass., USA). We could use:

cambridge/Cp.s/en.1

cambridge/Cp.s/en.2

Special atoms
=============

+-------+-----------------------+-------------------------------+
| Atom  | Purpose               | Example                       |
+=======+=======================+===============================+
| +/B/. | Define compound nouns | (+/B/. alan/Cp.s turing/Cp.s) |
+-------+-----------------------+-------------------------------+
| :/B/. | Tangential concept    |                               |
+-------+-----------------------+-------------------------------+
