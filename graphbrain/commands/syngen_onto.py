from graphbrain import *
from graphbrain.meaning import syngen_onto


def run(args):
    hg = hypergraph(args.hg)
    count = syngen_onto.generate(hg)
    print('{} ontology-derived synonym edges generated.'.format(count))
