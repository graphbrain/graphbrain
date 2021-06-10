import json

from graphbrain import hgraph, hedge


def run(args):
    print('importing hypergraph...')
    hg = hgraph(args.hg)
    n = 0
    with open(args.infile, 'r') as f:
        for line in f:
            edge_str, attributes = json.loads(line)
            hg.add_with_attributes(hedge(edge_str), attributes)
            n += 1
    print('{} edges imported.'.format(n))
