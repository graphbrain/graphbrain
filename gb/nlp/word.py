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


class Word:
    """Generic NLP word."""

    def __init__(self):
        self.text = None
        self.prob = None
        self.vector = None
        self.sword = None

    def similarity(self, other):
        return self.sword.similarity(other.sword)

    def __eq__(self, other):
        if isinstance(other, Word):
            return self.text == other.text
        return NotImplemented

    def __str__(self):
        return self.text

    def __repr__(self):
        return self.__str__()

    def __hash__(self):
        return hash(self.text)
