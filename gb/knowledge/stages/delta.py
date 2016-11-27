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


class DeltaStage(object):
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
            return entity.token.pos in self.rel_pos

    def process_entity(self, entity_id):
        entity = self.tree.get(entity_id)

        # process children first
        if entity.is_node():
            for i in range(len(entity.children_ids)):
                entity.children_ids[i] = self.process_entity(entity.children_ids[i])

        if entity.is_node() and self.is_relationship(entity.children_ids[0]):
            to_process = entity.children_ids[1:]
            entity.children_ids = entity.children_ids[:1]
            while len(to_process) > 0:
                child_id = to_process.pop(0)
                child = self.tree.get(child_id)
                if child.is_node() and (len(child.children_ids) == 3) and self.is_relationship(child.children_ids[0]):
                    child_child_id = child.children_ids[1]
                    child_child = self.tree.get(child_child_id)
                    if self.is_relationship(child_child.children_ids[0]):
                        child_child_rel = self.tree.get(child_child.children_ids[0])
                        if not child_child_rel.has_pos('VERB'):
                            entity.children_ids.append(child_child_id)
                            del child.children_ids[1]
                entity.children_ids.append(child_id)
                if child.is_node() and (len(child.children_ids) == 3) and self.is_relationship(child.children_ids[0]):
                    child_child_id = child.children_ids[2]
                    child_child = self.tree.get(child_child_id)
                    if self.is_relationship(child_child.children_ids[0]):
                        child_child_rel = self.tree.get(child_child.children_ids[0])
                        if not child_child_rel.has_pos('VERB'):
                            entity.children_ids.append(child_child_id)
                            del child.children_ids[2]

        return entity_id

    def process(self):
        self.tree.root_id = self.process_entity(self.tree.root_id)
        return self.tree


def transform(tree):
    return DeltaStage(tree).process()
