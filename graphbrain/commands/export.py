import json

from graphbrain import hgraph


def run(args):
    print('exporting hypergraph...')
    hg = hgraph(args.hg)
    n = 0
    with open(args.outfile, 'w') as f:
        for edge, attributes in hg.all_attributes():
            row = [edge.to_str(), attributes]
            f.write('{}\n'.format(
                json.dumps(row, encoding='utf-8', ensure_ascii=False)))
            n += 1
    print('{} edges exported.'.format(n))
