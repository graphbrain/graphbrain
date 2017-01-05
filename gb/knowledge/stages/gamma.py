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
from gb.nlp.parser import Parser
from gb.nlp.sentence import Sentence
from gb.knowledge.semantic_tree import Position


class GammaStage(object):
    def __init__(self, tree):
        self.rel_pos = ['VERB', 'ADV', 'ADP', 'PART']
        self.tree = tree

    def is_relationship(self, entity_id, shallow=False, depth=0):
        entity = self.tree.get(entity_id)
        if entity.is_node():
            if shallow and depth > 0:
                return False
            for child_id in entity.children_ids:
                if not self.is_relationship(child_id, shallow, depth + 1):
                    return False
            return True
        else:
            return entity.token.pos in self.rel_pos

    def combine_relationships(self, first_id, second_id):
        first = self.tree.get(first_id)
        second = self.tree.get(second_id)
        first_child = self.tree.get(second.children_ids[0])
        if first_child.is_node() and (not first_child.compound):
            self.combine_relationships(first.id, first_child.id)
            return second.id

        second.add_to_first_child(first.id, Position.LEFT)
        first_child = self.tree.get(second.children_ids[0])
        first_child.compound = True
        return second.id

    def process_entity1(self, entity_id):
        entity = self.tree.get(entity_id)

        # create trivial compounds
        if entity.is_node() and self.is_relationship(entity.id, shallow=True):
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
                if self.is_relationship(first.id) and self.is_relationship(second.children_ids[0]):
                    return self.combine_relationships(first.id, second.id)

        return entity_id

    def process_entity2(self, entity_id):
        entity = self.tree.get(entity_id)

        if entity.is_node() and self.is_relationship(entity.children_ids[0]):
            to_process = entity.children_ids[1:]
            entity.children_ids = entity.children_ids[:1]
            while len(to_process) > 0:
                child_id = to_process.pop(0)
                child = self.tree.get(child_id)
                if child.is_node() and (len(child.children_ids) == 3) and self.is_relationship(child.children_ids[0]):
                    child_child_id = child.children_ids[1]
                    child_child = self.tree.get(child_child_id)
                    if child_child.is_node() and self.is_relationship(child_child.children_ids[0]):
                        child_child_rel = self.tree.get(child_child.children_ids[0])
                        if not child_child_rel.has_pos('VERB'):
                            entity.children_ids.append(child_child_id)
                            del child.children_ids[1]
                entity.children_ids.append(child_id)
                if child.is_node() and (len(child.children_ids) == 3) and self.is_relationship(child.children_ids[0]):
                    child_child_id = child.children_ids[2]
                    child_child = self.tree.get(child_child_id)
                    if child_child.is_node() and self.is_relationship(child_child.children_ids[0]):
                        child_child_rel = self.tree.get(child_child.children_ids[0])
                        if not child_child_rel.has_pos('VERB'):
                            entity.children_ids.append(child_child_id)
                            del child.children_ids[2]

        return entity_id

    def add_to_relationships(self, base_id, child_id, extra_param_ids):
        base = self.tree.get(base_id)
        child = self.tree.get(child_id)
        rel_id = child.children_ids[0]
        rel = self.tree.get(rel_id)

        extra_param_strs = [str(self.tree.get(param_id)) for param_id in extra_param_ids]
        print('base: %s; child: %s; extra: %s' % (base, child, extra_param_strs))

        base_rel_id = base.children_ids[0]
        base_rel = self.tree.get(base_rel_id)

        extra_param_count = 0
        for param_id in extra_param_ids:
            extra_param = self.tree.get(param_id)
            if extra_param.is_node()\
                    and (len(extra_param.children_ids) > 2)\
                    and not self.is_relationship(extra_param.children_ids[0]):
                extra_param_count += 1

        #
        extra_param_count = len(extra_param_ids)

        arity = base_rel.arity()
        params = len(base.children_ids) + len(child.children_ids) + extra_param_count - 2
        print('arity: %s; params: %s' % (arity, params))

        if rel.is_node() and not rel.compound and (params - arity == 1):
            new_rel = self.tree.create_node(rel.children_ids)
            last_rel_child = self.tree.get(new_rel.children_ids[-1])
            last_rel_child.add_child(base_rel_id)
            last_rel_child = self.tree.get(new_rel.children_ids[-1])
            if params - arity == 1:
                last_rel_child.compound = True
            base.children_ids[0] = new_rel.id
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
        # process children first
        entity = self.tree.get(entity_id)
        if entity.is_node():
            for i in range(len(entity.children_ids)):
                entity.children_ids[i] = self.process_entity(entity.children_ids[i])

        eid = entity_id
        eid = self.process_entity1(eid)
        # print('1. %s' % self.tree.get(eid))
        #eid = self.process_entity2(eid)
        # print('2. %s' % self.tree.get(eid))
        return eid

    def process(self):
        self.tree.root_id = self.process_entity(self.tree.root_id)
        return self.tree


def transform(tree):
    return GammaStage(tree).process()


if __name__ == '__main__':
    test_text = """
    My name is James Bond.
    """

    print('Starting parser...')
    parser = Parser()
    print('Parsing...')
    result = parser.parse_text(test_text)

    print(result)

    for r in result:
        s = Sentence(r)
        print(s)
        s.print_tree()
        t = transform(s)
        print(t)
