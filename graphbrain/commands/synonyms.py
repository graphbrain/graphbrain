from graphbrain import *
from graphbrain.meaning import synonyms


def run(args):
    hg = hypergraph(args.hg)
    synonyms.generate(hg, args.pattern, args.lang)
