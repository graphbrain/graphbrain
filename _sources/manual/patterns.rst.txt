========
Patterns
========


In SH representation, an atom label that starts with upper case represents a variable. For example: ``PLAYER/C``. One can define perfectly valid hyperedges that include variables, as well as wildcards, so for example::

   (plays/P.so PLAYER/C *)

This is a valid hyperedge, but it is also a pattern that can be used to match other hyperedges. Then the ``match_pattern(edge, pattern)`` function can be used to apply patterns to edges. It works like this::

   from graphbrain import *
   from graphbrain.hyperedge import match_pattern

   pattern = hedge('(plays/P.so PLAYER/C *)')
   edge = hedge('(plays/P.so mary/C *)')
   match_pattern(edge, pattern)


In this case, we get::

   [{'PLAYER': mary/C}]

So, ``match_pattern`` gives a list of dictionaries (one pattern can match the same edge in several ways). Each dictionary represents a match, and assigns a value to a variable.
