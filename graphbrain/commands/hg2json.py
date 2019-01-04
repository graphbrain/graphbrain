from graphbrain.hypergraph import HyperGraph
from graphbrain.filters.filters import AllFilter


def run(params):
    hg = HyperGraph(params)
    outfile = params['outfile']
    filt = AllFilter(hg)
    filt.write_edges(outfile)
