from graphbrain.hypergraph import HyperGraph


def run(params):
    hg = HyperGraph(params)
    print('edge_symbols: %s' % hg.symbol_count())
    print('edges: %s' % hg.edge_count())
    print('total degree: %s' % hg.total_degree())
