from graphbrain.agents.agent import Agent
from graphbrain.op import create_op


class Iterator(Agent):
    def name(self):
        return 'iterator'

    def input_edge(self, edge):
        yield create_op(edge)
