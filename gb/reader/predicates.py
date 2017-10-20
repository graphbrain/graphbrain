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


def is_related_to_predicate(token):
    if not token.parent:
        return False
    if is_token_predicate(token.parent, None):
        return True


def is_token_predicate(token, parent):
    if token.pos in AUX_REL_POS:
        if parent and len(parent.children_ids) > 2:
            return False
        return is_related_to_predicate(token)
    return token.pos in REL_POS and token.dep not in NON_REL_DEPS


def is_predicate(entity, parent):
    if entity.is_node():
        for child in entity.children():
            if not is_predicate(child, entity):
                return False
        return True
    else:
        return is_token_predicate(entity.token, parent)
