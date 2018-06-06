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


IGNORE, APPLY_NODE, NEST_NODE, APPLY_ROOT, NEST_ROOT, APPEND = range(6)


def apply(parent, root, child_id, pos, transf):
    if transf == IGNORE:
        pass
    elif transf == APPLY_NODE:
        if pos == Position.LEFT:
            parent.apply_head(child_id)
        else:
            parent.apply_tail(child_id)
    elif transf == NEST_NODE:
        parent.nest(child_id)
    elif transf == APPLY_ROOT:
        root.apply_tail(child_id)
    elif transf == NEST_ROOT:
        root.nest(child_id)
    elif transf == APPEND:
        parent.reverse_apply(child_id)


def to_string(transf):
    if transf == IGNORE:
        return 'ignore'
    elif transf == APPLY_NODE:
        return 'apply node'
    elif transf == NEST_NODE:
        return 'next node'
    elif transf == APPLY_ROOT:
        return 'apply root'
    elif transf == NEST_ROOT:
        return 'nest root'
    elif transf == APPEND:
        return 'append'
    else:
        return '?'
