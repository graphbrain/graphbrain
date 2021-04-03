================
Knowledge Agents
================

Graphbrain includes an agent system for the performance of cognitive tasks over Semantic Hypergraphs (SH). There are three types of agents:

- **Input**: generate or extend SHs from natural language or other sources.
- **Introspective**: infer new knowledge within a SH.
- **Output**: produce outputs from SHs to other formats (e.g. charts, conventional graphs, reports, etc.)

Agents are meant to be single-purpose and collaborative. They conform to an interface that makes it easy to compose them into systems. Systems can be defined by combining agents already provided with Graphbrain, as well as new agents that can be defined by users of the library for their own purposes.

The philosophy of the Graphbrain agent system is to allow for the definition of sophisticated tasks through the interaction of simples parts. 


Running an agent from the command line
======================================

The command line interface provides for a way to run an agent over some hypergraph, using the general 'run' command::

   graphbrain --hg <hypergraph> --agent <agent name> run

Optional command line arguments might be required, depending on the agent. For example, let us say that we want to generate parse a text file into a hypergraph, which requires the specification of an input file and a hyperedge sequence::

   graphbrain --hg books --sequence alice-in-wonderland --infile alice.txt --agent txt_parser run 

The --agent argument specifies a module that implements an agent. If the namespace for the module is not specified (i.e. if it contains no dots to specify the package that the module belongs to), then it is assumed that it belongs to the package graphbrain.cognition.agents. In other words, it is assumed to be one of the agents that come with the library.

Suppose that the agent example.molecules was created. Then, it could be executed like so::

   graphbrain --hg chemistry --agent example.molecules run


Running an agent programmatically
=================================

It is naturally useful to be able to run a agents from inside programs. Agents must always run inside an agent system (we will discuss these subsequently), but Graphbrain provides a high-level utility to function that takes abstracts all such details. For example::

   from graphbrain import hgraph
   from graphbrain.cognition.system import run_agent

   hg = hgraph('test.hg')

   run_agent('conflicts', hg=hg)


The ``run_agent()`` function accepts a number of optional parameters, which namely allow for all the specifications that are also available from the command line. The full signature of the function is::

   run_agent(agent, lang=None, hg=None, infile=None, indir=None, url=None,
             sequence=None, progress_bar=True, corefs='resolve',
             logging_level=logging.INFO)


Defining an agent systems and running them from the command line
================================================================

Agents can be organized into systems to perform more complex tasks. The simplest realization of this idea is that of a pipeline, which is prevalent in NLP. Some task is performed, and then another tasks is performed, taking advantage of the outputs of the previous one. In Graphbrain, such sequences can happen in two ways:

- **Dependency**: if agent A depends on agent B, then agent B will only be triggered to perform its tasks once agent A is finished. Agent A writes its outputs to the hypergraph, and then agent B can take advantage of the information that was stored in the hypergraph by agent A.
- **Flow**: if agent B receives an input from agent A, then the hyperedges produced by agent A are not stored in the hypergraph. Instead, they are directly fed to agent B for further processing.

Both dependency and flow relationships can be multiplexed, in the sense that several agents can depend on the same one, or several agents can receive an input from the same one.

Agent systems can be specified in JSON. Let us consider a case where the ``wikipedia`` agent is used to retrieve and parse the contents of a wikipedia page, and ``taxonomy`` is used to derive the taxonomy of concepts, followed by ``corefs_onto`` to determine the co-reference sets of concepts::

   {
       "wikipedia": {
           "agent": "wikipedia"
       },
       "taxonomy": {
           "agent": "taxonomy",
           "depends_on": "wikipedia"
       },
       "corefs_onto": {
           "agent": "corefs_onto",
           "depends_on": "taxonomy"
       }
   }


Let us say we save this file as ``wikipedia.sys``. We can then run this system from the command line, for example like this::

   graphbrain --hg wikipedia.hg --url https://en.wikipedia.org/wiki/Artificial_intelligence --system systems/wikipedia.sys run

Notice that the arguments that are specified in the command line are transparently transfered to the agents in the system that make use of them. For example, the ``wikipedia`` agent requires an input url (``--url https://en.wikipedia.org/wiki/Artificial_intelligence``) and a hypergraph to write to (``--hg wikipedia.hg``). Then both ``taxonomy`` and ``corefs_names``, which are introspective, both require a hypergraph to read/write to. The same one is used. Agent systems are meant to operate on a given hypergraph, to which all agents refer to when performing their operations.


Defining and running an agent system programmatically
=====================================================

TODO


Developing your own agents
==========================

TODO


Agent bestiary
==============

txt_parser
----------

**Type**: input

**Languages**: all

Takes a text file as input and converts each one of its sentences to hyperedges, adding them to the hypergraph.

This is a very simple but also useful, general-purpose agent.

reddit_parser
-------------

**Type**: input

**Languages**: all

Takes a Reddit JSON corpus as input and converts each one of thread titles, and optionally thread comments to hyperedges, adding them to the hypergraph. Titles and comments are attributed to authors.

taxonomy
--------

**Type**: introspective

**Languages**: agnostic

Derives a taxonomy from concepts defined with builders of modifiers. For example, ``(of/Br.ma founder/Cc.s psychoanalysis/Cc.s)`` is a type of ``founder/Cc.s``, so the following hyperedge is added::

   (type_of/P/. (of/Br.ma founder/Cc.s psychoanalysis/Cc.s) founder/Cc.s)

Or, if we consider modifier-defined concepts such as ``(black/Ma cat/Cc.s)``::

   (type_of/P/. (black/Ma cat/Cc.s) cat/Cc.s)


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

Performs `coreference resolution <https://graphbrain.net/reference/special-relations.html#coreferences>`_ for atoms that become equal after unidecode() is a applied to both labels. For example, it will create a coreference relation between ``über/C`` and ``uber/C``.


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

   (actor/P/. mary/Cp.s/en)

The above simply means that ``mary/Cp.s/en`` was identified as an actor.


claims
------

**Type**: introspective

**Languages**: English

**Depends on**: coreference resolution

Identifies hyperedges that represent a claim. Claims are sentences such as: "North Korea says it's not afraid of US military strike". The claim is that "North Korea is not afraid of US military strike" and the author of the claim is "North Korea".

More specifically, claims are detected according to the following criteria:

1. Hyperedge is a relation with predicate of type ``Pd``.
2. The deep predicate atom of the predicate hyperedge has a lemma belonging to a predetermined lists of verb lemmas that denote a claim (e.g.: "say", "claim").
3. The hyperedge has a subject and a clausal complement. The first is used to identify the actor making the claim, the second the claim itself.

Claim relations follow the format::

   (claim/P/. *actor* *claim* *edge*)

Furthermore, simple anaphora resolution on the claim is performed (e.g. in "Pink Panther says that she loves pink.", the hyperedge for "she" is replaced with the hyperedge for "Pink Panther" in the claim). In these cases, pronouns are used to guess gender or nature of actors. Actors can be classified as female::

   (female/P/. *actor*)

Or as a group::

   (group/P/. *actor*)

Or as male::

   (male/P/. *actor*)

Or as non-human::

   (non-human/P/. *actor*)

conflicts
---------

**Type**: introspective

**Languages**: English

**Depends on**: coreference resolution

Identifies hyperedges that represent a conflict. Conflicts are sentences such as: "Germany warns Russia against military engagement in Syria". The source of the expression of conflict here is "Germany", the target is "Russia" and the topic is "military engagement in Syria".

More specifically, claims are detected according to the following criteria:

1. Hyperedge is a relation with predicate of type ``Pd``.
2. The deep predicate atom of the predicate hyperedge has a lemma belonging to a predetermined lists of verb lemmas that denote an expression of conflict (e.g.: "warn", "kill").
3. The hyperedge has a subject and an object. The first is used to identify the actor originating the expression of conflict and the second the actor which is the target of this expression.
4. [optional] Beyond subject and object, if any specifier arguments are present, and their trigger atoms belong to a predetermined list (e.g. "over", "against"), then topics of conflict are extracted from these specifiers.

Conflict relations follow the format::

   (conflict/P/. *actor_orig* *actor_targ* *edge*)

These conflict relations are connected to their topics by further relations with the format::

   (conflict-topic/P/. *actor_orig* *actor_targ* *concept* *edge*)
