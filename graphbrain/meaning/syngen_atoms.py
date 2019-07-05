from unidecode import unidecode
from graphbrain import *
from graphbrain.meaning.synonyms import make_synonyms


def generate(hg):
    count = 0
    print('processing atoms')
    for atom in hg.all_atoms():
        label = root(atom)
        label = label.replace('_', '')
        label = unidecode(label)
        if len(label) > 0 and root(atom) != label and label[0].isalpha():
            parts = (label,) + tuple(atom_parts(atom)[1:])
            syn_atom = '/'.join(parts)
            if hg.exists(syn_atom):
                make_synonyms(hg, atom, syn_atom)
                count += 1
    return count
