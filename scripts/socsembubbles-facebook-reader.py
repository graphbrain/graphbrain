# Reader of Facebook data for SocSemBubbles project


import json
import time
from graphbrain.funs import *
from graphbrain.reader.reader import Reader


class FacebookReader(object):
    def __init__(self, hg):
        self.hg = hg
        self.extractor = Reader(hg, stages=('alpha-forest', 'beta-naive', 'gamma', 'delta', 'epsilon'))
        self.main_edges = 0
        self.extra_edges = 0
        self.ignored = 0
        self.time_acc = 0
        self.items_processed = 0
        self.first_item = True

    def process_text(self, parent, author, text):
        start_t = time.time()
        parses = self.extractor.read_text(text.lower(), aux_text=None, reset_context=True)
        for p in parses:
            print('\n')
            print('sentence: %s' % p[0])
            print(edge2str(p[1].main_edge))
            if len(p[1].main_edge) < 8:
                self.hg.add_belief(author, p[1].main_edge)
                self.hg.add(('parent/gb', p[1].main_edge, parent))
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

    def process_comment(self, parent, author, message):
        print('author: %s' % author)

        text = message.strip()
        if len(text) == 0:
            return
        if text[-1].isalnum():
            text += '.'
        self.process_text(parent, author, text)

    def read_file(self, filename):
        # self.extractor.debug = True

        with open(filename, 'r') as f:
            for entry in f:
                entry = json.loads(entry)
                parent = entry['begin']['from']
                for comment in entry['comments']:
                    author = comment['from']
                    message = comment['message']
                    self.process_comment(parent, author, message)

        print('main edges created: %s' % self.main_edges)
        print('extra edges created: %s' % self.extra_edges)
        print('ignored edges: %s' % self.ignored)


if __name__ == '__main__':
    from graphbrain.hypergraph import HyperGraph
    hgr = HyperGraph({'backend': 'leveldb', 'hg': 'facebook.hg'})
    FacebookReader(hgr).read_file('statuses.json')
