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


# http://universaldependencies.org/u/pos/
POS_TAGS = {
    'ADJ':      'adjective',
    'ADP':      'adposition',
    'ADV':      'adverb',
    'AUX':      'auxiliary',
    'CONJ':     'conjunction',
    'CCONJ':    'coordinating conjunction',
    'DET':      'determiner',
    'INTJ':     'interjection',
    'NOUN':     'noun',
    'NUM':      'numeral',
    'PART':     'particle',
    'PRON':     'pronoun',
    'PROPN':    'proper noun',
    'PUNCT':    'punctuation',
    'SCONJ':    'subordinating conjunction',
    'SYM':      'symbol',
    'VERB':     'verb',
    'X':        'other',
    'EOL':      'end of line',
    'SPACE':    'space'
}


# https://github.com/clir/clearnlp-guidelines/blob/master/md/specifications/dependency_labels.md
DEPENDENCY_LABELS = {
    'acomp':        'adjectival complement',
    'advcl':        'adverbial clause modifier',
    'advmod':       'adverbial modifier',
    'agent':        'agent',
    'amod':         'adjectival modifier',
    'appos':        'appositional modifier',
    'attr':         'attribute',
    'aux':          'auxiliary',
    'auxpass':      'auxiliary (passive)',
    'cc':           'coordinating conjunction',
    'ccomp':        'clausal complement',
    'complm':       'complementizer',
    'conj':         'conjunct',
    'cop':          'copula',
    'csubj':        'clausal subject',
    'csubjpass':    'clausal subject (passive)',
    'dep':          'unclassified dependent',
    'det':          'determiner',
    'dobj':         'direct object',
    'expl':         'expletive',
    'hmod':         'modifier in hyphenation',
    'hyph':         'hyphen',
    'infmod':       'infinitival modifier',
    'intj':         'interjection',
    'iobj':         'indirect object',
    'mark':         'marker',
    'meta':         'meta modifier',
    'neg':          'negation modifier',
    'nmod':         'modifier of nominal',
    'nn':           'noun compound modifier',
    'npadvmod':     'noun phrase as adverbial modifier',
    'nsubj':        'nominal subject',
    'nsubjpass':    'nominal subject (passive)',
    'num':          'number modifier',
    'number':       'number compound modifier',
    'oprd':         'object predicate',
    'obj':          'object',
    'obl':          'oblique nominal',
    'parataxis':    'parataxis',
    'partmod':      'participal modifier',
    'pcomp':        'complement of preposition',
    'pobj':         'object of preposition',
    'poss':         'possession modifier',
    'possessive':   'possessive modifier',
    'preconj':      'pre-correlative conjunction',
    'prep':         'prepositional modifier',
    'prt':          'particle',
    'punct':        'punctuation',
    'quantmod':     'modifier of quantifier',
    'rcmod':        'relative clause modifier',
    'root':         'root',
    'xcomp':        'open clausal complement'
}
