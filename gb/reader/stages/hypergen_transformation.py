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


from gb.reader.semantic_tree import Position


IGNORE, APPLY, NEST, SHALLOW, DEEP, FIRST, APPLY_R, APPLY_L, NEST_R, NEST_L, DEEP_R, DEEP_L = range(12)


def apply(parent, child_id, position, transf):
    if transf == IGNORE:
        pass
    elif transf == APPLY:
        parent.apply(child_id, position)
    elif transf == NEST:
        parent.nest(child_id, position)
    elif transf == SHALLOW:
        parent.nest_shallow(child_id)
    elif transf == DEEP:
        parent.nest_deep(child_id, position)
    elif transf == FIRST:
        parent.insert(child_id, Position.LEFT)
    elif transf == APPLY_R:
        parent.apply(child_id, Position.RIGHT)
    elif transf == APPLY_L:
        parent.apply(child_id, Position.LEFT)
    elif transf == NEST_R:
        parent.nest(child_id, Position.RIGHT)
    elif transf == NEST_L:
        parent.nest(child_id, Position.LEFT)
    elif transf == DEEP_R:
        parent.nest_deep(child_id, Position.RIGHT)
    elif transf == DEEP_L:
        parent.nest_deep(child_id, Position.LEFT)


def with_position(transf, position):
    if transf == APPLY:
        if position == Position.LEFT:
            return APPLY_L
        else:
            return APPLY_R

    elif transf == NEST:
        if position == Position.LEFT:
            return NEST_L
        else:
            return NEST_R

    elif transf == DEEP:
        if position == Position.LEFT:
            return DEEP_L
        else:
            return DEEP_R


def to_string(transf):
    if transf == IGNORE:
        return 'ignore'
    elif transf == APPLY:
        return 'apply'
    elif transf == NEST:
        return 'nest'
    elif transf == SHALLOW:
        return 'shallow'
    elif transf == DEEP:
        return 'deep'
    elif transf == FIRST:
        return 'first'
    elif transf == APPLY_R:
        return 'apply [R]'
    elif transf == APPLY_L:
        return 'apply [L]'
    elif transf == NEST_R:
        return 'nest [R]'
    elif transf == NEST_L:
        return 'nest [L]'
    elif transf == DEEP_R:
        return 'deep [R]'
    elif transf == DEEP_L:
        return 'deep [L]'
    else:
        return '?'
