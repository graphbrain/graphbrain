from unidecode import unidecode
from graphbrain import hedge
from graphbrain.meaning.corefs import make_corefs_ops
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
    def __init__(self):
        super().__init__()
        self.corefs = 0

    def name(self):
        return 'corefs_unidecode'

    def on_start(self):
        self.corefs = 0

    def input_edge(self, edge):
        hg = self.system.get_hg(self)

        uedge = unidecode_edge(edge)
        if uedge != edge and hg.exists(uedge):
            self.corefs += 1
            for op in make_corefs_ops(hg, edge, uedge):
                yield op

    def report(self):
        return '{} coreferences were added.'.format(str(self.corefs))
