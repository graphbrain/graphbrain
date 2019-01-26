from graphbrain.hypergraph import HyperGraph
import graphbrain.synonyms.synonyms as synonyms


def run(params):
    hg = HyperGraph(params)
    synonyms.generate(hg)
