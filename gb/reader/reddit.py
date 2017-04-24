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


import json
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
from gb.reader.extractor import Extractor


class RedditReader(object):
    def __init__(self, hg):
        self.hg = hg
        self.extractor = Extractor(hg)
        self.main_edges = 0
        self.extra_edges = 0

    def process_post(self, post):
        parses = self.extractor.read_text(post['title'])
        author = sym.build([post['author'], 'reddit_user'])
        print('author: %s' % author)
        for p in parses:
            print('\n')
            print('sentence: %s' % p[0])
            print(ed.edge2str(p[1].main_edge))
            self.hg.add_belief(author, p[1].main_edge)
            self.main_edges += 1
            print('== extra ==')
            for edge in p[1].edges:
                print(ed.edge2str(edge))
                self.hg.add_belief('gb', edge)
                self.extra_edges += 1

    def read_file(self, filename):
        with open(filename, 'r') as f:
            for line in f:
                post = json.loads(line)
                self.process_post(post)

        print('main edges created: %s' % self.main_edges)
        print('extra edges created: %s' % self.extra_edges)


if __name__ == '__main__':
    from gb.hypergraph.hypergraph import HyperGraph
    hgr = HyperGraph({'backend': 'leveldb', 'hg': 'wikidata.hg'})
    RedditReader(hgr).read_file('reddit-wordlnews-27032017-28032017.json')
