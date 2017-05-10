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


def down(hg, symbol, visited=None):
    if not visited:
        visited = set()
    if sym.symbol2str(symbol) in visited:
        return None
    # print(symbol)
    visited.add(sym.symbol2str(symbol))
    synonyms = [synonym for synonym in syn.synonyms(hg, symbol)]
    edges = [s for s in hg.star(symbol)]
    edges = [edge for edge in edges if is_concept(edge)]
    return {'symbol': symbol,
            'synonyms': [down(hg, synonym, visited) for synonym in synonyms],
            'derived_symbols': [down(hg, edge, visited) for edge in edges]}


def derived_symbols(hg, ont, symbols=None, depth=0):
    if not symbols:
        symbols = {}
    symbol = ont['symbol']
    degree = syn.degree(hg, symbol)
    symbols[sym.symbol2str(symbol)] = {'degree': degree, 'depth': depth}
    for subont in ont['derived_symbols']:
        derived_symbols(hg, subont, symbols, depth + 1)
    return symbols


if __name__ == '__main__':
    params = {'backend': 'leveldb',
              'hg': 'reddit-worldnews-01012017-28032017.hg'}
    hyper = HyperGraph(params)
    onto = down(hyper, 'israel/wdQ801')
    ds = derived_symbols(hyper, onto)
    # print(ds)
    for s in ds:
        print('%s %s' % (s, ds[s]['degree']))
