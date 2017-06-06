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


REL_POS = ['VERB', 'ADV', 'PART']
CONCEPT_POS = ['NOUN', 'PROPN']
QUALIFIER_POS = ['ADJ']


class Transformation(object):
    IGNORE, APPLY, NEST, SHALLOW, DEEP, APPLY_R, APPLY_L, NEST_R, NEST_L, DEEP_R, DEEP_L = range(11)


def is_adp_token_relationship(token):
    if not token.parent:
        return False
    if token.parent.pos in REL_POS:
        return True
    if token.left_children:
        for child in token.left_children:
            if child.pos in REL_POS:
                return True
    if token.right_children:
        for child in token.right_children:
            if child.pos in REL_POS:
                return True
    if token.parent.pos == 'ADP':
        return is_adp_token_relationship(token.parent)


def is_adj_token_relationship(token):
    if not token.parent:
        return False
    if token.parent.pos in REL_POS:
        return True
    if token.left_children:
        for child in token.left_children:
            if child.pos in REL_POS:
                return True
    if token.right_children:
        for child in token.right_children:
            if child.pos in REL_POS:
                return True
    if token.parent.pos == 'ADJ':
        return is_adj_token_relationship(token.parent)


def is_relationship(entity, shallow=False, depth=0):
    if entity.is_node():
        if shallow and depth > 0:
            return False
        for child in entity.children():
            if not is_relationship(child, shallow, depth + 1):
                return False
        return True
    else:
        if entity.token.pos == 'ADP':
            return is_adp_token_relationship(entity.token)

        if entity.token.pos == 'ADJ':
            return is_adj_token_relationship(entity.token)

        return (entity.token.pos in REL_POS)\
            and (entity.token.dep != 'conj')\
            and (entity.token.dep != 'amod')\
            and (entity.token.dep != 'case')


def is_qualifier(entity):
    if entity.is_node():
        return False
    return entity.token.pos in QUALIFIER_POS


def is_concept(entity):
    if entity.is_node():
        for child in entity.children():
            if not is_concept(child) and not is_qualifier(child):
                return False
        return True
    return entity.token.pos in CONCEPT_POS
