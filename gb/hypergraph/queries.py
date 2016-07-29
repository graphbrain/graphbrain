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


def exclude_edge(hg, edge):
    for vertex in edge[1:]:
        if hg.degree(vertex) > 250:
            return True
    return False


def include_edge(hg, edge):
    return not exclude_edge(hg, edge)


def add_neighbours(neighbours, edge):
    return set(neighbours).union(set(edge[1:]))


def vemap_assoc_id_edge(vemap, vid, edge):
    if vid not in vemap:
        vemap[vid] = {'edges': [], 'neighbours': set()}
    vemap[vid]['edges'].append(edge)
    vemap[vid]['neighbours'] = add_neighbours(vemap[id]['neighbours'], edge)
    return vemap


def vemap_plus_edge(vemap, edge):
    for vid in edge:
        vemap_assoc_id_edge(vemap, vid, edge)
    return vemap


def edges2vemap(vemap, edges):
    for edge in edges:
        vemap_plus_edge(vemap, edge)
    return vemap


def node2edges(hg, node):
    edges = hg.ego(node, 2)
    edges = [edge for edge in edges if include_edge(hg, edge)]
    return edges


def interedge(edge, interverts):
    for vertex in edge[1:]:
        if vertex not in interverts:
            return False
    return True


def walks(vemap, nodes, walk, all_walks):
    valid = True
    for node in nodes:
        if node not in walk:
            valid = False

    if valid:
        all_walks.add(walk)
    else:
        neighbours = vemap[walk[-1]]['neighbours']
        next_steps = [node for node in neighbours if node not in walk]
        if len(next_steps) > 0:
            for step in next_steps:
                new_walk = walk[:]
                new_walk.append(step)
                wks = set()
                walks(vemap, nodes, new_walk, wks)
                for w in wks:
                    all_walks.add(w)


def walk_step2wmap(wmap, walk_length, step):
    if (step not in wmap) or (walk_length < wmap[step]):
        wmap[step] = walk_length


def walk2wmap(wmap, walk):
    walk_length = len(walk)
    for step in walk:
        walk_step2wmap(wmap, walk_length, step)


def walks2wmap(wks):
    wmap = {}
    for walk in wks:
        walk2wmap(wmap, walk)
    return wmap


def valid_step(wmap, nodes, walk_length, step):
    if step in nodes:
        return True
    if walk_length == wmap[step]:
        return True
    return False


def valid_walk(wmap, nodes, walk):
    walk_length = len(walk)
    for step in walk:
        if not valid_step(wmap, nodes, walk_length, step):
            return False
    return True


def intersect(hg, seeds):
    edgesets = [node2edges(hg, seed) for seed in seeds]
    vemap = {}
    for edges in edgesets:
        edges2vemap(vemap, edges)
    wlks = set()
    walks(vemap, seeds, [seeds[0]], wlks)
    wmap = walks2wmap(wlks)
    wlks = [walk for walk in wlks if valid_walk(wmap, seeds, walk)]
    interverts = set([item for sublist in wlks for item in sublist])
    edges = [vemap[vid]['edges'] for vid in vemap]
    edges = [item for sublist in edges for item in sublist]
    edges = [e for e in edges if interedge(e, interverts)]
    return edges
