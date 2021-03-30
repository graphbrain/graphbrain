import logging

from graphbrain import hedge
from graphbrain.cognition.agent import Agent
from graphbrain.meaning.concepts import all_concepts
from graphbrain.meaning.concepts import has_proper_concept
from graphbrain.meaning.concepts import strip_concept
from graphbrain.meaning.corefs import main_coref
from graphbrain.meaning.lemmas import deep_lemma
from graphbrain.op import create_op


CONFLICT_PRED_LEMMAS = {'warn', 'kill', 'accuse', 'condemn', 'slam', 'arrest',
                        'clash', 'blame'}

CONFLICT_TOPIC_TRIGGERS = {'of/T/en', 'over/T/en', 'against/T/en', 'for/T/en'}


class Conflicts(Agent):
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)
        self.conflicts = 0
        self.conflict_topics = 0

    def languages(self):
        return {'en'}

    def on_start(self):
        self.conflicts = 0
        self.conflict_topics = 0

    def _topics(self, hg, actor_orig, actor_targ, edge):
        for item in edge[1:]:
            if item.type()[0] == 'S':
                if item[0].to_str() in CONFLICT_TOPIC_TRIGGERS:
                    for concept in all_concepts(item[1]):
                        if hg.degree(concept) > 1:
                            yield create_op(('conflict-topic/P/.', actor_orig,
                                             actor_targ, concept, edge))
                            self.conflict_topics += 1

    def process_edge(self, edge, depth):
        hg = self.system.get_hg(self)

        if not edge.is_atom():
            ct = edge.connector_type()
            if ct[0] == 'P':
                pred = edge[0]
                if (len(edge) > 2 and
                        deep_lemma(
                            hg,
                            pred,
                            same_if_none=True).root() in CONFLICT_PRED_LEMMAS):
                    subjects = edge.edges_with_argrole('s')
                    objects = edge.edges_with_argrole('o')
                    if len(subjects) == 1 and len(objects) == 1:
                        subject = strip_concept(subjects[0])
                        obj = strip_concept(objects[0])
                        if (subject and obj and
                                has_proper_concept(subject) and
                                has_proper_concept(obj)):
                            actor_orig = main_coref(hg, subject)
                            actor_targ = main_coref(hg, obj)
                            conflict_edge = hedge(
                                ('conflict/P/.', actor_orig, actor_targ, edge))
                            yield create_op(conflict_edge)
                            for wedge in self._topics(
                                    hg, actor_orig, actor_targ, edge):
                                yield wedge
                            self.conflicts += 1

    def report(self):
        return 'conflict edges: {}; conflict-topic pairs: {}'.format(
            self.conflicts, self.conflict_topics)
