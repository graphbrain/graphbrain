from graphbrain import *
from graphbrain.meaning.concepts import *
from graphbrain.meaning.lemmas import deep_lemma
from graphbrain.meaning.corefs import main_coref
from graphbrain.meaning.actors import is_actor
from graphbrain.agents.agent import Agent


CONFLICT_PRED_LEMMAS = {'warn', 'kill', 'accuse', 'condemn', 'slam', 'arrest',
                        'clash'}

CONFLICT_TOPIC_TRIGGERS = {'of/t/en', 'over/t/en', 'against/t/en', 'for/t/en'}


class Conflicts(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        self.conflicts = 0
        self.conflict_topics = 0

    def name(self):
        return 'conflicts'

    def languages(self):
        return {'en'}

    def start(self):
        self.conflicts = 0
        self.conflict_topics = 0

    def _topics(self, actor_orig, actor_targ, edge):
        for item in edge[1:]:
            if item.type()[0] == 's':
                if item[0].to_str() in CONFLICT_TOPIC_TRIGGERS:
                    for concept in all_concepts(item[1]):
                        if self.hg.degree(concept) > 1:
                            self.add(('conflict-topic/p/.', actor_orig,
                                      actor_targ, concept, edge))
                            self.conflict_topics += 1

    def input_edge(self, edge):
        if not edge.is_atom():
            ct = edge.connector_type()
            if ct[:2] == 'pd':
                pred = edge[0]
                if (len(edge) > 2 and
                        deep_lemma(self.hg,
                                   pred).root() in CONFLICT_PRED_LEMMAS):
                    subjects = edge.edges_with_argrole('s')
                    objects = edge.edges_with_argrole('o')
                    if len(subjects) == 1 and len(objects) == 1:
                        subject = strip_concept(subjects[0])
                        obj = strip_concept(objects[0])
                        if (subject and obj and
                                has_proper_concept(subject) and
                                has_proper_concept(obj)):
                            actor_orig = main_coref(self.hg, subject)
                            actor_targ = main_coref(self.hg, obj)
                            if (is_actor(self.hg, actor_orig) and
                                    is_actor(self.hg, actor_targ)):
                                self.add(('conflict/p/.', actor_orig,
                                          actor_targ, edge))
                                self._topics(actor_orig, actor_targ, edge)
                                self.conflicts += 1

    def report(self):
        rep_str = ('conflict edges: {}\n'
                   'conflict-topic pairs: {}'.format(self.conflicts,
                                                     self.conflict_topics))
        return '{}\n\n{}'.format(rep_str, super().report())
