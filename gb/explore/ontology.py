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


from gb.hypergraph.hypergraph import HyperGraph
import gb.hypergraph.symbol as sym
import gb.knowledge.synonyms as syn


def is_concept(edge):
    rel = edge[0]
    if sym.is_edge(rel):
        return False
    return rel[0] == '+'


def down(hg, symbol):
    synonyms = syn.synonyms(hg, symbol)
    edges = hg.star(symbol)
    edges = [edge for edge in edges if is_concept(edge)]
    return {'symbol': symbol,
            'synonyms': [down(hg, synonym) for synonym in synonyms],
            'derived_symbols': [down(hg, edge) for edge in edges]}


if __name__ == '__main__':
    params = {'backend': 'leveldb',
              'hg': 'test.hg'}
    hg = HyperGraph(params)
    print(down(hg, 'xpto'))
