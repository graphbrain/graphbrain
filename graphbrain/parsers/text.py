from graphbrain import *


def edge_text(atom2word, edge):
    atoms = edge.all_atoms()
    words = list(atom2word[UniqueAtom(atom)]
                 for atom in atoms
                 if UniqueAtom(atom) in atom2word)
    words.sort(key=lambda word: word[1])
    return ' '.join([word[0] for word in words])
