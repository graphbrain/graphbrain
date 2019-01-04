from graphbrain.hypergraph import HyperGraph
import graphbrain.synonyms.synonyms as synonyms


def generate_synonyms(params):
    hg = HyperGraph(params)
    synonyms.generate(hg)
