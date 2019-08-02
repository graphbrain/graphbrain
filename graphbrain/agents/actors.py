from collections import Counter
from graphbrain import *
from graphbrain.meaning.corefs import main_coref
from graphbrain.agents.agent import Agent


def get_concept(edge):
    if edge.type()[0] == 'c':
        return edge
    elif not edge.is_atom():
        return get_concept(edge[1])
    else:
        return None


def is_proper(concept):
    if concept.is_atom():
        return concept.type()[:2] == 'cp'
    else:
        for edge in concept[1:]:
            if is_proper(edge):
                return True
        return False


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
                    subject = get_concept(subjects[0])
                    if subject and is_proper(subject):
                        actor = main_coref(self.hg, subject)
                        self.actor_counter[actor] += 1

    def end(self):
        for actor in self.actor_counter:
            if self.actor_counter[actor] > 1:
                self.add(('actor/p/.', actor))
