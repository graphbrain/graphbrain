import spacy
from graphbrain.funs import edge2str


deps_types = {
    'acl': 'p',  # concept-predicate? ###
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
    'prep': {
        'p': 'x',
        '*': 'b'
    },
    'prt': 'a',  # could be in front of predicate, e.g. "shut down")
    'punct': '',  # ignore
    'quantmod': 'w',
    'relcl': 'p',
    'root': '',
    'xcomp': 'c'
}


deps_arg_types = {
    'nsubj': 's',      # subject
    'nsubjpass': 'p',  # passive subject
    'agent': 'a',      # agent
    'acomp': 'c',      # subject complement
    'dobj': 'o',       # direct object
    'dative': 'i',     # indirect object
    'advcl': 'x',      # specifier
    'prep': 'x',       # specifier
    'parataxis': 't',  # parataxis
    'intj': 'j'        # interjection
}


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
    dep = token.dep_
    if dep == 'ROOT':
        if token.pos_ == 'VERB':  # TODO: generalize!
            return 'p'
        else:
            return 'c'
    else:
        typ = deps_types.get(dep, '')
        if type(typ) is str:
            return typ
        else:
            head = token.head
            if head:
                parent_dep = token_type(head)
            else:
                parent_dep = '*'
            return typ.get(parent_dep, typ['*'])


def arg_type(token):
    return deps_arg_types.get(token.dep_, '?')


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


class Parser(object):
    def __init__(self, lang):
        if lang == 'en':
            self.nlp = spacy.load('en_core_web_lg')
        elif lang == 'fr':
            self.nlp = spacy.load('fr_core_news_md')
        else:
            raise RuntimeError('unkown language: %s' % lang)

    def parse_token(self, token):
        entities = []
        modifiers = []
        builders = []
        auxiliaries = []
        metas = []
        subpredicates = []
        positions = {}
        tokens = {}

        parent_type = token_type(token)

        child_tokens = tuple((t, True) for t in token.lefts) + tuple((t, False) for t in token.rights)

        for child_token, pos in child_tokens:
            child = self.parse_token(child_token)
            positions[child] = pos
            tokens[child] = child_token
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

        parent = '%s/%s' % (token.text.lower(), parent_type)

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

            if not is_atom(parent) and connector_type(parent) == 'c':
                parent = apply_n('+/b/gb', parent)

        elif parent_type == 'p':
            subtype = 'd'  # TODO
            args_string = ''.join([arg_type(tokens[entity]) for entity in entities])
            parent = '%s.%s.%s' % (parent, subtype, args_string)

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
                parent = nest(parent, builder, positions[builder])

            parent = apply_n(parent, entities)

        elif parent_type == 'm':
            for meta in metas:
                parent = nest(parent, meta, positions[meta])

            for modifier in modifiers:
                parent = nest(parent, modifier, positions[modifier])

            for builder in builders:
                parent = nest(parent, builder, positions[builder])

        if parent_type == 'x':
            parent = connect(parent, entities)

        elif parent_type == '':
            return None

        else:
            # error
            pass

        # print(edge2str(parent))
        return parent

    def parse(self, text):
        doc = self.nlp(text)
        return [(self.parse_token(sent.root), sent) for sent in doc.sents]
