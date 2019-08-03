from graphbrain import *
from graphbrain.meaning.concepts import *
from graphbrain.meaning.lemmas import deep_lemma
from graphbrain.meaning.corefs import main_coref
from graphbrain.agents.agent import Agent


CONFLICT_PRED_LEMMAS = {'warn', 'kill', 'accuse', 'condemn', 'slam', 'arrest',
                        'clash'}


class Conflicts(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        self.actors = None

    def name(self):
        return 'conflicts'

    def languages(self):
        return {'en'}

    def _is_actor(self, edge):
        if edge in self.actors:
            return True

        if self.hg.exists(('actor/p/.', edge)):
            self.actors.add(edge)
            return True
        else:
            return False

    def start(self):
        self.actors = set()

    def input_edge(self, edge):
        if not edge.is_atom():
            ct = edge.connector_type()
            if ct[:2] == 'pd':
                pred = edge[0]
                if (len(edge) > 3 and
                        deep_lemma(self.hg,
                                   pred).root() in CONFLICT_PRED_LEMMAS):
                    subjects = edge.edges_with_argrole('s')
                    objects = edge.edges_with_argrole('o')
                    if len(subjects) == 1 and len(objects) == 1:
                        subject = find_concept(subjects[0])
                        obj = find_concept(objects[0])
                        if (subject and obj and
                                is_proper_concept(subject) and
                                is_proper_concept(obj)):
                            actor_orig = main_coref(self.hg, subject)
                            actor_targ = main_coref(self.hg, obj)
                            if (self._is_actor(actor_orig) and
                                    self._is_actor(actor_targ)):
                                self.add(('conflict/p/.', actor_orig,
                                          actor_targ, edge))
