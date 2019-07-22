from graphbrain import *
from graphbrain.meaning import corefs_atoms


def run(args):
    hg = hypergraph(args.hg)
    count = corefs_atoms.generate(hg)
    print('{} atom coreferences generated.'.format(count))
