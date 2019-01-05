import sys
import csv
import time
from graphbrain.funs import *
from graphbrain.parsers.stagewise_parser import StagewiseParser


class SemBubbleReader(object):
    def __init__(self, hg):
        self.hg = hg
        self.extractor = StagewiseParser(hg, stages=('alpha-forest', 'beta-naive', 'gamma', 'delta', 'epsilon'))
        self.main_edges = 0
        self.extra_edges = 0
        self.ignored = 0
        self.time_acc = 0
        self.items_processed = 0
        self.first_item = True

    def process_text(self, text, author):
        start_t = time.time()
        parses = self.extractor.read_text(text.lower(), aux_text=None, reset_context=True)
        for p in parses:
            print('\n')
            print('sentence: %s' % p[0])
            print(edge2str(p[1].main_edge))
            if len(p[1].main_edge) < 8:
                self.hg.add_belief(author, p[1].main_edge)
                self.main_edges += 1
                for edge in p[1].edges:
                    self.hg.add_belief('gb', edge)
                    self.extra_edges += 1
                self.hg.set_attribute(p[1].main_edge, 'text', text)
            else:
                self.ignored += 1

        if self.first_item:
            self.first_item = False
        else:
            delta_t = time.time() - start_t
            self.time_acc += delta_t
            self.items_processed += 1
            items_per_min = float(self.items_processed) / float(self.time_acc)
            items_per_min *= 60.
            print('total items: %s' % self.items_processed)
            print('items per minute: %s' % items_per_min)

    def process_post(self, post):
        web_entity = build_symbol(post['web_entity'], 'web_entity')
        print('web_entity: %s' % web_entity)

        text = post['text'].strip()
        if len(text) == 0:
            return
        if text[-1].isalnum():
            text += '.'
        self.process_text(text, web_entity)

    def read_file(self, filename):
        # self.extractor.debug = True

        csv.field_size_limit(sys.maxsize)
        with open(filename, 'r') as csvfile:
            first = True
            for row in csv.reader(csvfile, delimiter=',', quotechar='"'):
                if first:
                    first = False
                else:
                    post = {'id': row[0],
                            'url': row[1],
                            'web_entity_id': row[2],
                            'web_entity': row[3],
                            'text': row[4]}
                    self.process_post(post)

        print('main edges created: %s' % self.main_edges)
        print('extra edges created: %s' % self.extra_edges)
        print('ignored edges: %s' % self.ignored)


if __name__ == '__main__':
    from graphbrain.hypergraph import HyperGraph
    hgr = HyperGraph({'backend': 'leveldb', 'hg': 'card_and_id_fraud.hg'})
    SemBubbleReader(hgr).read_file('Card_and_ID_fraud.csv')
