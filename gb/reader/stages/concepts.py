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


from gb.nlp.nlp_token import Token
import gb.constants as cons
import gb.hypergraph.symbol as sym
import gb.reader.predicates as pred


class Concepts(object):
    def __init__(self, output):
        self.output = output

    def make_combinator_leaf(self):
        leaf = self.output.tree.create_leaf(Token(''))
        leaf.connector = True
        leaf.namespace = 'gb'
        return leaf

    def build_concept(self, connector_id, edge_id):
        edge = self.output.tree.get(edge_id)
        edge.children_ids.insert(0, connector_id)
        return edge_id

    def process_entity_inner(self, entity_id):
        entity = self.output.tree.get(entity_id)
        # process node
        if entity.is_node() and not entity.compound:
            # build concept
            if len(entity.children_ids) > 1:
                if not pred.is_predicate(entity.get_child(0), entity):
                    self.build_concept(self.make_combinator_leaf().id, entity.id)
                    return entity.id
        return entity_id

    def process_entity(self, entity_id):
        # process children first
        entity = self.output.tree.get(entity_id)
        if entity.is_node():
            for i in range(len(entity.children_ids)):
                entity.children_ids[i] = self.process_entity(entity.children_ids[i])

        return self.process_entity_inner(entity_id)

    def generate_labels(self, entity_id):
        entity = self.output.tree.get(entity_id)
        if entity.is_node():
            # process children first
            for i in range(len(entity.children_ids)):
                self.generate_labels(entity.children_ids[i])

            if entity.is_compound_concept():
                edge = entity.to_hyperedge()
                text = entity.as_text()
                label = sym.build(text, cons.label_namespace)
                syn_edge = [cons.has_label, edge, label]
                self.output.edges.append(syn_edge)

    def process(self):
        self.output.tree.root_id = self.process_entity(self.output.tree.root_id)
        self.generate_labels(self.output.tree.root_id)
        return self.output
