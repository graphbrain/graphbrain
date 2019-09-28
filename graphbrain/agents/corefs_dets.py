from graphbrain.meaning.corefs import make_corefs
from graphbrain.meaning.concepts import has_proper_concept
from graphbrain.agents.agent import Agent


class CorefsDets(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        self.corefs = 0

    def name(self):
        return 'corefs_dets'

    def languages(self):
        return {'en'}

    def start(self):
        self.corefs = 0

    def input_edge(self, edge):
        if (not edge.is_atom() and
                len(edge) == 2 and
                edge[0].is_atom() and
                edge[0].root() == 'the' and
                has_proper_concept(edge[1])):
            make_corefs(self.hg, edge, edge[1])
            self.corefs += 1

    def report(self):
        return '{} coreferences were added.'.format(str(self.corefs))
