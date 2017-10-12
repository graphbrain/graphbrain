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


IGNORE, APPLY_HEAD, APPLY_TAIL, NEST_INNER, NEST_OUTER, NEST_SHALLOW, MULTINEST_INNER, MULTINEST_OUTER = range(8)


def apply(parent, child_id, transf):
    if transf == IGNORE:
        pass
    elif transf == APPLY_HEAD:
        parent.apply_head(child_id)
    elif transf == APPLY_TAIL:
        parent.apply_tail(child_id)
    elif transf == NEST_INNER:
        parent.nest(child_id, outer=False)
    elif transf == NEST_OUTER:
        parent.nest(child_id, outer=True)
    elif transf == MULTINEST_INNER:
        parent.nest_deep(child_id, outer=False)
    elif transf == MULTINEST_OUTER:
        parent.nest_deep(child_id, outer=True)
    elif transf == NEST_SHALLOW:
        parent.nest_shallow(child_id)


def with_position(transf, position):
    if transf == 'APPLY':
        if position == Position.LEFT:
            return APPLY_HEAD
        else:
            return APPLY_TAIL

    elif transf == 'NEST':
        if position == Position.LEFT:
            return NEST_OUTER
        else:
            return NEST_INNER

    elif transf == 'MULTINEST':
        if position == Position.LEFT:
            return MULTINEST_OUTER
        else:
            return MULTINEST_INNER


def to_string(transf):
    if transf == IGNORE:
        return 'ignore'
    elif transf == APPLY_HEAD:
        return 'apply [head]'
    elif transf == APPLY_TAIL:
        return 'apply [tail]'
    elif transf == NEST_INNER:
        return 'nest [inner]'
    elif transf == NEST_OUTER:
        return 'nest [outer]'
    elif transf == MULTINEST_INNER:
        return 'multinest [inner]'
    elif transf == MULTINEST_OUTER:
        return 'multinest [outer]'
    elif transf == NEST_SHALLOW:
        return 'nest [shallow]'
    else:
        return '?'
