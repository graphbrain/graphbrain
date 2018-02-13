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


import gb.hypergraph.symbol as sym


def conflict_targets(hg, actors, actor):
    targets = set()
    for edge in hg.pattern2edges(('conflict/gb.inf', actor, None, None)):
        target = edge[2]
        if target in actors:
            targets.add(target)
    return targets


def json_str(hg, symbol):
    labels = {symbol: hg.get_label(symbol)}

    actors = set()

    conflict_map = {}
    for edge in hg.pattern2edges(('conflict/gb.inf', symbol, None, None)):
        targ = edge[2]
        if not sym.is_edge(targ):
            actors.add(targ)
            labels[targ] = hg.get_label(targ)
            if targ not in conflict_map:
                conflict_map[targ] = {'topics': set()}
            topic = edge[3]
            labels[topic] = hg.get_label(topic)
            conflict_map[targ]['topics'].add(topic)
    for edge in hg.pattern2edges(('conflict/gb.inf', None, symbol, None)):
        targ = edge[1]
        if not sym.is_edge(targ):
            actors.add(targ)
            labels[targ] = hg.get_label(targ)
            if targ not in conflict_map:
                conflict_map[targ] = {'topics': set()}
            topic = edge[3]
            labels[topic] = hg.get_label(topic)
            conflict_map[targ]['topics'].add(topic)

    conflict = [{'target': targ, 'topics': tuple(conflict_map[targ]['topics'])} for targ in conflict_map]

    nodes = []
    actor_id = {}
    i = 0
    for actor in actors:
        actor_id[actor] = i
        nodes.append({'label': labels[actor], 'r': 3})
        i += 1
    links = []
    for actor in actors:
        targets = conflict_targets(hg, actors, actor)
        for target in targets:
            links.append({'source': actor_id[actor], 'target': actor_id[target]})
            nodes[actor_id[actor]]['r'] += 1
            nodes[actor_id[target]]['r'] += 1

    data = {'entity': symbol,
            'labels': labels,
            'conflict': conflict,
            'conflict_graph': {'nodes': nodes, 'links': links}}

    return data
