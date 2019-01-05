from graphbrain.parsers.semantic_tree import Position
from graphbrain.parsers.predicates import Predicates


class Merge(object):
    def __init__(self, output, lang):
        self.output = output
        self.pred = Predicates(lang=lang)

    def process_entity(self, entity_id):
        changes = False

        # process children first
        entity = self.output.tree.get(entity_id)
        if entity.is_node():
            for i in range(len(entity.children_ids)):
                if self.process_entity(entity.children_ids[i]):
                    changes = True

        # combine relationships
        entity = self.output.tree.get(entity_id)

        if entity.is_node() and len(entity.children_ids) > 1:
            first_head = entity.first_child()

            second = entity.get_child(1)
            if second.is_node():
                second_parent = second
            else:
                second_parent = entity

            second_head = second.first_child()
            if not entity.is_compound()\
                    and self.pred.is_predicate(first_head, entity)\
                    and self.pred.is_predicate(second_head, second_parent):
                pos = Position.RIGHT
                first_head.insert(second_head.id, pos)
                entity = self.output.tree.get(entity_id)
                first = entity.first_child()
                first.compound = True
                entity_children_ids = entity.children_ids[2:]
                if entity.get_child(1).is_node():
                    second_children_ids = entity.get_child(1).children_ids[1:]
                else:
                    second_children_ids = []
                new_children_ids = [first.id] + second_children_ids + entity_children_ids
                entity.children_ids = new_children_ids
                changes = True

        return changes

    def process(self):
        while self.process_entity(self.output.tree.root_id):
            pass
        return self.output
