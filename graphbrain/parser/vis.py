from collections import OrderedDict
from asciitree import LeftAligned
from termcolor import colored


def token2str(token):
    word = colored(token.lower_.strip(), 'cyan')
    lemma = token.lemma_.strip()
    tag = colored(token.tag_, 'green')
    dep = colored(token.dep_, 'yellow')
    named_entity = token.ent_type_

    if named_entity != '':
        named_entity = colored('{%s}' % named_entity, 'magenta')

    return '%s/%s/%s (%s) %s' % (word, lemma, tag, dep, named_entity)


def token2label_tree(token, prefix='*'):
    children = [token2label_tree(leaf, '<') for leaf in token.lefts] +\
               [token2label_tree(leaf, '>') for leaf in token.rights]

    label = '%s %s' % (colored(prefix, 'red'), token2str(token))
    return label, OrderedDict(children)


def print_tree(token):
    label, children = token2label_tree(token)
    tr = LeftAligned()
    print(tr({label: children}))
