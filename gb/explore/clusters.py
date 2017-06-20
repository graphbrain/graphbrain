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


import operator
import numpy as np
from sklearn.cluster import DBSCAN
import gb.tools.json as json_tools
import gb.hypergraph.edge as ed
import gb.nlp.parser as par
from gb.explore.similarity import edge_similarity


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

    sorted_edges = sorted(extra_edges.items(), key=operator.itemgetter(1), reverse=False)
    print(sorted_edges)
    print(len(sorted_edges))

    print('creating distance matrix...')
    size = len(sorted_edges)
    dists = np.zeros((size, size))
    for i in range(size):
        ei = ed.str2edge(sorted_edges[i][0])
        for j in range(size):
            ej = ed.str2edge(sorted_edges[j][0])
            sim = edge_similarity(par, ei, ej, best=True)
            dists[i][j] = 1. - sim
    print('distance matrix created.')

    print('clustering...')
    dbscan = DBSCAN(eps=0.2, min_samples=2, metric='precomputed')
    dbscan.fit(dists)
    print('clustering done.')

    labels = dbscan.labels_

    # Number of clusters in labels, ignoring noise if present.
    print(labels)
    n_clusters_ = len(set(labels)) - (1 if -1 in labels else 0)
    print('Estimated number of clusters: %d' % n_clusters_)

    clusters = {}
    for i in range(len(labels)):
        label = labels[i]
        if label not in clusters:
            clusters[label] = []
        clusters[label].append(ed.edge2str(ed.str2edge(sorted_edges[i][0]), namespaces=False))
    print(clusters)
