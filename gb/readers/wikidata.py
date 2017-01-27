#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


import sys
import bz2
import json


table = {}


def concat_claims(claims):
    for rel_id, rel_claims in claims.items():
        for claim in rel_claims:
            yield claim


def to_triplets(ent):
    claims = concat_claims(ent['claims'])
    triplets = []
    e1 = ent['id']
    for claim in claims:
        mainsnak = claim['mainsnak']
        if mainsnak['snaktype'] != "value":
            continue
        if mainsnak['datatype'] == 'wikibase-item':
            rel = mainsnak['property']
            e2 = 'Q{}'.format(mainsnak['datavalue']['value']['numeric-id'])
            triplets.append((e1, rel, e2))
    return triplets


def process_line(line):
    try:
        ent = json.loads(line.decode().rstrip('\n,'))

        ent_id = ent['id']
        ent_label = ent['labels']['en']['value']
        table[ent_id] = ent_label
        #print('[%s] %s' % (ent_id, ent_label))
        return

        print(json.dumps(ent, sort_keys=False, indent=4))
        if not ent['id'].startswith('Q'):
            print("Skipping item with id {}".format(ent['id']),
                  file=sys.stderr)
            return
        print('\n'.join(
            ['{}\t{}\t{}'.format(*t) for t in to_triplets(ent)]),
            file=sys.stdout)
    except (KeyError, ValueError) as e:
        print('? %s' % e, file=sys.stderr)


def process_wikidata_dump(filename):
    source_file = bz2.BZ2File(filename, 'r')
    count = 0
    for line in source_file:
        process_line(line)
        count += 1
        if (count % 10000) == 0:
            print(count)

    source_file.close()


if __name__ == '__main__':
    process_wikidata_dump('/Users/telmo/projects/graphbrain/latest-all.json.bz2')
