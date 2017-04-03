#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


class Token:
    """Generic NLP token."""

    def __init__(self, word=None):
        self.word = word
        self.depth = -1
        self.lemma = None
        self.shape = None
        self.logprob = None
        self.pos = None
        self.dep = None
        self.parent = None
        self.left_children = None
        self.right_children = None
        self.entity_type = None
        self.separator = False

    def to_list(self):
        tokens = []
        for token in self.left_children:
            tokens += token.to_list()
        tokens += [self]
        for token in self.right_children:
            tokens += token.to_list()
        return tokens

    def is_word(self):
        if self.separator:
            return False
        if self.pos == 'PUNCT':
            return False
        if self.pos == 'SPACE':
            return False
        return True

    def to_word_token_list(self):
        tokens = self.to_list()
        return [token for token in tokens if token.is_word()]

    def chunk_str(self):
        words = [token.word for token in self.to_word_token_list()]
        return ' '.join(words)

    def __eq__(self, other):
        if isinstance(other, Token):
            return (self.word == other.word)\
                   and (self.pos == other.pos)\
                   and (self.dep == other.dep)
        return NotImplemented

    def __str__(self):
        return '%s/%s (%s) {%s} [%s]' % (self.word.strip(), self.pos, self.dep, self.entity_type, self.depth)

    def __repr__(self):
        return self.__str__()
