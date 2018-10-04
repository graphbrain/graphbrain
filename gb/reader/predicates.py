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
