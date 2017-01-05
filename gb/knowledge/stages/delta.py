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


from __future__ import print_function
from gb.knowledge.semantic_tree import Position


class DeltaStage(object):
    def __init__(self, tree):
        self.rel_pos = ['VERB', 'ADV', 'ADP', 'PART']
        self.tree = tree
        self.best_node = None
        self.best_fit = 0

    def is_relationship(self, entity_id):
        entity = self.tree.get(entity_id)
        if entity.is_node():
            for child_id in entity.children_ids:
                if not self.is_relationship(child_id):
                    return False
            return True
        else:
            return entity.token.pos in self.rel_pos

    def node_fit(self, node):
        fit = 0
        if node.is_node() and self.is_relationship(node.children_ids[0]):
            rel = self.tree.get(node.children_ids[0])
            if rel.arity() == 1 and (len(node.children_ids) == 2):
                fit += 1000
            elif rel.arity() == (len(node.children_ids) - 2):
                fit += 1000
        return fit

    def test_node(self, node):
        fit = self.node_fit(node)
        print('fit: %s' % fit)
        print('node; %s' % node)
        if fit >= self.best_fit:
            self.best_fit = fit
            self.best_node = node

    def find_best_node(self, original_node, node=None, to_process=None):
        if node is None:
            to_process = original_node.children_ids[1:]
            new_node = self.tree.create_node(original_node.children_ids[:1])
            self.find_best_node(original_node, new_node, to_process)
            return
        if len(to_process) > 0:
            child_id = to_process[0]
            child = self.tree.get(child_id)
            if child.is_node() and self.is_relationship(child.children_ids[0]):
                if len(node.children_ids) == 1:
                    rel = self.tree.create_node([node.children_ids[0]])
                    rel.children_ids.insert(0, child.children_ids[0])
                    new_node = node.clone()
                    new_node.children_ids[0] = rel.id
                    new_node.children_ids += child.children_ids[1:]
                    self.find_best_node(original_node, new_node, to_process[1:])
                elif len(node.children_ids) == 2 and len(child.children_ids) == 2:
                    rel_id = node.children_ids[0]
                    rel = self.tree.get(rel_id)
                    if not rel.is_terminal():
                        rel_id = rel.children_ids[-1]
                        rel = self.tree.get(rel_id)
                    if not rel.is_terminal():
                        rel = rel.clone()
                    else:
                        rel = self.tree.create_node([rel.id])
                    rel.add_child(child.children_ids[0])
                    rel.compound = True
                    new_node = node.clone()
                    new_node.children_ids[0] = rel.id
                    new_node.children_ids += child.children_ids[1:]
                    self.find_best_node(original_node, new_node, to_process[1:])

                new_node = node.clone()
                rel_id = node.children_ids[0]
                rel = self.tree.get(rel_id)
                if not rel.is_terminal():
                    rel = rel.clone()
                else:
                    rel = self.tree.create_node([rel.id])
                new_node.children_ids[0] = rel.id
                new_node.add_to_first_child(child.children_ids[0], Position.RIGHT)
                new_node.children_ids += child.children_ids[1:]
                self.find_best_node(original_node, new_node, to_process[1:])
                return
            else:
                new_node = node.clone()
                new_node.children_ids.append(child_id)
                self.find_best_node(original_node, new_node, to_process[1:])
                return
        else:
            if node == original_node:
                self.test_node(node)
            else:
                self.find_best_node(node)

    def process_entity(self, entity_id):
        entity = self.tree.get(entity_id)

        # process children first
        if entity.is_node():
            for i in range(len(entity.children_ids)):
                entity.children_ids[i] = self.process_entity(entity.children_ids[i])

        # print('process_entity; %s' % entity)

        if not entity.is_terminal() and self.is_relationship(entity.children_ids[0]):
            # print('xxx; %s' % entity)
            self.best_node = None
            self.best_fit = 0
            self.find_best_node(entity)
            new_node = self.best_node
            # print('fit: %s' % self.best_fit)
            # print('new_node; %s' % new_node)

            # flatten relationship
            if self.is_relationship(new_node.children_ids[0]):
                rel = self.tree.get(new_node.children_ids[0])
                if rel.is_node():
                    for child in rel.children():
                        if child.is_node():
                            child.compound = True
            return new_node.id

        return entity_id

    def process(self):
        self.tree.root_id = self.process_entity(self.tree.root_id)
        return self.tree


def transform(tree):
    return DeltaStage(tree).process()
