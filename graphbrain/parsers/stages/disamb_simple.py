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


class DisambSimple(object):
    def __init__(self, output):
        self.output = output
        self.compound_deps = ['pobj', 'compound', 'dobj', 'nsubj']

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

    def process_entity(self, entity_id):
        entity = self.output.tree.get(entity_id)

        entity.namespace = '?'

        if entity.is_node():
            entity.compound = self.is_compound(entity)
            for child_id in entity.children_ids:
                self.process_entity(child_id)

    def process(self):
        self.process_entity(self.output.tree.root_id)
        return self.output
