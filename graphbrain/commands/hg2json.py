import json
from graphbrain.hypergraph import HyperGraph


def edge2dict(hg, edge):
    return {'edge': edge2str(edge), 'text': hg.get_str_attribute(edge, 'text')}


def run(params):
    hg = HyperGraph(params)
    outfile = params['outfile']

    edge_data = [edge2dict(hg, edge) for edge in hg.all()]

    with open(outfile, 'w') as json_file:
        json.dump(edge_data, json_file)
