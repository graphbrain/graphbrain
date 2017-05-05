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


class CandidateMetrics(object):
    def __init__(self):
        self.prob_meaning = float('inf')
        self.degree = 0

    def better_than(self, other):
        if not isinstance(other, CandidateMetrics):
            return NotImplemented
        if self.prob_meaning != other.prob_meaning:
            return self.prob_meaning < other.prob_meaning
        elif self.degree != other.degree:
            return self.degree > other.degree
        else:
            return False

    def __str__(self):
        return 'prob_meaning: %s; degree: %s' % (self.prob_meaning, self.degree)
