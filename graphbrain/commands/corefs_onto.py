from graphbrain import *
from graphbrain.meaning import corefs_onto


def run(args):
    hg = hypergraph(args.hg)
    count = corefs_onto.generate(hg)
    print('{} ontology-derived coreferences generated.'.format(count))
