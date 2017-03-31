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
from gb.reader.extractor import Extractor


class RedditReader(object):
    def __init__(self, hg):
        self.hg = hg
        self.extractor = Extractor(hg)

    def process_post(self, post):
        print(post['title'])
        parses = self.extractor.read_text(post['title'])
        for p in parses:
            print(p[1].main_edge)

    def read_file(self, filename):
        with open(filename, 'r') as f:
            for line in f:
                post = json.loads(line)
                self.process_post(post)


if __name__ == '__main__':
    from gb.hypergraph.hypergraph import HyperGraph
    hg = HyperGraph({'backend': 'leveldb', 'hg': 'wikidata.hg'})
    RedditReader(hg).read_file('reddit-wordlnews-27032017-28032017.json')
