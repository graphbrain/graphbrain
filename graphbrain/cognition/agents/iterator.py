import logging

from graphbrain.cognition.agent import Agent
from graphbrain.op import create_op


class Iterator(Agent):
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)
        self.recursive = False

    def process_edge(self, edge, depth):
        yield create_op(edge)
