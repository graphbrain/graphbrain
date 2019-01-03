from graphbrain.funs import *
import graphbrain.constants as const


class DisambNaive(object):
    def __init__(self, output):
        self.output = output

    def process_entity(self, entity_id):
        entity = self.output.tree.get(entity_id)

        entity.generate_namespace()

        if entity.is_leaf():
            if entity.token.word.lower() != entity.token.lemma.lower():
                lemma_ent = build_symbol(entity.token.lemma.lower(), entity.namespace)
                self.output.edges.append((const.have_same_lemma, entity.to_hyperedge(), lemma_ent))
        else:
            for child_id in entity.children_ids:
                self.process_entity(child_id)

    def process(self):
        self.process_entity(self.output.tree.root_id)
        return self.output
