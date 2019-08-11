from unidecode import unidecode
from graphbrain import *
from graphbrain.meaning.corefs import make_corefs
from graphbrain.agents.agent import Agent


def unidecode_edge(edge):
    if edge.is_atom():
        atom = edge
        label = unidecode(atom.root().replace('_', ''))
        if label == atom.root() or len(label) == 0 or not label[0].isalpha():
            return edge
        else:
            parts = (label,) + tuple(atom.parts()[1:])
            return hedge('/'.join(parts))
    else:
        return hedge(tuple(unidecode_edge(item) for item in edge))


class CorefsUnidecode(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        self.corefs = 0

    def name(self):
        return 'corefs_unidecode'

    def languages(self):
        return set()

    def start(self):
        self.corefs = 0

    def input_edge(self, edge):
        uedge = unidecode_edge(edge)
        if uedge != edge and self.hg.exists(uedge):
            make_corefs(self.hg, edge, uedge)
            self.corefs += 1

    def report(self):
        return '{} coreferences were added.'.format(str(self.corefs))
