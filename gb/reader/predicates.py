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


# English
REL_POS_EN = ['VERB', 'ADV', 'PART']
AUX_REL_POS_EN = ['ADP']


# French
REL_POS_FR = ['VERB', 'ADV', 'PART', 'AUX']
AUX_REL_POS_FR = ['ADP']


class Predicates(object):
    def __init__(self, lang='en'):
        if lang == 'en':
            self.rel_pos = REL_POS_EN
            self.aux_rel_pos = AUX_REL_POS_EN
        elif lang == 'fr':
            self.rel_pos = REL_POS_FR
            self.aux_rel_pos = AUX_REL_POS_FR

    def is_parent_predicate(self, token):
        if not token.parent:
            return False
        if self.is_token_predicate(token.parent, None):
            for child_token in token.parent.left_children:
                if child_token == token:
                    return True
                if not self.is_token_predicate(child_token, None):
                    return False
            for child_token in token.parent.right_children:
                if child_token == token:
                    return True
                if not self.is_token_predicate(child_token, None):
                    return False
                return False
        else:
            return False

    def is_token_predicate(self, token, parent):
        if token.pos in self.aux_rel_pos:
            if parent and not parent.compound and len(parent.children_ids) > 2:
                return False
            return self.is_parent_predicate(token)
        return token.pos in self.rel_pos

    def is_predicate(self, entity, parent):
        if entity.is_node():
            for child in entity.children():
                if not self.is_predicate(child, entity):
                    return False
            return True
        else:
            return self.is_token_predicate(entity.token, parent)
