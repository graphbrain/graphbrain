import spacy
from graphbrain import *


deps_arg_types = {
    'nsubj': 's',      # subject
    'nsubjpass': 'p',  # passive subject
    'agent': 'a',      # agent
    'acomp': 'c',      # subject complement
    'attr': 'c',       # subject complement
    'dobj': 'o',       # direct object
    'dative': 'i',     # indirect object
    'advcl': 'x',      # specifier
    'prep': 'x',       # specifier
    'parataxis': 't',  # parataxis
    'intj': 'j'        # interjection
}


def token_head_type(token):
    head = token.head
    if head:
        return token_type(head)
    else:
        return ''


def is_noun(token):
    return token.tag_[:2] == 'NN'


def token_type(token):
    dep = token.dep_
    if dep == 'ROOT':
        if token.pos_ == 'VERB':  # TODO: generalize!
            return 'p'
        else:
            return 'c'
    elif dep in {'acomp', 'appos', 'attr', 'compound', 'conj', 'dative', 'dep', 'dobj', 'nsubj', 'nsubjpass',
                 'oprd', 'pobj', 'meta', 'xcomp'}:
        return 'c'
    elif dep in {'acl', 'advcl', 'ccomp', 'csubj', 'csubjpass', 'parataxis', 'pcomp', 'relcl'}:
        return 'p'
    elif dep in {'amod', 'det', 'npadvmod', 'nummod', 'preconj', 'predet'}:
        return 'm'
    elif dep in {'aux', 'auxpass', 'expl', 'neg', 'prt', 'quantmod'}:
        return 'a'
    elif dep in {'case', 'cc', 'mark'}:
        return 'b'
    elif dep == 'agent':
        return 'x'
    elif dep in {'intj', 'punct'}:
        return ''
    elif dep == 'advmod':
        if token_head_type(token) == 'p':
            return 'a'
        else:
            return 'm'
    elif dep == 'poss':
        if is_noun(token):
            return 'c'
        else:
            return 'm'
    elif dep == 'prep':
        if token_head_type(token) == 'p':
            return 'x'
        else:
            return 'b'
    else:
        #  error / warning
        pass


def arg_type(token):
    return deps_arg_types.get(token.dep_, '?')


def post_process(entity):
    if is_atom(entity):
        return entity
    else:
        entity = tuple(post_process(item) for item in entity)
        if connector_type(entity) == 'c':
            return connect('+/b/.', entity)
        else:
            return entity


class Parser(object):
    def __init__(self, lang):
        if lang == 'en':
            self.nlp = spacy.load('en_core_web_lg')
        elif lang == 'fr':
            self.nlp = spacy.load('fr_core_news_md')
        else:
            raise RuntimeError('unkown language: %s' % lang)

    def parse_token(self, token):
        positions = {}
        tokens = {}
        children = []
        entities = []

        child_tokens = tuple((t, True) for t in token.lefts) + tuple((t, False) for t in token.rights)

        for child_token, pos in child_tokens:
            child = self.parse_token(child_token)
            positions[child] = pos
            tokens[child] = child_token
            if child:
                child_type = entity_type(child)
                if child_type:
                    children.append(child)
                    if child_type in {'c', 'r', 'd'}:
                        entities.append(child)

        children.reverse()

        parent_type = token_type(token)
        if parent_type == '':
            return None

        parent_atom = build_atom(token.text.lower(), parent_type)

        if parent_type == 'p':
            subtype = 'd'  # TODO
            args_string = ''.join([arg_type(tokens[entity]) for entity in entities])
            parent_atom = '%s.%s.%s' % (parent_atom, subtype, args_string)

        parent = parent_atom

        for child in children:
            child_type = entity_type(child)
            if child_type in {'c', 'r', 'd'}:
                if parent_type == 'c':
                    if connector_type(child) == 'b':
                        parent = nest(parent, child, positions[child])
                    else:
                        if entity_type(parent_atom) == 'c' and connector_type(child) == 'c':
                            parent = apply_fun_to_atom(lambda target: sequence(target, child, positions[child]),
                                                       parent_atom, parent)
                        else:
                            parent = apply_fun_to_atom(lambda target: connect(target, (child,)), parent_atom, parent)
                else:
                    parent = insert_first_argument(parent, child)
            elif child_type == 'b':
                if connector_type(parent) == 'c':
                    parent = connect(child, parent)
                else:
                    parent = nest(parent, child, positions[child])
            elif child_type == 'm':
                parent = nest(parent, child, positions[child])
            elif child_type == 'a':
                parent = nest_predicate(parent, child, positions[child])
            elif child_type == 'w':
                parent = nest(parent, child, positions[child])
            else:
                # TODO: warning ?
                pass
            
            parent_type = entity_type(parent)

        return post_process(parent)

    def parse(self, text):
        doc = self.nlp(text)
        return [(self.parse_token(sent.root), sent) for sent in doc.sents]
