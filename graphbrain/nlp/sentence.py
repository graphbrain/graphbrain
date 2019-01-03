import json
from asciitree import LeftAligned
from collections import OrderedDict
from graphbrain.nlp.nlp_token import token_from_dict


def node_label(token, prefix):
    return '[%s]%s' % (prefix, token)


def token2label_tree(token, prefix='*'):
    children = [token2label_tree(leaf, 'L') for leaf in token.left_children] +\
               [token2label_tree(leaf, 'R') for leaf in token.right_children]

    return node_label(token, prefix), OrderedDict(children)


def assign_depths(token, depth):
    token.depth = depth
    for leaf in token.left_children:
        assign_depths(leaf, depth + 1)
    for leaf in token.right_children:
        assign_depths(leaf, depth + 1)


class Sentence:
    def __init__(self, tokens=None, json_str=None):
        if tokens:
            self.tokens = tokens
            assign_depths(self.root(), 0)
        if json_str:
            self.from_json(json_str)

    def to_json(self):
        data = [token.to_dict() for token in self.tokens]
        return json.dumps(data)

    def from_json(self, json_str):
        token_dicts = json.loads(json_str)
        self.tokens = [token_from_dict(token_dict) for token_dict in token_dicts]
        for token in self.tokens:
            if token.parent >= 0:
                token.parent = self.tokens[token.parent]
            token.left_children = [self.tokens[i] for i in token.left_children]
            token.right_children = [self.tokens[i] for i in token.right_children]

    def root(self):
        for token in self.tokens:
            if token.dep == 'ROOT':
                return token
        return None

    def label_tree(self):
        r = self.root()
        if r is None:
            return {}
        label, children = token2label_tree(r)
        return {label: children}

    def print_tree(self):
        tr = LeftAligned()
        print(tr(self.label_tree()))

    def __str__(self):
        return ' '.join([token.word.strip() for token in self.tokens])
