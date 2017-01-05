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


class EpsilonStage(object):
    def __init__(self, tree):
        self.rel_pos = ['VERB', 'ADV', 'ADP', 'PART']
        self.tree = tree

    def is_relationship(self, entity_id):
        entity = self.tree.get(entity_id)
        if entity.is_node():
            for child_id in entity.children_ids:
                if not self.is_relationship(child_id):
                    return False
            return True
        else:
            return (entity.token.pos in self.rel_pos) and (entity.token.dep != 'advmod')

    def add_to_relationships(self, base_id, child_id, extra_params):
        base = self.tree.get(base_id)
        child = self.tree.get(child_id)
        rel_id = child.children_ids[0]
        rel = self.tree.get(rel_id)

        base_rel_id = base.children_ids[0]
        base_rel = self.tree.get(base_rel_id)

        extra_param_count = 0
        for param_id in extra_params:
            extra_param = self.tree.get(param_id)
            if extra_param.is_node()  and (len(extra_param.children_ids) > 2) and not self.is_relationship(extra_param.children_ids[0]):
                extra_param_count += 1

        arity = base_rel.arity()
        params = len(base.children_ids) + len(child.children_ids) + extra_param_count - 2

        if rel.is_node() and not rel.compound:
            for cid in rel.children_ids:
                self.add_to_relationships(base_id, cid, extra_params)
        else:
            pos = Position.RIGHT
            if len(base.children_ids) == 1:
                pos = Position.LEFT
            if params - arity == 1:
                base.add_to_first_child(rel_id, pos)
                first_child = self.tree.get(base.children_ids[0])
                first_child.compound = True
            else:
                base.add_to_first_child(rel_id, pos)

    def process_entity(self, entity_id):
        entity = self.tree.get(entity_id)

        # process children first
        if entity.is_node():
            for i in range(len(entity.children_ids)):
                entity.children_ids[i] = self.process_entity(entity.children_ids[i])

        if entity.is_node() and (len(entity.children_ids) > 2) and self.is_relationship(entity.children_ids[0]):
            to_process = entity.children_ids[1:]
            entity.children_ids = entity.children_ids[:1]
            while len(to_process) > 0:
                child_id = to_process.pop(0)
                child = self.tree.get(child_id)
                if (not child.has_dep('nsubj', shallow=True))\
                        and child.is_node()\
                        and self.is_relationship(child.children_ids[0]):
                    self.add_to_relationships(entity.id, child_id, to_process)
                    to_process = child.children_ids[1:] + to_process
                else:
                    entity.children_ids.append(child_id)

        return entity_id

    def process(self):
        self.tree.root_id = self.process_entity(self.tree.root_id)
        return self.tree


def transform(tree):
    return EpsilonStage(tree).process()
