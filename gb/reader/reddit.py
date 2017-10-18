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


import time
import json
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
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
        self.extractor = Reader(hg, stages=('alpha-forest', 'beta-naive', 'gamma', 'delta', 'epsilon'))
        self.main_edges = 0
        self.extra_edges = 0
        self.ignored = 0
        self.time_acc = 0
        self.items_processed = 0
        self.first_item = True

    def process_text(self, text, author, reset_context=False, aux_text=None):
        start_t = time.time()
        parses = self.extractor.read_text(text.lower(), aux_text, reset_context=reset_context)
        for p in parses:
            print('\n')
            print('sentence: %s' % p[0])
            print(ed.edge2str(p[1].main_edge))
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
            author = sym.build(post['author'], 'reddit_user')
            self.process_text(post['body'], author, reset_context=False)
        if 'comments' in post:
            for comment in post['comments']:
                if comment:
                    self.process_comments(comment)

    def process_post(self, post):
        author = sym.build(post['author'], 'reddit_user')
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
    from gb.hypergraph.hypergraph import HyperGraph
    hgr = HyperGraph({'backend': 'leveldb', 'hg': 'wikidata.hg'})
    RedditReader(hgr, comments=False).read_file('reddit-wordlnews-27032017-28032017.json')
