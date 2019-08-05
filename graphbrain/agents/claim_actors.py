from graphbrain.meaning.actors import find_actors
from graphbrain.agents.agent import Agent


class ClaimActors(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        self.search_pattern = ('claim/p/.', '*', '*', '*')

    def name(self):
        return 'claim_actors'

    def languages(self):
        return set()

    def input_edge(self, edge):
        _, main_actor, claim, main_edge = edge
        actors = find_actors(self.hg, claim)
        for actor in actors:
            self.add(('claim-actor/p/.', main_actor, actor, claim, main_edge))
