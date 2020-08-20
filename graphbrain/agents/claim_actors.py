from graphbrain.meaning.actors import find_actors
from graphbrain.agents.agent import Agent
from graphbrain.agents.system import wrap_edge


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
            yield wrap_edge(
                ('claim-actor/P/.', main_actor, actor, claim, main_edge))
