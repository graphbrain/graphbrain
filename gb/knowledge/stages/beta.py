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
import gb.disambiguation.disambiguate as disamb


def is_compound_by_entity_type(node):
    first_child = node.get_child(0)
    if first_child.is_node():
        return False
    entity_type = first_child.token.entity_type
    if (entity_type is None) or (len(entity_type) == 0):
        return False
    for child in node.children():
        if child.is_node():
            return False
        if child.token.entity_type != entity_type:
            return False
    return True


def element_to_bag_of_words(element, bow):
    bow.add(element.as_text())
    if element.is_node():
        for child in element.children():
            element_to_bag_of_words(child, bow)


def trees_to_bag_of_words(trees):
    bow = set()
    for tree in trees:
        element_to_bag_of_words(tree.root(), bow)
    return bow


class BetaStage(object):
    def __init__(self, hg, output, trees):
        self.hg = hg
        self.output = output
        self.compound_deps = ['pobj', 'compound', 'dobj', 'nsubj']
        self.bag_of_words = trees_to_bag_of_words(trees)

    def is_compound_by_deps(self, node):
        for child in node.children():
            if child.is_leaf():
                if child.token.dep not in self.compound_deps:
                    return False
            else:
                if not self.is_compound_by_deps(child):
                    return False
        return True

    def is_compound(self, node):
        return is_compound_by_entity_type(node) or self.is_compound_by_deps(node)

    def process_entity(self, entity_id, exclude):
        entity = self.output.tree.get(entity_id)
        roots = {sym.str2symbol(entity.as_text())}
        if entity.is_leaf():
            roots.add(entity.token.lemma)
        disamb_ent, prob = disamb.disambiguate(self.hg, roots, self.bag_of_words, exclude)

        # print('>>> %s %s %s' % (entity.as_text(), disamb_ent, prob))

        exclude = exclude[:]
        exclude.append(entity.as_text())

        make_entity = True
        if entity.is_node():
            for child_id in entity.children_ids:
                p = self.process_entity(child_id, exclude)
                if p < prob:
                    make_entity = False
                    prob = p

        if make_entity:
            if disamb_ent is None:
                entity.namespace = sym.random_hash()
            else:
                entity.namespace = sym.nspace(disamb_ent)
            if entity.is_node():
                entity.compound = True
        elif entity.is_node():
            if self.is_compound(entity):
                entity.compound = True

        return prob

    def process(self):
        self.process_entity(self.output.tree.root_id, [])
        return self.output
