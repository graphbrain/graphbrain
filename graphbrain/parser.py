from graphbrain.funs import edge2str
from graphbrain.nlp.parser import Parser


deps_types = {'acl': 'p',  # concept-predicate? ###
              'acomp': 'c',
              'advcl': 'p',
              'advmod': 'w',  # meta-modifier?
              'agent': 'x',
              'amod': 'm',
              'appos': 'c',
              'attr': 'c',
              'aux': 'a',
              'auxpass': 'a',
              'case': 'b',  # ##
              'cc': 'b',
              'ccomp': 'p',
              'compound': 'c',  # add +/b to form concept above
              'conj': 'c',
              'csubj': 'p',  # ?
              'csubjpass': 'p',  # ?
              'dative': 'c',
              'dep': 'c',
              'det': 'm',
              'dobj': 'c',
              'expl': 'a',
              'intj': '',  # ignore
              'mark': 'x',
              'meta': 'c',  # ?
              'neg': 'a',   # should be own type
              'npadvmod': 'm',  # multiple words? ("a share")
              'nsubj': 'c',
              'nsubjpass': 'c',
              'nummod': 'm',
              'oprd': 'c',
              'parataxis': 'p',
              'pcomp': 'p',
              'pobj': 'c',
              'poss': 'c',
              'preconj': 'm',
              'predet': 'm',
              'prep': 'b',
              'prt': 'a',  # could be in front of predicate, e.g. "shut down")
              'punct': '',  # ignore
              'quantmod': 'w',
              'relcl': 'p',
              'root': '',
              'xcomp': 'c'}


def is_atom(entity):
    return isinstance(entity, str)


def atom_role(atom):
    parts = atom.split('/')
    if len(parts) < 2:
        return tuple('c')
    else:
        return parts[1].split('.')


def atom_type(atom):
    return atom_role(atom)[0]


def entity_type(entity):
    if is_atom(entity):
        return atom_type(entity)
    else:
        ptype = entity_type(entity[0])
        if len(entity) < 2:
            return ptype
        else:
            if ptype == 'p':
                return 'r'
            elif ptype == 'a':
                return 'p'
            elif ptype == 'w':
                return 'm'
            elif ptype == 'x':
                return 'd'
            elif ptype == 't':
                return 's'
            else:
                return 'c'


def connector_type(entity):
    if is_atom(entity):
        return entity_type(entity)
    else:
        return entity_type(entity[0])


def token_type(token):
    dep = token.dep
    if dep == 'ROOT':
        if token.pos == 'VERB':
            return 'p'
        else:
            return 'c'
    return deps_types.get(dep, '')


def nest(inner, outer, before):
    if is_atom(outer):
        return outer, inner
    else:
        if before:
            return outer + (inner,)
        else:
            return (outer[0], inner) + outer[1:]


def parens(entity):
    if is_atom(entity):
        return (entity,)
    else:
        return entity


def connect(connector, arguments):
    return (connector,) + tuple(arguments)


def apply_n(entity, arguments):
    if len(arguments) == 0:
        return entity
    else:
        return parens(entity) + tuple(arguments)


def sequence(target, entity, before):
    if before:
        return parens(entity) + parens(target)
    else:
        return parens(target) + parens(entity)


def process_token(token):
    entities = []
    modifiers = []
    builders = []
    auxiliaries = []
    metas = []
    subpredicates = []
    positions = {}

    parent_type = token_type(token)

    child_tokens = tuple((t, True) for t in token.left_children) + tuple((t, False) for t in token.right_children)

    for child_token, pos in child_tokens:
        child = process_token(child_token)
        positions[child] = pos
        if child:
            child_type = entity_type(child)
            if child_type in {'c', 'r', 'd'}:
                if parent_type == 'c' and connector_type(child) == 'b':
                    builders.append(child)
                else:
                    entities.append(child)
            elif child_type == 'a':
                auxiliaries.append(child)
            elif child_type == 'm':
                modifiers.append(child)
            elif child_type == 'b':
                builders.append(child)
            elif child_type == 'w':
                metas.append(child)
            elif child_type == 'x':
                subpredicates.append(child)
            else:
                # error
                pass

    modifiers.reverse()
    builders.reverse()
    auxiliaries.reverse()
    metas.reverse()
    subpredicates.reverse()

    parent = '%s/%s' % (token.word, parent_type)

    # print('')
    # print('=> %s' % parent_type)
    # print('entities: %s' % len(entities))
    # print(edge2str(parent))

    if parent_type == 'c':
        for builder in builders:
            parent = nest(parent, builder, positions[builder])

        if is_atom(parent) or entity_type(parent[0]) == 'c':
            for entity in entities:
                parent = sequence(parent, entity, positions[entity])
        else:
            parent = apply_n(parent, entities)

        for modifier in modifiers:
            parent = nest(parent, modifier, positions[modifier])

    elif parent_type == 'p':
        for auxiliary in auxiliaries:
            parent = nest(parent, auxiliary, positions[auxiliary])

        parent = connect(parent, entities)

        for subpredicate in subpredicates:
            parent = nest(parent, subpredicate, positions[subpredicate])

    elif parent_type == 'b':
        for meta in metas:
            parent = nest(parent, meta, positions[meta])

        for modifier in modifiers:
            parent = nest(parent, modifier, positions[modifier])

        for builder in builders:
            parent = nest(parent, builder, positions[modifier])

        parent = apply_n(parent, entities)

    elif parent_type == 'm':
        for meta in metas:
            parent = nest(parent, meta, positions[meta])

        for modifier in modifiers:
            parent = nest(parent, modifier, positions[modifier])

        for builder in builders:
            parent = nest(parent, builder, positions[modifier])

    if parent_type == 'x':
        parent = connect(parent, entities)

    elif parent_type == '':
        return None

    else:
        # error
        pass

    # print(edge2str(parent))

    return parent


def process_sentence(sentence):
    return process_token(sentence.root())


if __name__ == '__main__':
    test_text = """
    Satellites from NASA and other agencies have been tracking sea ice changes since 1979.
    """

    # test_text = """
    #    The algorithm can play both black and white.
    #    """

    test_text = "Britain's hopes of a trade deal with America just suffered a big blow"

    print('Starting parser...')
    parser = Parser()
    print('Parsing...')
    result = parser.parse_text(test_text)
    sent = result[0][1]

    sent.print_tree()

    edge = process_sentence(sent)
    print(edge2str(edge))
