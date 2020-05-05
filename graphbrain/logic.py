class Inference(object):
    def __init__(self, edge=None, matches=(), variables={}):
        self.edge = edge
        self.matches = matches
        self.variables = variables

    def __add__(self, other):
        edge = other.edge if self.edge is None else self.edge
        matches = self.matches + other.matches
        variables = {**self.variables, **other.variables}
        return Inference(edge=edge, matches=matches, variables=variables)


def is_rule(edge):
    if edge.is_atom():
        return False
    if len(edge) != 3:
        return False
    if edge[0].to_str() != ':-':
        return False
    if edge[1].is_atom() or edge[2].is_atom():
        return False
    return True


def match_premises(hg, premises, inference):
    if len(premises) == 0:
        yield inference
    else:
        for inference_i in match_premise(hg, premises[0], inference.variables):
            for inference_j in match_premises(hg, premises[1:], inference_i):
                yield inference + inference_j


def match_premise(hg, premise, curvars={}):
    if premise[0].to_str() == 'and':
        for inference in match_premises(hg, premise[1:], Inference()):
            yield inference
    else:
        pattern = premise.apply_vars(curvars)
        for edge, results in hg.match(pattern):
            for result in results:
                yield Inference(
                    matches=((premise, edge),),
                    variables={**curvars, **result})


def eval_rule(hg, rule):
    if not is_rule(rule):
        raise RuntimeError('Not a valid rule: {}'.format(rule.to_str()))

    for inference in match_premise(hg, rule[2]):
        inference.edge = rule[1].apply_vars(inference.variables)
        yield inference
