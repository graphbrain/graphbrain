from graphbrain import *
from graphbrain.meaning import *
# import graphbrain.synonyms.synonyms as synonyms


def run(args):
    hg = hypergraph(args.hg)
    iter = hg.pat2ents(args.pattern)
    for edge in iter:
        print(ent2str(edge))

    # synonyms.generate(hg)
