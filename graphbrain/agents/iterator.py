from graphbrain.agents.agent import Agent


class Iterator(Agent):
    def name(self):
        return 'iterator'

    def input_edge(self, edge):
        return edge
