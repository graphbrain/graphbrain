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


from gb.funs import *
import gb.constants as const


class DisambNaive(object):
    def __init__(self, output):
        self.output = output

    def process_entity(self, entity_id):
        entity = self.output.tree.get(entity_id)

        entity.generate_namespace()

        if entity.is_leaf():
            if entity.token.word.lower() != entity.token.lemma.lower():
                lemma_ent = build_symbol(entity.token.lemma.lower(), entity.namespace)
                self.output.edges.append((const.have_same_lemma, entity.to_hyperedge(), lemma_ent))
        else:
            for child_id in entity.children_ids:
                self.process_entity(child_id)

    def process(self):
        self.process_entity(self.output.tree.root_id)
        return self.output
