from graphbrain import *


class UniqueAtom(Atom):
    def __init__(self, atom):
        self.atom = atom

    def __hash__(self):
        return id(self.atom)

    def __eq__(self, other):
        return id(self.atom) == id(other.atom)


def edge_text(atom2word, edge):
    atoms = edge.all_atoms()
    words = list(atom2word[UniqueAtom(atom)]
                 for atom in atoms
                 if UniqueAtom(atom) in atom2word)
    words.sort(key=lambda word: word[1])
    return ' '.join([word[0] for word in words])
