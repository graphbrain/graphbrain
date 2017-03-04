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


import gb.constants as const


def synonyms(hg, symbol):
    syns1 = [edge[2] for edge in hg.pattern2edges((const.are_synonyms, symbol, None))]
    syns2 = [edge[1] for edge in hg.pattern2edges((const.are_synonyms, None, symbol))]
    syns3 = [edge[2] for edge in hg.pattern2edges((const.have_same_lemma, symbol, None))]
    syns4 = [edge[1] for edge in hg.pattern2edges((const.have_same_lemma, None, symbol))]
    return set([symbol]).union(syns1).union(syns2).union(syns3).union(syns4)


def degree(hg, symbol):
    syns = synonyms(hg, symbol)
    total_degree = 0
    for syn in syns:
        total_degree += hg.degree(syn)
    return total_degree
