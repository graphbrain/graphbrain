from graphbrain.meaning.corefs import make_corefs_ops
from graphbrain.meaning.concepts import has_proper_concept
from graphbrain.agents.agent import Agent


class CorefsDets(Agent):
    def __init__(self):
        super().__init__()
        self.corefs = 0

    def name(self):
        return 'corefs_dets'

    def languages(self):
        return {'en'}

    def on_start(self):
        self.corefs = 0

    def input_edge(self, edge):
        hg = self.system.get_hg(self)

        if (not edge.is_atom() and
                len(edge) == 2 and
                edge[0].is_atom() and
                edge[0].root() == 'the' and
                has_proper_concept(edge[1])):
            self.corefs += 1
            for op in make_corefs_ops(hg, edge, edge[1]):
                yield op

    def report(self):
        return '{} coreferences were added.'.format(str(self.corefs))
