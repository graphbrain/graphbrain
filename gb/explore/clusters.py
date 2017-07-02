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


import math
import operator
import numpy as np
import igraph
from sklearn.cluster import DBSCAN, KMeans
import gb.tools.json as json_tools
import gb.hypergraph.edge as ed
import gb.nlp.parser as par
import gb.explore.similarity as simil


def build_graph(vertices, edges):
    vert_bimap = {}
    for i in range(len(vertices)):
        vert_str = ed.edge2str(vertices[i])
        vert_bimap[i] = vert_str
        vert_bimap[vert_str] = i

    _edges = [(vert_bimap[ed.edge2str(edge[0])], vert_bimap[ed.edge2str(edge[1])]) for edge in edges]

    g = igraph.Graph()
    g.add_vertices(len(vertices))
    g.add_edges(_edges)
    return g, vert_bimap


def find_communities(vertices, edges):
    g, vert_bimap = build_graph(vertices, edges)

    comms = igraph.Graph.community_multilevel(g, return_levels=False)
    memb = comms.membership
    mod = g.modularity(memb)

    communities = {}
    for i in range(len(memb)):
        comm = memb[i]
        if comm not in communities:
            communities[comm] = []
        communities[comm].append(vert_bimap[i])

    return communities, mod


if __name__ == '__main__':
    print('creating parser...')
    par = par.Parser()
    print('parser created.')

    edge_data = json_tools.read('edges_similar_concepts.json')

    extra_edges = {}
    for item in edge_data:
        edge = ed.str2edge(item['edge'])
        matched = [ed.str2edge(match[1]) for match in item['matches']]
        for part in edge[1:]:
            if part not in matched:
                key = ed.edge2str(part)
                if key in extra_edges:
                    extra_edges[key] += 1
                else:
                    extra_edges[key] = 1

    # sorted_edges = sorted(extra_edges.items(), key=operator.itemgetter(1), reverse=False)
    # print(sorted_edges)
    # print(len(sorted_edges))

    edges = [ed.str2edge(edge_str) for edge_str in extra_edges.keys()]
    edges = [edge for edge in edges if simil.edge_min_prob(par, edge) < -8]

    print('creating distance matrix...')
    size = len(edges)
    print(size)
    dists = np.zeros((size, size))
    graph_edges = []
    for i in range(size):
        for j in range(size):
            sim = simil.edge_x_similarity(par, edges[i], edges[j])
            dist = 1.
            if sim < 1.:
                dist = 1. / abs(math.log(sim))
            dists[i][j] = dist
            if dist < 1. and edges[i] != edges[j]:
                print('%s ||| %s ==>> %s' % (edges[i], edges[j], dist))
                graph_edges.append((edges[i], edges[j]))
    print('distance matrix created.')

    comms, modul = find_communities(edges, graph_edges)
    for comm in comms:
        print('COMMUNITY: %s' % comm)
        for item in comms[comm]:
            print(item)
    print('number of communities: %s' % len(comms))

    exit(0)

    print('clustering...')
    # kmeans = KMeans(n_clusters=10)
    # kmeans.fit(dists)
    # labels = kmeans.labels_
    dbscan = DBSCAN(eps=0.1, min_samples=3, metric='precomputed')
    dbscan.fit(dists)
    labels = dbscan.labels_
    print('clustering done.')

    # Number of clusters in labels, ignoring noise if present.
    print(labels)
    n_clusters_ = len(set(labels)) - (1 if -1 in labels else 0)

    clusters = {}
    for i in range(len(labels)):
        label = labels[i]
        if label not in clusters:
            clusters[label] = []
        clusters[label].append(ed.edge2str(edges[i], namespaces=False))

    for cluster in clusters:
        print('CLUSTER %s' % cluster)
        for edge in clusters[cluster]:
            print(edge)

    print('Estimated number of clusters: %d' % n_clusters_)
