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


import gb.reader.stages.common as co


class GammaStage(object):
    def __init__(self, ouput):
        self.ouput = ouput

    def combine_relationships(self, first_id, second_id):
        first = self.ouput.tree.get(first_id)
        second = self.ouput.tree.get(second_id)
        first_child = self.ouput.tree.get(second.children_ids[0])
        if first_child.is_node() and (not first_child.compound):
            self.combine_relationships(first.id, first_child.id)
            return second.id

        second.add_to_first_child(first.id, first.position)
        first_child = self.ouput.tree.get(second.children_ids[0])
        first_child.compound = True
        return second.id

    def process_entity_inner(self, entity_id):
        entity = self.ouput.tree.get(entity_id)

        # create trivial compounds
        if entity.is_node() and co.is_relationship(entity, shallow=True):
            entity.compound = True
            return entity_id

        # process node
        if entity.is_node() and (len(entity.children_ids) == 2):
            first = entity.get_child(0)
            second = entity.get_child(1)
            if first.is_leaf():
                # remove
                if (first.token.pos == 'DET') and (first.token.lemma in ('the', 'an', 'a')):
                    return second.id
            # combine relationships
            if second.is_node() and not second.compound:
                if co.is_relationship(first) and co.is_relationship(second.children()[0]):
                    return self.combine_relationships(first.id, second.id)

        return entity_id

    def process_entity(self, entity_id):
        # process children first
        entity = self.ouput.tree.get(entity_id)
        if entity.is_node():
            for i in range(len(entity.children_ids)):
                entity.children_ids[i] = self.process_entity(entity.children_ids[i])

        eid = entity_id
        eid = self.process_entity_inner(eid)
        return eid

    def process(self):
        self.ouput.tree.root_id = self.process_entity(self.ouput.tree.root_id)
        return self.ouput
