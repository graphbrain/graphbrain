from unidecode import unidecode
from graphbrain import *
from graphbrain.meaning.corefs import make_corefs
from graphbrain.agents.agent import Agent


class CorefsAtoms(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        self.corefs = 0

    def name(self):
        return 'corefs_atoms'

    def languages(self):
        return set()

    def start(self):
        self.corefs = 0

    def input_edge(self, edge):
        if edge.is_atom():
            atom = edge
            label = atom.root()
            label = label.replace('_', '')
            label = unidecode(label)
            if len(label) > 0 and atom.root() != label and label[0].isalpha():
                parts = (label,) + tuple(atom.parts()[1:])
                coref_atom = hedge('/'.join(parts))
                if self.hg.exists(coref_atom):
                    make_corefs(self.hg, atom, coref_atom)
                    self.corefs += 1

    def report(self):
        return '{} coreferences were added.'.format(str(self.corefs))
