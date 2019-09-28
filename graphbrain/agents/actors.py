from collections import Counter
from graphbrain.meaning.concepts import *
from graphbrain.meaning.corefs import main_coref
from graphbrain.agents.agent import Agent


class Actors(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        self.actor_counter = None

    def name(self):
        return 'actors'

    def languages(self):
        return set()

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
                        actor = main_coref(self.hg, subject)
                        self.actor_counter[actor] += 1

    def end(self):
        for actor in self.actor_counter:
            if self.actor_counter[actor] > 1:
                self.add(('actor/p/.', actor))
