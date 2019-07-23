import progressbar
from unidecode import unidecode
from graphbrain import *
from graphbrain.meaning.corefs import make_corefs


def generate(hg):
    count = 0
    print('processing atoms')
    atom_count = hg.atom_count()
    i = 0
    with progressbar.ProgressBar(max_value=atom_count) as bar:
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
            if i < atom_count:
                i += 1
                bar.update(i)
    return count
