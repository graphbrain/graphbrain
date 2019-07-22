from graphbrain import *
from graphbrain.meaning import corefs_names


def run(args):
    hg = hypergraph(args.hg)
    count = corefs_names.generate(hg)
    print('{} proper name coreferences generated.'.format(count))
