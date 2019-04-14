from graphbrain import *


def run(args):
    hg = hypergraph(args.hg)
    print('edge_symbols: {}'.format(hg.symbol_count()))
    print('edges: {}'.format(hg.edge_count()))
    print('total degree: {}'.format(hg.total_degree()))
