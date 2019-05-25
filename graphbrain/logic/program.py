from graphbrain.funs import *


def is_var(atom):
    return is_atom(atom) and atom[0].isupper()


def pattern_and_variables(entity):
    pattern = []
    varset = set()
    for item in entity:
        if is_atom(item):
            if is_var(item):
                varset.add(item)
                pattern.append('*')
            else:
                pattern.append(item)
        else:
            pattern_, varset_ = pattern_and_variables(item)
            pattern.append(pattern_)
            varset |= varset_
    return pattern, varset


def match_variables(expression, entity, variables):
    for pair in zip(expression, entity):
        if is_atom(pair[0]):
            if is_var(pair[0]):
                var = pair[0]
                if var in variables:
                    if variables[var] != pair[1]:
                        return False
                else:
                    variables[var] = pair[1]
        elif is_edge(pair[1]):
            if not match_variables(pair[0], pair[1], variables):
                return False
    return True


def match_expression(hg, expression):
    pattern, varset = pattern_and_variables(expression)
    for entity in hg.pat2ents(pattern):
        variables = {}
        if match_variables(expression, entity, variables):
            yield variables
