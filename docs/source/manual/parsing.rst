========================
Parsing Natural Language
========================


Parsing natural language to semantic hypergraphs is another crucial aspect of Graphbrain. This aspect of the library is covered by the ``graphbrain.parsers`` package. Let us see how to create a parser, and how to use it to parse a sentence::

   >>> from graphbrain.parsers import create_parser
   >>> parser = create_parser('en')
   >>> parse_results = parser.parse('Einstein first published the theory of relativity in 1905')
   >>> parse_results['parses'][0]['main_edge']
   ((first/M/en published/Pd.sox.<f-----/en) einstein/Cp.s/en (of/Br.ma/en (the/Md/en theory/Cc.s/en) relativity/Cc.s/en) (in/Tt/en 1905/C#/en))

Notice that ``create_parser()`` takes a some time (10 to 20 seconds is normal), because it has to load the underlying language models. Here we pass it the only mandatory parameter, which is the name of the parser to use. This will typically correspond to the language to parse. Currently only one parser for English is provided ("en"), but Graphbrain already comes with auxiliary scripts to help in the development of parsers for other languages.

We also see that we specify a position in the ``parse_results`` data structure (at the top-level, it is a dictionary) to directly access the hyperedge that corresponds to the parsed sentence. This is because ``parse_results`` includes useful additional information about the parsed text, and also because the parsed text can correspond to more than one hyperedge. So let us take a look at the full contents of ``parse_results`` from the example above::

   >>> parse_results
   {'parses':
       ({'main_edge': ((first/M/en published/Pd.sox.<f-----/en) einstein/Cp.s/en (of/Br.ma/en (the/Md/en theory/Cc.s/en) relativity/Cc.s/en) (in/Tt/en 1905/C#/en)),
         'extra_edges': set(),
         'failed': False,
         'text': 'Einstein first published the theory of relativity in 1905',
         'atom2word': {first/M/en: ('first', 1),
                       published/Pd.sox.<f-----/en: ('published', 2),
                       einstein/Cp.s/en: ('Einstein', 0),
                       of/Br.ma/en: ('of', 5),
                       the/Md/en: ('the', 3),
                       theory/Cc.s/en: ('theory', 4),
                       relativity/Cc.s/en: ('relativity', 6),
                       in/Tt/en: ('in', 7),
                       1905/C#/en: ('1905', 8)},
         'atom2token': {einstein/Cp.s/en: Einstein,
                        first/M/en: first,
                        published/Pd.sox.<f-----/en: published: published,
                        the/Md/en: the,
                        theory/Cc.s/en: theory,
                        of/Br.ma/en: of,
                        relativity/Cc.s/en: relativity,
                        in/Tt/en: in,
                        1905/C#/en: 1905},
         'spacy_sentence': Einstein first published the theory of relativity in 1905},),
    'inferred_edges': []}


Lemmas
======

TODO


Parser-level coreference resolution
===================================

TODO


Parser-level inferences
=======================

TODO