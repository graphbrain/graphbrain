from graphbrain import *
from graphbrain.meaning import syngen_names


def run(args):
    hg = hypergraph(args.hg)
    count = syngen_names.generate(hg)
    print('{} proper name synonym edges generated.'.format(count))
