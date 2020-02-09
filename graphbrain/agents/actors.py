from collections import Counter
from graphbrain.meaning.concepts import *
from graphbrain.meaning.lemmas import deep_lemma
from graphbrain.meaning.corefs import main_coref
from graphbrain.agents.agent import Agent


ACTOR_PRED_LEMMAS = {'say', 'claim', 'warn', 'kill', 'accuse', 'condemn',
                     'slam', 'arrest', 'clash', 'blame', 'want', 'call',
                     'tell'}


class Actors(Agent):
    def __init__(self, hg, lang, sequence=None):
        super().__init__(hg, lang, sequence)
        self.actor_counter = None

    def name(self):
        return 'actors'

    def languages(self):
        return {'en'}

    def start(self):
        self.actor_counter = Counter()

    def input_edge(self, edge):
        if not edge.is_atom():
            ct = edge.connector_type()
            if ct[:2] == 'pd':
                subjects = edge.edges_with_argrole('s')
                if len(subjects) == 1:
                    subject = strip_concept(subjects[0])
                    if subject and has_proper_concept(subject):
                        pred = edge[0]
                        dlemma = deep_lemma(self.hg, pred).root()
                        if dlemma in ACTOR_PRED_LEMMAS:
                            try:
                                actor = main_coref(self.hg, subject)
                                self.actor_counter[actor] += 1
                            except Exception as e:
                                print(str(e))

    def end(self):
        for actor in self.actor_counter:
            if self.actor_counter[actor] > 0:
                self.add(('actor/p/.', actor))
