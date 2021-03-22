import logging
from collections import Counter

from graphbrain.cognition.agent import Agent
from graphbrain.meaning.concepts import has_proper_concept
from graphbrain.meaning.concepts import strip_concept
from graphbrain.meaning.corefs import main_coref
from graphbrain.meaning.lemmas import deep_lemma
from graphbrain.op import create_op


ACTOR_PRED_LEMMAS = {'say', 'claim', 'warn', 'kill', 'accuse', 'condemn',
                     'slam', 'arrest', 'clash', 'blame', 'want', 'call',
                     'tell'}


class Actors(Agent):
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)
        self.actor_counter = None

    def languages(self):
        return {'en'}

    def on_start(self):
        self.actor_counter = Counter()

    def process_edge(self, edge, depth):
        hg = self.system.get_hg(self)

        if not edge.is_atom():
            ct = edge.connector_type()
            if ct[0] == 'P':
                subjects = edge.edges_with_argrole('s')
                if len(subjects) == 1:
                    subject = strip_concept(subjects[0])
                    if subject and has_proper_concept(subject):
                        pred = edge[0]
                        dlemma = deep_lemma(hg, pred).root()
                        if dlemma in ACTOR_PRED_LEMMAS:
                            try:
                                actor = main_coref(hg, subject)
                                self.actor_counter[actor] += 1
                            except Exception as e:
                                print(str(e))

    def on_end(self):
        for actor in self.actor_counter:
            yield create_op(('actor/P/.', actor))

    def report(self):
        return 'actors found: {}'.format(
            len(self.actor_counter))
