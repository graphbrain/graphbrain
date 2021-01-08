import logging

from graphbrain.cognition.agent import Agent
from graphbrain.meaning.actors import find_actors
from graphbrain.op import create_op


class ClaimActors(Agent):
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)
        self.search_pattern = ('claim/P/.', '*', '*', '*')

    def process_edge(self, edge, depth):
        hg = self.hg.get_hg(self)

        _, main_actor, claim, main_edge = edge
        actors = find_actors(hg, claim)
        for actor in actors:
            yield create_op(
                ('claim-actor/P/.', main_actor, actor, claim, main_edge))
