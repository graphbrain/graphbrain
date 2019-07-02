from graphbrain import *
from graphbrain.meaning import ontology


def run(args):
    hg = hypergraph(args.hg)
    count = ontology.generate(hg)
    print('{} ontology edges generated.'.format(count))
