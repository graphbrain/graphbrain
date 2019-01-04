import os.path
import bz2
import json
import sqlite3
from graphbrain.funs import *


ENTITY_TABLE_PATH = 'wikidata_entity_table.temp'


def entity_table_exists():
    return os.path.isfile(ENTITY_TABLE_PATH)


class EntityTable(object):
    def __init__(self):
        self.conn = sqlite3.connect(ENTITY_TABLE_PATH)
        cur = self.conn.cursor()
        try:
            cur.execute('CREATE TABLE entities (wikidata_id TEXT PRIMARY KEY, symbol TEXT)')
            self.conn.commit()
        except sqlite3.OperationalError:
            pass
        cur.close()
        self.cur = None
        self.counter = 0

    def grab_cursor(self):
        self.counter += 1
        if self.cur is None:
            self.cur = self.conn.cursor()
        return self.cur

    def release_cursor(self):
        if self.counter % 10000 == 0:
            self.conn.commit()
            self.cur.close()
            self.cur = None

    def add(self, wikidata_id, label):
        cur = self.grab_cursor()
        symbol = str2symbol(label)
        cur.execute('INSERT INTO entities (wikidata_id, symbol) VALUES (?, ?)', (wikidata_id, symbol))
        self.release_cursor()

    def get(self, wikidata_id):
        cur = self.grab_cursor()
        cur.execute('SELECT symbol FROM entities WHERE wikidata_id=?', (wikidata_id,))
        res = cur.fetchone()
        if res:
            return res[0]
        return None

    def close(self):
        cur = self.grab_cursor()
        self.conn.commit()
        cur.close()
        self.cur = None


def concat_claims(claims):
    for rel_id, rel_claims in claims.items():
        for claim in rel_claims:
            yield claim


def to_symbol(wikidata_id, label):
    ns = 'wd%s' % wikidata_id
    return build_symbol(label, ns)


def to_triplets(ent):
    claims = concat_claims(ent['claims'])
    triplets = []
    e1 = ent['id']
    for claim in claims:
        mainsnak = claim['mainsnak']
        if mainsnak['snaktype'] != 'value':
            continue
        if mainsnak['datatype'] == 'wikibase-item':
            rel = mainsnak['property']
            e2 = 'Q{}'.format(mainsnak['datavalue']['value']['numeric-id'])
            triplets.append((e1, rel, e2))
    return triplets


class WikidataReader(object):
    def __init__(self, hg):
        self.hg = hg
        self.entity_table = EntityTable()
        self.entities_found = 0
        self.edges_found = 0
        self.edge_buffer = []

    def extract_entity(self, line):
        try:
            ent = json.loads(line.decode().rstrip('\n,'))
            wikidata_id = ent['id']
            try:
                label = ent['labels']['en']['value']
                self.entity_table.add(wikidata_id, label)
                self.entities_found += 1
            except (KeyError, ValueError):
                pass
        except (KeyError, ValueError) as e:
            print('exception: %s' % e)

    def triplet_to_edge(self, triplet):
        ent1 = self.entity_table.get(triplet[0])
        if ent1 is None:
            return None
        rel = self.entity_table.get(triplet[1])
        if rel is None:
            return None
        ent2 = self.entity_table.get(triplet[2])
        if ent2 is None:
            return None
        ent1 = to_symbol(triplet[0], ent1)
        rel = to_symbol(triplet[1], rel)
        ent2 = to_symbol(triplet[2], ent2)
        return rel, ent1, ent2

    def extract_triplets(self, line):
        try:
            ent = json.loads(line.decode().rstrip('\n,'))
            if not ent['id'].startswith('Q'):
                print("Skipping item with id {}".format(ent['id']))
                return
            trips = to_triplets(ent)
            for trip in trips:
                edge = self.triplet_to_edge(trip)
                if edge:
                    self.hg.add_belief(u'wikidata/gb', edge)
                    self.edges_found += 1
        except (KeyError, ValueError) as e:
            print('exception: %s' % e)

    def create_entities_table(self, filename):
        source_file = bz2.BZ2File(filename, 'r')
        count = 0
        for line in source_file:
            self.extract_entity(line)
            count += 1
            if (count % 10000) == 0:
                print('%s [%s]' % (count, self.entities_found))

        source_file.close()
        print('%s entities found' % self.entities_found)

    def flush_edge_buffer(self):
        self.hg.add_belief(u'wikidata/gb', self.edge_buffer)
        self.edge_buffer = []

    def create_edges(self, filename):
        source_file = bz2.BZ2File(filename, 'r')
        count = 0
        for line in source_file:
            self.extract_triplets(line)
            count += 1
            if (count % 10000) == 0:
                print('%s [%s]' % (count, self.edges_found))
        source_file.close()
        print('%s edges found' % self.edges_found)


def read(hg, filename):
    if not entity_table_exists():
        print('creating entity table...')
        WikidataReader(hg).create_entities_table(filename)
        print('done creating entity table.')
    else:
        print('entity table found, skipping entity table creation.')
    print('extracting edges...')
    WikidataReader(hg).create_edges(filename)
