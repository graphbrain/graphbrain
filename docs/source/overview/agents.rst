================
Knowledge Agents
================

Knowledge agents are programs that manipulate hypergraphs in some way. They can be introspective, working only on the current contents of the hypergraph to derive new knowledge. For example, the *taxonomy* agent infers simple taxonomies from concepts. It can infer that 'black cat' is a type of 'cat' or that 'city of Berlin' is a type of 'city'.

It produces new hyperedges such as::

   (type_of/p/. city/c (of/b city/c berlin/c))

Certain agents use outside sources to introduce knowledge into hypergraphs. For example, the *txt_parser* agent receives as input a simple text file and converters each sentence that it detects in it into an hyperedge.

You can find the full list of agents that are distributed with Graphbrain here:

https://graphbrain.net/reference/agents.html
