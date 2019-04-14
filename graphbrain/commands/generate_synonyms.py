from graphbrain import *
import graphbrain.synonyms.synonyms as synonyms


def run(args):
    hg = hypergraph(args.hg)
    synonyms.generate(hg)
