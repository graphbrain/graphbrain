=========
Tutorials
=========

We provide a sequence of tutorials of increasing complexity, to illustrate how to perform common and useful tasks with Graphbrain.

Parsing a sentence
==================

Transforming a sentence in natural language into an hyperedge is the most fundamental and quintessential task one can perform with Graphbrain.

We start by creating a parser, in this case for the English language:

::

   from graphbrain.parsers import *
   parser = create_parser(name='en')

Initializing the parser requires loading potentially large language models. This can take from a few seconds to a minute. Let's assign some text to a variable, in this case a simple sentence:

::

    text = "The Turing test, developed by Alan Turing in 1950, is a test of machine intelligence."


Finally, let us parse the text and print the result:

::

    parses = parser.parse(text)
    for parse in parses:
        edge = parse['main_edge']
        print(edge.to_str())

Calling the ``parse()`` method on a parser object returns a collection of parses -- one per sentence. Each parse object is a dictionary, where 'main_edge' contains the hyperedge that directly corresponds to the sentence. Hyperedge objects have a ``to_str()`` method that can be used to produce a string representation. The code above should cause a single hyperedge to be printed to the screen.

Experiment with changing the text that is passed to the parser object and see what happens.


Working with notebooks
----------------------

Jupyter notebooks are a particularly handy way to perform exploratory computation with Python, and very popular for scientific applications. Graphbrain is no exception. The notebook corresponding to this tutorial can be found here:

https://github.com/graphbrain/graphbrain/blob/master/notebooks/parser.ipynb

Notice how to import the utility functions that exist specifically for working with notebooks:

::

    from graphbrain.notebook import *

The ``show()`` function allows one to render hyperedges in a nicer way. In the example above, we could replace the ``print()`` call with ``show(edge)``, and obtain something like this:

.. raw:: html

    <span style="color:#a65628"><span style="font-weight:bold;font-size:14pt;">(</span><span style="font-size:14pt;"><span style="font-weight:bold;color:#a65628">is</span><span style="color:#7F7F6F;font-size:8pt">/pd.sc.|f--3s-/en</span></span> <div style="margin-left:20px;color:#377eb8"><span style="font-weight:bold;font-size:13pt;">[</span><span style="font-size:13pt;"><span style="font-weight:bold;color:#377eb8">:</span><span style="color:#7F7F6F;font-size:8pt">/b/.</span></span> <div style="margin-left:20px;color:#4daf4a"><span style="font-weight:bold;font-size:12pt;">[</span><span style="font-size:12pt;"><span style="font-weight:bold;color:#4daf4a">the</span><span style="color:#7F7F6F;font-size:8pt">/md/en</span></span> <div style="margin-left:20px;color:#984ea3"><span style="font-weight:bold;font-size:11pt;">[</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#984ea3">+</span><span style="color:#7F7F6F;font-size:8pt">/b.mm/.</span></span> <span style="font-size:11pt;"><span style="color:#404040">turing</span><span style="color:#7F7F6F;font-size:8pt">/cp.s/en</span></span> <span style="font-size:11pt;"><span style="color:#404040">test</span><span style="color:#7F7F6F;font-size:8pt">/cc.s/en</span></span><span style="color:#984ea3"><span style="font-weight:bold;font-size:11pt;">]</span></span><span style="color:#4daf4a"><span style="font-weight:bold;font-size:12pt;">]</span></span></div></div> <div style="margin-left:20px;color:#4daf4a"><span style="font-weight:bold;font-size:12pt;">(</span><span style="font-size:12pt;"><span style="font-weight:bold;color:#4daf4a">developed</span><span style="color:#7F7F6F;font-size:8pt">/pc.ax.&lt;pf----/en</span></span> <div style="margin-left:20px;color:#984ea3"><span style="font-weight:bold;font-size:11pt;">(</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#984ea3">by</span><span style="color:#7F7F6F;font-size:8pt">/x/en</span></span> <div style="margin-left:20px;color:#ff7f00"><span style="font-weight:bold;font-size:10pt;">[</span><span style="font-size:10pt;"><span style="font-weight:bold;color:#ff7f00">+</span><span style="color:#7F7F6F;font-size:8pt">/b.am/.</span></span> <span style="font-size:10pt;"><span style="color:#404040">alan</span><span style="color:#7F7F6F;font-size:8pt">/cp.s/en</span></span> <span style="font-size:10pt;"><span style="color:#404040">turing</span><span style="color:#7F7F6F;font-size:8pt">/cp.s/en</span></span><span style="color:#ff7f00"><span style="font-weight:bold;font-size:10pt;">]</span></span><span style="color:#984ea3"><span style="font-weight:bold;font-size:11pt;">)</span></span></div></div> <div style="margin-left:20px;color:#984ea3"><span style="font-weight:bold;font-size:11pt;">{</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#984ea3">in</span><span style="color:#7F7F6F;font-size:8pt">/tt/en</span></span> <span style="font-size:11pt;"><span style="color:#404040">1950</span><span style="color:#7F7F6F;font-size:8pt">/c#/en</span></span><span style="color:#984ea3"><span style="font-weight:bold;font-size:11pt;">}</span></span><span style="color:#4daf4a"><span style="font-weight:bold;font-size:12pt;">)</span></span><span style="color:#377eb8"><span style="font-weight:bold;font-size:13pt;">]</span></span></div></div></div> <div style="margin-left:20px;color:#377eb8"><span style="font-weight:bold;font-size:13pt;">[</span><span style="font-size:13pt;"><span style="font-weight:bold;color:#377eb8">a</span><span style="color:#7F7F6F;font-size:8pt">/md/en</span></span> <div style="margin-left:20px;color:#4daf4a"><span style="font-weight:bold;font-size:12pt;">[</span><span style="font-size:12pt;"><span style="font-weight:bold;color:#4daf4a">of</span><span style="color:#7F7F6F;font-size:8pt">/br.ma/en</span></span> <div style="margin-left:20px;font-size:12pt;"><span style="color:#404040">test</span><span style="color:#7F7F6F;font-size:8pt">/cc.s/en</span></div> <div style="margin-left:20px;color:#984ea3"><span style="font-weight:bold;font-size:11pt;">[</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#984ea3">+</span><span style="color:#7F7F6F;font-size:8pt">/b.am/.</span></span> <span style="font-size:11pt;"><span style="color:#404040">machine</span><span style="color:#7F7F6F;font-size:8pt">/cc.s/en</span></span> <span style="font-size:11pt;"><span style="color:#404040">intelligence</span><span style="color:#7F7F6F;font-size:8pt">/cc.s/en</span></span><span style="color:#984ea3"><span style="font-weight:bold;font-size:11pt;">]</span></span><span style="color:#4daf4a"><span style="font-weight:bold;font-size:12pt;">]</span></span><span style="color:#377eb8"><span style="font-weight:bold;font-size:13pt;">]</span></span><span style="color:#a65628"><span style="font-weight:bold;font-size:14pt;">)</span></span></div></div></div></span>
    <br /><br />

The ``show()`` function provides several visualization styles, and also the possibility of reducing visual clutter by only displaying the roots of the atoms. Refer to the `function signature <https://graphbrain.net/api.html#graphbrain.notebook.show>`_ for all the details.

Hypergraph operations
=====================

An hypergraph can also be seen as a type of database, that stores knowledge in the form of sets of hyperedges, and provides functions that make it easy to add and search for hyperedges in useful ways. We will see here how to perform some fundamental tasks with hypergraphs.

The notebook for this tutorial can be found here:

https://github.com/graphbrain/graphbrain/blob/master/notebooks/hypergraph.ipynb

Form here on, the following imports are assumed:

::

    from graphbrain import *
    from graphbrain.notebook import *
    from graphbrain.parsers import *

Create an hypergraph
--------------------

Creating an hypergraph is straightforward:

::

    hg = hgraph('example.hg')

This assigns a hypergraph instance to ``hg``, which is physically stored as 'example.hg'. If this hypergraph already exists, it is simply opened. If it does not exist, and empty one is created.

Parse sentence and add hyperedge to hypergraph
----------------------------------------------

Let's create a parser to obtain an hyperedge from a sentence, and then add it to the hypergraph:

::


    parser = create_parser(name='en')
    text = "Mary is playing a very old violin."

    parses = parser.parse(text)
    for parse in parses:
        edge = parse['main_edge']
        hg.add(edge)

Notice that the ``add()`` function works recursively. We will see in the next subsection that not only the top hyperedge, but all of their children are added to the hypergraph.

Iterate through all edges
-------------------------

Hyoergraph objects include the function ``all()``, which returns an iterator that can be used to transverse all the hyperedges contained in the hypergraph. Let's see and example, in this case assuming that we are in a notebook environment:

::

    for edge in hg.all():
        show(edge, style='oneline')

.. raw:: html

    <div class="output_subarea output_html rendered_html"><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">(</span><span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">(</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#377eb8">is</span><span style="color:#7F7F6F;font-size:8pt">/av.|f--3s-/en</span></span> <span style="font-size:11pt;"><span style="font-weight:bold;color:#404040">playing</span><span style="color:#7F7F6F;font-size:8pt">/pd.so.|pg----/en</span></span><span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">)</span></span></span> <span style="font-size:11pt;"><span style="color:#404040">mary</span><span style="color:#7F7F6F;font-size:8pt">/cp.s/en</span></span> <span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">[</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#377eb8">a</span><span style="color:#7F7F6F;font-size:8pt">/md/en</span></span> <span style="color:#4daf4a"><span style="font-weight:bold;font-size:11pt;">[</span><span style="color:#984ea3"><span style="font-weight:bold;font-size:11pt;">(</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#984ea3">very</span><span style="color:#7F7F6F;font-size:8pt">/w/en</span></span> <span style="font-size:11pt;"><span style="font-weight:bold;color:#404040">old</span><span style="color:#7F7F6F;font-size:8pt">/ma/en</span></span><span style="color:#984ea3"><span style="font-weight:bold;font-size:11pt;">)</span></span></span> <span style="font-size:11pt;"><span style="color:#404040">violin</span><span style="color:#7F7F6F;font-size:8pt">/cc.s/en</span></span><span style="color:#4daf4a"><span style="font-weight:bold;font-size:11pt;">]</span></span><span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">]</span></span><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">)</span></span></span></span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">[</span><span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">(</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#377eb8">very</span><span style="color:#7F7F6F;font-size:8pt">/w/en</span></span> <span style="font-size:11pt;"><span style="font-weight:bold;color:#404040">old</span><span style="color:#7F7F6F;font-size:8pt">/ma/en</span></span><span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">)</span></span></span> <span style="font-size:11pt;"><span style="color:#404040">violin</span><span style="color:#7F7F6F;font-size:8pt">/cc.s/en</span></span><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">]</span></span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">[</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#a65628">a</span><span style="color:#7F7F6F;font-size:8pt">/md/en</span></span> <span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">[</span><span style="color:#4daf4a"><span style="font-weight:bold;font-size:11pt;">(</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#4daf4a">very</span><span style="color:#7F7F6F;font-size:8pt">/w/en</span></span> <span style="font-size:11pt;"><span style="font-weight:bold;color:#404040">old</span><span style="color:#7F7F6F;font-size:8pt">/ma/en</span></span><span style="color:#4daf4a"><span style="font-weight:bold;font-size:11pt;">)</span></span></span> <span style="font-size:11pt;"><span style="color:#404040">violin</span><span style="color:#7F7F6F;font-size:8pt">/cc.s/en</span></span><span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">]</span></span><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">]</span></span></span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">(</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#a65628">is</span><span style="color:#7F7F6F;font-size:8pt">/av.|f--3s-/en</span></span> <span style="font-size:11pt;"><span style="color:#404040">playing</span><span style="color:#7F7F6F;font-size:8pt">/pd.so.|pg----/en</span></span><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">)</span></span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">(</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#a65628">very</span><span style="color:#7F7F6F;font-size:8pt">/w/en</span></span> <span style="font-size:11pt;"><span style="color:#404040">old</span><span style="color:#7F7F6F;font-size:8pt">/ma/en</span></span><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">)</span></span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="font-size:11pt;"><span style="color:#000">a</span><span style="color:#7F7F6F;font-size:8pt">/md/en</span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="font-size:11pt;"><span style="color:#000">is</span><span style="color:#7F7F6F;font-size:8pt">/av.|f--3s-/en</span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="font-size:11pt;"><span style="color:#000">mary</span><span style="color:#7F7F6F;font-size:8pt">/cp.s/en</span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="font-size:11pt;"><span style="color:#000">old</span><span style="color:#7F7F6F;font-size:8pt">/ma/en</span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="font-size:11pt;"><span style="color:#000">playing</span><span style="color:#7F7F6F;font-size:8pt">/pd.so.|pg----/en</span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="font-size:11pt;"><span style="color:#000">very</span><span style="color:#7F7F6F;font-size:8pt">/w/en</span></span></div>

    <div class="output_subarea output_html rendered_html"><span style="font-size:11pt;"><span style="color:#000">violin</span><span style="color:#7F7F6F;font-size:8pt">/cc.s/en</span></span></div>

    <br /><br />

Search with patterns
--------------------

Hypergraph objects have a generic ``search()`` function, which returns iterators corresponding to sets of hyperedges that match a given pattern.

::

    edge_iterator = hg.search(pattern)

For example, with the current hypergraph, the executing the below code would produce the shown result:

::

    # '...' at the end indicates that the edge may have an arbitrary number of extra entities
    for edge in hg.search('((is/av.|f--3s-/en playing/pd.so.|pg----/en) ...)'):
        show(edge, style='oneline')

.. raw:: html

    <div class="output_subarea output_html rendered_html"><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">(</span><span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">(</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#377eb8">is</span><span style="color:#7F7F6F;font-size:8pt">/av.|f--3s-/en</span></span> <span style="font-size:11pt;"><span style="font-weight:bold;color:#404040">playing</span><span style="color:#7F7F6F;font-size:8pt">/pd.so.|pg----/en</span></span><span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">)</span></span></span> <span style="font-size:11pt;"><span style="color:#404040">mary</span><span style="color:#7F7F6F;font-size:8pt">/cp.s/en</span></span> <span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">[</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#377eb8">a</span><span style="color:#7F7F6F;font-size:8pt">/md/en</span></span> <span style="color:#4daf4a"><span style="font-weight:bold;font-size:11pt;">[</span><span style="color:#984ea3"><span style="font-weight:bold;font-size:11pt;">(</span><span style="font-size:11pt;"><span style="font-weight:bold;color:#984ea3">very</span><span style="color:#7F7F6F;font-size:8pt">/w/en</span></span> <span style="font-size:11pt;"><span style="font-weight:bold;color:#404040">old</span><span style="color:#7F7F6F;font-size:8pt">/ma/en</span></span><span style="color:#984ea3"><span style="font-weight:bold;font-size:11pt;">)</span></span></span> <span style="font-size:11pt;"><span style="color:#404040">violin</span><span style="color:#7F7F6F;font-size:8pt">/cc.s/en</span></span><span style="color:#4daf4a"><span style="font-weight:bold;font-size:11pt;">]</span></span><span style="color:#377eb8"><span style="font-weight:bold;font-size:11pt;">]</span></span><span style="color:#a65628"><span style="font-weight:bold;font-size:11pt;">)</span></span></span></span></span></div>

    <br /><br />

For all the details about how to define search patterns, refer to the ``search()`` function documentation:

https://graphbrain.net/api.html#graphbrain.hypergraph.Hypergraph.search

Parsing a book
==============

TODO


Extracting claims and conflicts from news headers
=================================================

TODO