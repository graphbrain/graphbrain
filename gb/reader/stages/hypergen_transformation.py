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


IGNORE, APPLY_HYPEREDGE, NEST_HYPEREDGE, APPLY_TOKEN, NEST_TOKEN, HEAD = range(6)


def apply(parent, root, child_id, pos, transf):
    if transf == IGNORE:
        pass
    elif transf == APPLY_HYPEREDGE:
        if pos == Position.LEFT:
            parent.apply_head(child_id)
        else:
            parent.apply_tail(child_id)
    elif transf == NEST_HYPEREDGE:
        parent.nest(child_id)
    elif transf == APPLY_TOKEN:
        root.apply_tail(child_id)
    elif transf == NEST_TOKEN:
        root.nest(child_id)
    elif transf == HEAD:
        parent.reverse_apply(child_id)


def to_string(transf):
    if transf == IGNORE:
        return 'ignore'
    elif transf == APPLY_HYPEREDGE:
        return 'apply hyperedge'
    elif transf == NEST_HYPEREDGE:
        return 'nest hyperedge'
    elif transf == APPLY_TOKEN:
        return 'apply token'
    elif transf == NEST_TOKEN:
        return 'nest token'
    elif transf == HEAD:
        return 'head'
    else:
        return '?'
