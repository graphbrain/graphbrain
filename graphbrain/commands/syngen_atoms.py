from graphbrain import *
from graphbrain.meaning import syngen_atoms


def run(args):
    hg = hypergraph(args.hg)
    count = syngen_atoms.generate(hg)
    print('{} atom synonym edges generated.'.format(count))
