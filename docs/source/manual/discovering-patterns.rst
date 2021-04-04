====================
Discovering patterns
====================

Patterns are useful to identify hyperedge structures from which knowledge can be inferred. To facilitate the discovery of such patterns, Graphbrain provides the ``PatternCounter``, a class in the ``graphbrain.patterns`` module. By creating an instance of this class, one can feed it a collection of hyperedges and then obtain the most frequent patterns inferred from these hyperedges. This way, empirical sources can be used to guide the discovery of knowledge inference methods. We will see that ``PatternCounter`` can be tuned to focus on specific constructs and to operate at different levels of abstraction.


A simple example
================

To exemplify, let us first generate a hypergraph parsed from real text. For this, we will use the "wikipedia" agent. Agents will be discussed in a subsequent section, but for now it is enough to know that the "wikipedia" agent extracts the text from a Wikipedia page identified by it's URL, and then populates a hypergraph by parsing each sentence of the text into a hyperedge. Let us run the agent from the command line::

   $ graphbrain --hg ai.hg --url https://en.wikipedia.org/wiki/Artificial_intelligence --agent wikipedia run


Once it finishes, we will have the ``ai.hg`` hypergraph database in the current directory. Let us now simply find the most common patterns in this hypergraph::

   from graphbrain import hgraph
   from graphbrain.patterns import PatternCounter

   hg = hgraph('ai.hg')
   pc = PatternCounter()
   for edge in hg.all():
       if hg.is_primary(edge):
           pc.count(edge)

We see above that we create a ``PatternCounter`` object and then iterate through all the primary hyperedges in the hypergraph, feeding it to the counter one at a time by way of calling the ``count(edge)`` method. By default, the ``PatternCounter`` recurses into the internal structure of each hyperedge, so inner constructs are also counted. Let us now query for the 10 most common patterns found::

   >> pc.patterns.most_common(10)
   [((*/M */C), 2208),
    ((*/J */C */C), 1867),
    ((*/B.ma */C */C), 1315),
    ((*/J */M */M), 708),
    ((*/J */P */P), 628),
    ((*/M (*/M */C)), 456),
    ((*/T */C), 448),
    ((*/B.ma (*/M */C) */C), 385),
    ((*/B.ma */C (*/M */C)), 315),
    ((*/B.mm */C */C), 266)]

We see that most patterns are related to concept construction. We can set the ``count_subedges`` argument to ``False`` to only focus on the top-level structures::

   pc = PatternCounter(count_subedges=False)
   for edge in hg.all():
       if hg.is_primary(edge):
           pc.count(edge)

   >> pc.patterns.most_common(10)
   [((*/J */C */C), 1569),
    ((*/J */M */M), 708),
    ((*/J */P */P), 628),
    ((*/J */R */R), 131),
    ((*/J */T */T), 55),
    ((*/J */B */B), 30),
    ((*/B.mm */C */C), 29),
    ((*/J (*/J */R */R) */R), 29),
    ((*/P.so */C */C), 26),
    ((*/J */R */C), 24)]

Here we see that the results become more dominated by relational constructs.


Focusing the exploration by controlling expansions
==================================================

The ``PatternCounter`` works by recursively expanding the given hyperedge up to a maximum depth is reached (``max_depth`` argument with default ``2``). Notice that the atom ``*`` is the most general pattern possible, matching anything. For example, with typing, all relations would generalize to ``*/R``. If instead we keep going, and recursively "expand" the hyperedge into its constituent elements, replacing these elements with typed wildcards, we get less general but possibly more useful patterns, as the ones we saw above.

By default, all hyperedges are expanded. The constructor argument ``expansions`` defaults to ``{'*'}``. It is however possible to have more control, and focus the expansions on more specific things that interest us. This is achieved by specifying patterns. Each hyperedge that is fed to the counter is only expanded (and also recursively) if it happens to match one of the patterns in ``expansions``. Let us say that we are interested in relations with at least two arguments::

   
   pc = PatternCounter(expansions={'(*/P * * ...)'})
   for edge in hg.all():
       if hg.is_primary(edge):
           pc.count(edge)

   >> pc.patterns.most_common(10)
   [((*/P.so */C */C), 32),
    ((*/P.o */C), 29),
    ((*/P.sr */C */R), 25),
    ((*/P.sx */C */S), 23),
    ((*/P.sc */C */C), 21),
    ((*/P.sox */C */C */S), 19),
    ((*/P.sr */C */S), 19),
    ((*/P.px */C */S), 11),
    ((*/P.x */S), 10),
    ((*/P.ox */C */S), 9)]

Any pattern(s) can be used here to control the expansions and focus the exploration.


Including explicit roots in the patterns
========================================

The examples we have seen so far produce completely abstract patterns. It is also useful to be able to discover patterns where certain hyperedges are not wildcards, but instead contain the roots of the atoms. This is achieved with the ``match_roots`` constructor argument. Here one can include patterns that, when matched, cause the expansion to produce explicit atoms instead of wildcards. For example, we could be interested in the actual predicates of the most common relations::

   pc = PatternCounter(expansions={'(*/P * * ...)'}, match_roots={'*/P'})
   for edge in hg.all():
       if hg.is_primary(edge):
           pc.count(edge)

   >> pc.patterns.most_common(10)
   [((include/P.so */C */C), 5),
    ((is/P.sc */C */C), 4),
    ((are/P.sc */C */C), 4),
    ((have/P.so */C */C), 2),
    ((are/P.sx */C */S), 2),
    ((became/P.scx? */C */C */S */C), 2),
    ((named/P.so */C */C), 2),
    (((already/M have/P.so) */C */C), 1),
    (((also/M opens/P.sox) */C */C */S), 1),
    (((and/M come/P.sxx) */C */S */S), 1)]

Notice that ``match_roots`` also causes the expansion of the matched expression, even if it does not match any pattern in ``expansions``. We see this case above with ``(*/M */P)`` structures. Exploiting the machinery of patterns, many variations can be achieved. For example, we might be interested in only matching the roots of the predicate atoms, but keep the rest of the predicate abstract. This can be achieved by using ``match_roots={'./P'}`` (remember that the ``.`` wildcard only matched atoms)::

   pc = PatternCounter(expansions={'(*/P * * ...)'}, match_roots={'./P'})
   for edge in hg.all():
       if hg.is_primary(edge):
           pc.count(edge)
   
   >> pc.patterns.most_common(10)
   [((include/P.so */C */C), 5),
    ((is/P.sc */C */C), 4),
    ((are/P.sc */C */C), 4),
    ((have/P.so */C */C), 2),
    ((are/P.sx */C */S), 2),
    ((became/P.scx? */C */C */S */C), 2),
    ((named/P.so */C */C), 2),
    (((*/M have/P.so) */C */C), 1),
    (((*/M opens/P.sox) */C */C */S), 1),
    (((*/M come/P.sxx) */C */S */S), 1)]
