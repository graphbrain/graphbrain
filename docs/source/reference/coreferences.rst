============
Coreferences
============

Coreferences indicate hyperedges that refer to the same thing. They are specified in the hypergraph using hyperedges with the special connector ``coref/p/.``. For example:

(coref/p/. turing/cp.s (+/b/. alan/cp.s turing/cp.s))

Coreference pairs such as the above define sets of coreferences that point to the same thing. Naturally, they can have arbitrary sizes. It is useful to specify the hyperedge that acts as the preferred representation for the entire coreference set. An hyperedge can also be connected to its main coreference with hyperedges such as this one:

(main_coref/p/. (of/b.ma city/cc.s berlin/cp.s) berlin/cp.s)

In the above case, ``berlin/cp.s`` is the main coreference for the coreference set that ``(of/b.ma city/cc.s berlin/cp.s)`` belongs to.