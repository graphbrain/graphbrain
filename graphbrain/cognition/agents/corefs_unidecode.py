import logging

from unidecode import unidecode

from graphbrain.hyperedge import hedge
from graphbrain.cognition.agent import Agent
from graphbrain.meaning.corefs import make_corefs_ops


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
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)
        self.corefs = 0

    def on_start(self):
        self.corefs = 0

    def process_edge(self, edge, depth):
        hg = self.system.get_hg(self)

        uedge = unidecode_edge(edge)
        if uedge != edge and hg.exists(uedge):
            self.corefs += 1
            for op in make_corefs_ops(hg, edge, uedge):
                yield op

    def report(self):
        return '{} coreferences were added.'.format(str(self.corefs))
