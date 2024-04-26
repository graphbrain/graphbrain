from urllib.parse import quote

import graphbrain.constants as const
from graphbrain.hyperedge import hedge
from graphbrain.notebook import _edge2html_blocks
from graphbrain.patterns import match_pattern, extract_vars_map


def _is_list(edge):
    return edge.not_atom and str(edge[0]) == const.list_or_matches_builder


def _val_values_equal(val1, val2):
    if _is_list(val1) and _is_list(val2):
        return set(val1[1:]) == set(val2[1:])
    else:
        return val1 == val2


class Rule:
    def __init__(self, positive, hg=None):
        self.positive = positive
        self.hg = hg
        self.pattern = None
        self.case_matches = []
        self.index = -1

    def copy(self):
        rule = Rule(self.positive, hg=self.hg)
        rule.pattern = self.pattern
        return rule

    def matches(self, edge):
        matches = match_pattern(edge, self.pattern, hg=self.hg)
        if len(matches) > 0:
            _vars = extract_vars_map(edge)
            if len(_vars) == 0:
                return matches
            for match in matches:
                match_found = True
                if len(match) != len(_vars):
                    match_found = False
                else:
                    for var in _vars:
                        if var not in match or not _val_values_equal(match[var], _vars[var]):
                            match_found = False
                            break
                if match_found:
                    return matches
        return []

    def n_case_matches(self):
        return len(self.case_matches)

    def pattern_html(self):
        return _edge2html_blocks(self.pattern)

    def pattern_url(self):
        return quote(str(self.pattern))

    def to_json(self):
        return {'positive': self.positive,
                'pattern': self.pattern.to_str()}

    def __str__(self):
        return 'pattern: {} positive: {}'.format(str(self.pattern), self.positive)


def from_json(json_obj, hg=None):
    rule = Rule(json_obj['positive'], hg=hg)
    rule.pattern = hedge(json_obj['pattern'])
    return rule
