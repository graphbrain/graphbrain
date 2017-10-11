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
NON_REL_DEPS = ['conj', 'amod', 'case']
AUX_REL_POS = ['ADP', 'ADJ']

CONCEPT_POS = ['NOUN', 'PROPN']
QUALIFIER_POS = ['ADJ']


class Transformation(object):
    IGNORE, APPLY, NEST, SHALLOW, DEEP, FIRST, APPLY_R, APPLY_L, NEST_R, NEST_L, DEEP_R, DEEP_L = range(12)


def is_related_to_relationship(token, parent):
    if not token.parent:
        return False
    if is_token_relationship(token.parent, None):
        return True


def is_token_relationship(token, parent):
    if token.pos in AUX_REL_POS:
        if parent and len(parent.children_ids) > 2:
            return False
        return is_related_to_relationship(token, parent)
    return token.pos in REL_POS and token.dep not in NON_REL_DEPS


def is_relationship(entity, parent):
    if entity.is_node():
        for child in entity.children():
            if not is_relationship(child, entity):
                return False
        return True
    else:
        return is_token_relationship(entity.token, parent)


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
