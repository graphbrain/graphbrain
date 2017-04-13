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


REL_POS = ['VERB', 'ADV', 'ADP', 'PART']


def is_relationship(entity, shallow=False, depth=0):
    if entity.is_node():
        if shallow and depth > 0:
            return False
        for child in entity.children():
            if not is_relationship(child, shallow, depth + 1):
                return False
        return True
    else:
        return (entity.token.pos in REL_POS)\
               and (entity.token.dep != 'conj')\
               and (entity.token.dep != 'amod')\
               and (entity.token.dep != 'case')


def is_possessive(entity):
    if entity.is_node():
        return False
    if entity.token.pos == 'PART' and entity.token.dep == 'case':
        return True
    return False
