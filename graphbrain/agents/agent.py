from graphbrain import *


class Agent(object):
    def __init__(self, hg):
        self.hg = hg

    def name(self):
        raise NotImplementedError()

    def languages(self):
        raise NotImplementedError()

    def input_edge(self, edge):
        raise NotImplementedError()

    def input_pattern(self, pattern):
        for edge in self.hg.search(pattern):
            vars = match_pattern(edge, pattern)
            for var in vars:
                self.input_edge(vars[var])
