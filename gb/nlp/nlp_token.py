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


def token_from_dict(token_dict):
    token = Token()
    token.word = token_dict['word']
    token.depth = token_dict['edge_depth']
    token.lemma = token_dict['lemma']
    token.shape = token_dict['shape']
    token.logprob = token_dict['logprob']
    token.pos = token_dict['pos']
    token.dep = token_dict['dep']
    token.tag = token_dict['tag']
    token.parent = token_dict['parent']
    token.left_children = token_dict['left_children']
    token.right_children = token_dict['right_children']
    token.entity_type = token_dict['entity_type']
    token.separator = token_dict['separator']
    token.position_in_sentence = token_dict['position_in_sentence']
    return token


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
        self.tag = None
        self.parent = None
        self.left_children = None
        self.right_children = None
        self.entity_type = None
        self.separator = False
        self.position_in_sentence = -1
        self.vector = None

    def to_dict(self):
        token_dict = {
            'word': self.word,
            'edge_depth': self.depth,
            'lemma': self.lemma,
            'shape': self.shape,
            'logprob': self.logprob,
            'pos': self.pos,
            'dep': self.dep,
            'tag': self.tag,
            'parent': -1,
            'left_children': [child.position_in_sentence for child in self.left_children],
            'right_children': [child.position_in_sentence for child in self.right_children],
            'entity_type': self.entity_type,
            'separator': self.separator,
            'position_in_sentence': self.position_in_sentence
            # 'vector': self.vector
        }
        if self.parent:
            token_dict['parent'] = self.parent.position_in_sentence
        return token_dict

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

    def __hash__(self):
        return hash((self.word, self.pos, self.dep, self.position_in_sentence))

    def __eq__(self, other):
        if isinstance(other, Token):
            return (self.word, self.pos, self.dep, self.position_in_sentence)\
                   == (other.word, other.pos, other.dep, other.position_in_sentence)
        return NotImplemented

    def __ne__(self, other):
        if isinstance(other, Token):
            return not (self == other)
        return NotImplemented

    def __str__(self):
        return '%s/%s/%s/%s (%s) {%s} [%s]'\
               % (self.word.strip(), self.lemma, self.pos, self.tag, self.dep, self.entity_type,
                  self.position_in_sentence)

    def __repr__(self):
        return self.__str__()
