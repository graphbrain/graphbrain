import time
import json
import argparse
from graphbrain import *
from graphbrain.parser import *


def comments_to_text(comments):
    chunks = []
    for comment in comments:
        if comment:
            if 'body' in comment:
                chunks.append(comment['body'])
            if 'comments' in comment:
                chunks.append(comments_to_text(comment['comments']))
    return '\n'.join(chunks)


class RedditReader(object):
    def __init__(self, hg, comments):
        self.hg = hg
        self.comments = comments
        self.parser = Parser(lang='en')
        self.main_edges = 0
        self.extra_edges = 0
        self.time_acc = 0
        self.items_processed = 0
        self.first_item = True

    def process_text(self, text, author):
        start_t = time.time()
        parses = self.parser.parse(text)
        for parse, sent in parses:
            print('\n')
            print('sentence: {}'.format(sent))
            print(ent2str(parse))
            self.hg.add_belief(author, parse)
            self.main_edges += 1
            # for edge in p[1].edges:
            #     self.hg.add_belief('gb', edge)
            #     self.extra_edges += 1
            # self.hg.set_attribute(p[1].main_edge, 'text', text)

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
            self.process_text(post['body'], author)
        if 'comments' in post:
            for comment in post['comments']:
                if comment:
                    self.process_comments(comment)

    def process_post(self, post):
        author = build_symbol(post['author'], 'reddit_user')
        print('author: %s' % author)

        text = post['title'].strip()
        if text[-1].isalnum():
            text += '.'
        self.process_text(text, author)
        if self.comments:
            self.process_comments(post)

    def read_file(self, filename):
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


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('--backend', type=str,
                        help='hypergraph backend (leveldb, null)',
                        default='leveldb')
    parser.add_argument('--hg', type=str, help='hypergraph name',
                        default='gb.hg')
    parser.add_argument('--infile', type=str, help='input file', default=None)
    parser.add_argument('--comments', help='include comments',
                        action='store_true')

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
