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


from gb.reader.reader import Reader
import gb.hypergraph.symbol as sym


class Shell(object):
    def __init__(self, hg):
        self.hg = hg
        self.extractor = None

    def get_extractor(self):
        if self.extractor is None:
            self.extractor = Reader(None)
        return self.extractor

    def command_parse(self, params):
        extractor = self.get_extractor()
        sentence = ' '.join(params)
        result = extractor.read_text(sentence)
        print(result)

    def command_search(self, params):
        term = ' '.join(params)
        root = sym.str2symbol(term)
        symbols = self.hg.symbols_with_root(root)
        [print(str(symbol)) for symbol in symbols]

    def command_star(self, params):
        edges = self.hg.star(params[0])
        [print(str(edge)) for edge in edges]

    def eval(self, line):
        tokens = line.split()
        if len(tokens) > 0:
            command = tokens[0]
            params = tokens[1:]
            if command == 'exit':
                return True
            elif command == 'parse':
                self.command_parse(params)
            elif command == 'search':
                self.command_search(params)
            elif command == 'star':
                self.command_star(params)
            else:
                print("error: uknown command: '%s'." % command)

        return False

    def run(self):
        print('Welcome to the GraphBrain shell.')
        print()

        done = False
        while not done:
            print('> ', end='')
            line = input()
            done = self.eval(line)
