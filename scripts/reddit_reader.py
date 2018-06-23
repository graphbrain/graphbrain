import time
import json
import argparse
from gb.hypergraph import HyperGraph
from gb.funs import *
from gb.reader.reader import Reader


def comments_to_text(comments):
    chunks = []
    for comment in comments:
        if comment:
            if 'body' in comment:
                chunks.append(comment['body'])
            if 'comments' in comment:
                chunks.append(comments_to_text(comment['comments']))
    return '\n'.join(chunks)


def generate_aux_text(post):
    text = ''
    if 'comments' in post:
        text = '%s\n%s' % (text, comments_to_text(post['comments']))
    return text


class RedditReader(object):
    def __init__(self, hg, comments):
        self.hg = hg
        self.comments = comments
        self.reader = Reader(hg)
        self.main_edges = 0
        self.extra_edges = 0
        self.ignored = 0
        self.time_acc = 0
        self.items_processed = 0
        self.first_item = True

    def process_text(self, text, author, reset_context=False, aux_text=None):
        start_t = time.time()
        parses = self.reader.read_text(text, aux_text, reset_context=reset_context)
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

    def process_comments(self, post):
        if 'body' in post:
            author = build_symbol(post['author'], 'reddit_user')
            self.process_text(post['body'], author, reset_context=False)
        if 'comments' in post:
            for comment in post['comments']:
                if comment:
                    self.process_comments(comment)

    def process_post(self, post):
        author = build_symbol(post['author'], 'reddit_user')
        print('author: %s' % author)

        # aux_text = generate_aux_text(post)

        text = post['title'].strip()
        if text[-1].isalnum():
            text += '.'
        self.process_text(text, author, reset_context=True, aux_text='')
        if self.comments:
            self.process_comments(post)

    def read_file(self, filename):
        # self.extractor.debug = True

        if self.comments:
            print('Including comments.')
        else:
            print('Not including comments.')

        with open(filename, 'r') as f:
            for line in f:
                post = json.loads(line)
                self.process_post(post)

        print('main edges created: %s' % self.main_edges)
        print('extra edges created: %s' % self.extra_edges)
        print('ignored edges: %s' % self.ignored)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--backend', type=str, help='hypergraph backend (leveldb, null)', default='leveldb')
    parser.add_argument('--hg', type=str, help='hypergraph name', default='gb.hg')
    parser.add_argument('--infile', type=str, help='input file', default=None)
    parser.add_argument('--comments', help='include comments', action='store_true')

    args = parser.parse_args()

    params = {
        'backend': args.backend,
        'hg': args.hg,
        'infile': args.infile,
        'comments': args.comments
    }

    hgraph = HyperGraph(params)
    infile = params['infile']
    read_comments = params['comments']
    RedditReader(hgraph, comments=read_comments).read_file(infile)
