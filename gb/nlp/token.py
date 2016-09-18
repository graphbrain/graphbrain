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


class Token:
    """Generic NLP token."""

    def __init__(self):
        self.word = None
        self.lemma = None
        self.shape = None
        self.logprob = None
        self.pos = None
        self.dep = None
        self.parent = None
        self.left_children = None
        self.right_children = None
        self.entity_type = None

    def __str__(self):
        return '%s/%s (%s) {%s}' % (self.word.strip(), self.pos, self.dep, self.entity_type)

    def __repr__(self):
        return self.__str__()
