import time
import logging
from graphbrain.funs import *
import graphbrain.constants as const
from graphbrain.sense.candidate_metrics import CandidateMetrics
from graphbrain.parsers.predicates import Predicates


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


def element_to_bag_of_ngrams(element, bon):
    bon.add(element.as_text())
    bon.add(element.as_text(True))
    if element.is_node():
        for child in element.children():
            element_to_bag_of_ngrams(child, bon)


def trees_to_bag_of_ngrams(trees):
    bon = set()
    for tree in trees:
        element_to_bag_of_ngrams(tree.root(), bon)
    return bon


class Disamb(object):
    def __init__(self, hg, parser, disamb, output, aux_text, lang):
        self.hg = hg
        self.parser = parser
        self.disamb = disamb
        self.output = output
        self.compound_deps = ['pobj', 'compound', 'dobj', 'nsubj']
        self.aux_text = aux_text
        self.profiling = {}
        self.pred = Predicates(lang=lang)

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

    def force_wordnet(self, entity):
        if self.pred.is_predicate(entity):
            return True
        if entity.is_leaf():
            if entity.token.pos == 'PART' and entity.token.dep == 'case':
                return True
        return False

    def process_entity(self, entity_id, exclude):
        start = time.time()
        entity = self.output.tree.get(entity_id)

        # profiling
        prof_key = entity.as_text()
        self.profiling[prof_key] = {}
        self.profiling[prof_key]['candidates'] = 0
        self.profiling[prof_key]['words1'] = 0
        self.profiling[prof_key]['words2'] = 0

        roots = {str2symbol(entity.as_text())}
        if entity.is_leaf():
            roots.add(str2symbol(entity.token.lemma))
        else:
            words = entity.as_label_list()
            lemmas = entity.as_label_list(lemmas=True)
            lemma_at_end = ' '.join(words[:-1] + [lemmas[-1]])
            roots.add(str2symbol(lemma_at_end))
        namespaces = None
        if self.force_wordnet(entity):
            namespaces = ('wn.', 'lem.wn.')

        if entity.is_leaf() and entity.token.pos in {'ADP', 'CONJ'}:
            disamb_ent = None
            metrics = CandidateMetrics()
        else:
            disamb_ent, metrics = self.disamb.best_sense(roots, self.aux_text, namespaces)
            # profiling
            self.profiling[prof_key]['candidates'] = self.disamb.candidates
            self.profiling[prof_key]['words1'] = self.disamb.words1
            self.profiling[prof_key]['words2'] = self.disamb.words2

        logging.info('[disamb] text: %s; entity: %s; metrics: %s' % (entity.as_text(), disamb_ent, metrics))

        exclude = exclude[:]
        exclude.append(entity.as_text())

        make_entity = True
        if entity.is_node():
            for child_id in entity.children_ids:
                m = self.process_entity(child_id, exclude)
                if m.better_than(metrics):
                    make_entity = False
                    metrics = m

        if make_entity:
            if disamb_ent is None:
                entity.generate_namespace()
            else:
                if entity.as_text() == symbol_root(disamb_ent):
                    entity.namespace = symbol_namespace(disamb_ent)
                # entity with shared lemma
                else:
                    entity.namespace = '%s.%s' % (const.lemma_derived_namespace, symbol_namespace(disamb_ent))
                    # additional edge for shared lemma
                    self.output.edges.append((const.have_same_lemma, entity.to_hyperedge(), disamb_ent))
            if entity.is_node():
                entity.compound = True
        elif entity.is_node():
            if self.is_compound(entity):
                entity.compound = True

        # profiling
        self.profiling[prof_key]['time'] = time.time() - start

        return metrics

    def process(self):
        self.profiling = {}
        self.process_entity(self.output.tree.root_id, [])

        for entity_id in self.profiling.keys():
            print('%s => %s' % (entity_id, self.profiling[entity_id]))

        print(self.disamb.profile_string())

        return self.output
