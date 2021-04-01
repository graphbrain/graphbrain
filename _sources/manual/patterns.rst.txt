========
Patterns
========


As briefly discussed before, Graphbrain provides a pattern language to query and match hyperedges in the hypergraph. These patterns are valid hyperedges, and can even be stored in the hypergraph themselves. Patterns are defined with the help of special atoms: *wildcards* and *variables*. The former are useful for simply querying and matching, while the latter allow for the extraction of specific parts of a hyperedge.


Searching with wildcards
========================

The basic wildcard is ``*``. It matches any hyperedge. It allows for the definition of patterns like this::

   (plays/P * *)

We can use them to search the hypergraph for matching hyperedges. For example::

   >>> from graphbrain import *
   >>> hg = hgraph('example.hg')
   >>> hg.destroy()
   >>> hg.add('(plays/Pd.so alice/C chess/C)')
   (plays/Pd.so alice/C chess/C)
   >>> list(hg.search('(plays/P * *)'))
   [(plays/Pd.so alice/C chess/C)]

Notice that the specified atoms, such as ``plays/P``, match atoms in the target hyperedge in the most general way, meaning that, if a subtype or other roles are not specified in the pattern, then any subtypes or argroles will match, as can be seen in the example above.

There are two more wildcard:

- ``.``, which only matches atoms.
- ``(*)``, which only matches non-atomic hyperedges.

Furthermore, it is possible to specify types and roles in wildcards, as in any other atom. For example ``*/C`` only matches hyperedges of type concept. ``(*/C)`` only matches non-atomic hyperedges of type concept, and so on.

It is possible to specify the optional presence of further arguments with the special atom ``...``, which simply indicates that any number (including zero) hyperedges may be present at that point. For instance::

   (plays/P * *)

does not match::

   (plays/P.sox alice/C chess/C (at/T (the/M club/C)))

but this pattern does::

   (plays/P * * ...)


Matching argroles
=================

If the connector indicates argument roles, then any further arguments may be present, unless indicated otherwise. So::

   (plays/P.so * *)

matches::

   (plays/P.sox alice/C chess/C (at/T (the/M club/C)))

When specifying argroles, more often than not this is the behavior that is the most useful. 

It is often desirable to allow the various pattern elements to appear in any order. This is indicated by surrounding the roles of such elements with curly brackets. For example::

   (is/P.{sc} * */C)

The above pattern would match both::

   (is/P.sc (the/M sky/C) blue/C)
   (is/P.cs blue/C (the/M sky/C))

Sometimes it is also desirable to explicitly forbid certain argument roles. This is achieved by indicating them after '-' in the argrole sequence. For example::

   (plays/P.{so}-x * *)

does not match::

   (plays/P.sox alice/C chess/C (at/T (the/M club/C)))


Patterns with variables for information extraction
==================================================

Let us introduce the concept of *variable*. Like a wildcard, a variable indicates a placeholder that can match a hyperedge, but can then be used to refer to that matched hyperedge. In SH representation, an atom label that starts with upper case represents a variable. For example: ``PLAYER/C``. One can define perfectly valid hyperedges that include variables, as well as wildcards, so for example::

   (plays/P.{so} PLAYER/C *)

Then the ``match_pattern(edge, pattern)`` function can be used to apply patterns to edges. It works like this::

   >> from graphbrain import hedge
   >> from graphbrain.hyperedge import match_pattern
   >> pattern = hedge('(plays/P.{so} PLAYER/C *)')
   >> edge = hedge('(plays/P.so mary/C *)')
   >> match_pattern(edge, pattern)
   [{'PLAYER': mary/C}]

So, ``match_pattern`` gives a list of dictionaries (one pattern can match the same edge in several ways). Each dictionary represents a match, and assigns a value to a variable.

The ``Hypergraph`` object provides the ``match()`` method , which is similar to ``search()`` but returns dictionaries with the matched variables::

   TODO


Discovering frequent patterns
=============================

TODO