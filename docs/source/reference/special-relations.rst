=================
Special relations
=================

Coreferences
============

Coreferences indicate hyperedges that refer to the same thing. They are specified in the hypergraph using hyperedges with the special connector ``coref/p/.``. For example::

   (coref/p/. turing/cp.s (+/b/. alan/cp.s turing/cp.s))

Coreference pairs such as the above define sets of coreferences that point to the same thing. Naturally, they can have arbitrary sizes. It is useful to specify the hyperedge that acts as the preferred representation for the entire coreference set. An hyperedge can also be connected to its main coreference with hyperedges such as this one::

   (main_coref/p/. (of/b.ma city/cc.s berlin/cp.s) berlin/cp.s)

In the above case, ``berlin/cp.s`` is the main coreference for the coreference set that ``(of/b.ma city/cc.s berlin/cp.s)`` belongs to.


Taxonomies
==========

Taxonomies can be built using *type-of* relations between hyperedges, with the help of the special predicate ``type_of/p/.``. For example::

   (type_of/p/. (of/b.ma city/cc.s berlin/cp.s) city/cc.s)


Lemmas
======

In informal and simple terms, *lemma* refers to the root of a word. Words and derived from lemma for example by verb conjugation, gender, number, grammatical case, etc. It can be useful to know that two atoms share the same lemma, e.g. to infer that two predicates refer to the same verb.

Lemma relations are stored in the hypergraph using the following hyperedge format::

   (lemma/p/. asked/pd.so.<pf----/en ask/p/en)

The last parameter of the hyperedge is the lemma atom. Lemma atoms only have the main type, with no further specification (e.g. ``ask/p/en`` but not ``ask/pd/en`` or ``ask/pd.so.|pf----/en``). The intent is to keep an appropriate level of generality.

The parsers provided with Graphbrain generate lemma relations by default.