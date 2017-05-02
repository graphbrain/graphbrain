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
import gb.hypergraph.edge as ed
import gb.reader.stages.common as co


def flatten_concept(edge):
    children_ids = []
    for child in edge.children():
        if child.is_leaf() or child.compound:
            children_ids.append(child.id)
        else:
            for cid in child.children_ids[1:]:
                children_ids.append(cid)
    edge.children_ids = children_ids
    return edge


class EpsilonStage(object):
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
                if co.is_concept(entity):
                    self.build_concept(self.make_combinator_leaf().id, entity.id)
                    return entity.id
            first = entity.get_child(0)
            # make connector
            if first.is_leaf() and (not co.is_relationship(first)):
                first.connector = True
            if len(entity.children_ids) == 2:
                if co.is_relationship(first) and (not co.is_relationship(entity)):
                    first.connector = True
                second = entity.get_child(1)
                if second.is_node():
                    # (connector (+ a b)) -> (connector a b)
                    if first.is_leaf()\
                            and not co.is_qualifier(first)\
                            and first.is_connector()\
                            and first.token.word != '':
                        second_rel = second.children()[0]
                        if second_rel.is_leaf() and second_rel.is_connector() and second_rel.token.word == '':
                            second.children_ids[0] = first.id
                            return second.id

        return entity_id

    def process_entity(self, entity_id):
        # process children first
        entity = self.output.tree.get(entity_id)
        if entity.is_node():
            for i in range(len(entity.children_ids)):
                entity.children_ids[i] = self.process_entity(entity.children_ids[i])

        eid = entity_id
        eid = self.process_entity_inner(eid)
        return eid

    def generate_synonyms(self, entity_id):
        # process children first
        entity = self.output.tree.get(entity_id)
        if entity.is_node():
            for i in range(len(entity.children_ids)):
                self.generate_synonyms(entity.children_ids[i])

        entity = self.output.tree.get(entity_id)
        if entity.is_node() and entity.children()[0].is_connector():
            edge = entity.to_hyperedge()
            text = entity.as_text()
            ns = 'gb%s' % sym.hashed(ed.edge2str(edge))
            symbol = sym.build(text, ns)
            syn_edge = [cons.are_synonyms, edge, symbol]
            self.output.edges.append(syn_edge)

    def process(self):
        self.output.tree.root_id = self.process_entity(self.output.tree.root_id)
        self.generate_synonyms(self.output.tree.root_id)
        return self.output
