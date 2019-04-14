import json
from graphbrain import *


def edge2dict(hg, edge):
    return {'edge': ent2str(edge),
            'text': hg.get_str_attribute(edge, 'text')}


def run(args):
    hg = hypergraph(args.hg)

    edge_data = [edge2dict(hg, edge) for edge in hg.all()]

    with open(args.outfile, 'w') as json_file:
        json.dump(edge_data, json_file)
