from graphbrain.meaning.actors import find_actors
from graphbrain.agents.agent import Agent
from graphbrain.op import create_op


class ClaimActors(Agent):
    def __init__(self):
        super().__init__()
        self.search_pattern = ('claim/P/.', '*', '*', '*')

    def name(self):
        return 'claim_actors'

    def input_edge(self, edge):
        hg = self.hg.get_hg(self)

        _, main_actor, claim, main_edge = edge
        actors = find_actors(hg, claim)
        for actor in actors:
            yield create_op(
                ('claim-actor/P/.', main_actor, actor, claim, main_edge))
