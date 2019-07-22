from unidecode import unidecode
from graphbrain import *
from graphbrain.meaning.corefs import make_corefs


def generate(hg):
    count = 0
    print('processing atoms')
    for atom in hg.all_atoms():
        label = atom.root()
        label = label.replace('_', '')
        label = unidecode(label)
        if len(label) > 0 and atom.root() != label and label[0].isalpha():
            parts = (label,) + tuple(atom.parts()[1:])
            coref_atom = hedge('/'.join(parts))
            if hg.exists(coref_atom):
                make_corefs(hg, atom, coref_atom)
                count += 1
    return count
