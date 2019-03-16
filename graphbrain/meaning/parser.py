import sys
import logging
import spacy
from graphbrain import *
from .vis import print_tree


logging.basicConfig(stream=sys.stderr, level=logging.ERROR)


deps_arg_types = {
    'nsubj': 's',      # subject
    'nsubjpass': 'p',  # passive subject
    'agent': 'a',      # agent
    'acomp': 'c',      # subject complement
    'attr': 'c',       # subject complement
    'dobj': 'o',       # direct object
    'prt': 'o',        # direct object
    'dative': 'i',     # indirect object
    'advcl': 'x',      # specifier
    'prep': 'x',       # specifier
    'parataxis': 't',  # parataxis
    'intj': 'j',       # interjection
    'xcomp': 'r',      # clausal complement
    'ccomp': 'r'       # clausal complement
}


def token_head_type(token):
    head = token.head
    if head and head != token:
        return token_type(head)
    else:
        return ''


def is_noun(token):
    return token.tag_[:2] == 'NN'


# TODO: check if complete
def is_verb(token):
    tag = token.tag_
    if len(tag) > 0:
        return token.tag_[0] == 'V'
    else:
        return False


def is_compound(token):
    return token.dep_ == 'compound'


def token_type(token):
    dep = token.dep_
    head_type = token_head_type(token)
    if len(head_type) > 1:
        head_subtype = head_type[1]
    else:
        head_subtype = ''
    if len(head_type) > 0:
        head_type = head_type[0]

    if dep == 'ROOT':
        if token.pos_ == 'VERB':  # TODO: generalize!
            return 'p'
        else:
            return 'c'
    elif dep in {'acomp', 'appos', 'attr', 'compound', 'dative', 'dep',
                 'dobj', 'nsubj', 'nsubjpass', 'oprd', 'pobj', 'meta'}:
        return 'c'
    elif dep in {'advcl', 'ccomp', 'csubj', 'csubjpass', 'parataxis'}:
        return 'p'
    elif dep == 'relcl':
        if is_verb(token):
            return 'pr'
        else:
            return 'c'
    elif dep in {'acl', 'pcomp', 'xcomp'}:
        if token.tag_ == 'IN':
            return 'a'
        else:
            return 'pc'
    elif dep in {'amod', 'det', 'npadvmod', 'nummod', 'nmod', 'preconj',
                 'predet'}:
        return 'm'
    elif dep in {'aux', 'auxpass', 'expl', 'prt', 'quantmod'}:
        if token.n_lefts + token.n_rights == 0:
            return 'a'
        else:
            return 'x'
    elif dep == 'cc':
        if head_type == 'p':
            return 'pm'
        else:
            return 'b'
    elif dep == 'case':
        if token.head.dep_ == 'poss':
            return 'bp'
        else:
            return 'b'
    elif dep == 'neg':
        return 'an'
    elif dep == 'agent':
        return 'x'
    elif dep in {'intj', 'punct'}:
        return ''
    elif dep == 'advmod':
        if token.head.dep_ == 'advcl':
            return 't'
        elif head_type == 'p':
            return 'a'
        elif head_type in {'m', 'x', 't'}:
            return 'w'
        else:
            return 'm'
    elif dep == 'poss':
        if is_noun(token):
            return 'c'
        else:
            return 'mp'
    elif dep == 'prep':
        if head_type == 'p':
            return 't'
        else:
            return 'b'
    elif dep == 'conj':
        if head_type == 'p' and is_verb(token):
            return 'p'
        else:
            return 'c'
    elif dep == 'mark':
        if head_type == 'p' and head_subtype != 'c':
            return 'x'
        else:
            return 'b'
    else:
        #  error / warning
        pass


def is_relative_concept(token):
    return token.dep_ == 'appos'


def arg_type(token):
    return deps_arg_types.get(token.dep_, '?')


def insert_after_predicate(targ, orig):
    targ_type = entity_type(targ)
    if targ_type[0] == 'p':
        return (targ, orig)
    elif targ_type[0] == 'r':
        if targ_type == 'rm':
            inner_rel = insert_after_predicate(targ[1], orig)
            return (targ[0], inner_rel) + tuple(targ[2:])
        else:
            return insert_first_argument(targ, orig)
    else:
        # TODO: error / warning
        print('ERROR %s %s' % (targ, orig))
        return targ


def nest_predicate(inner, outer, before):
    if entity_type(inner) == 'rm':
        first_rel = nest_predicate(inner[1], outer, before)
        return (inner[0], first_rel) + tuple(inner[2:])
    elif is_atom(inner) or entity_type(inner)[0] == 'p':
        return outer, inner
    else:
        return ((outer, inner[0]),) + inner[1:]


def post_process(entity):
    if is_atom(entity):
        return entity
    else:
        entity = tuple(post_process(item) for item in entity)
        if connector_type(entity)[0] == 'c':
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
        extra_edges = set()

        positions = {}
        tokens = {}
        children = []
        entities = []

        child_tokens = tuple((t, True) for t in token.lefts)
        child_tokens += tuple((t, False) for t in token.rights)

        for child_token, pos in child_tokens:
            child, child_extra_edges = self.parse_token(child_token)
            if child:
                extra_edges |= child_extra_edges
                positions[child] = pos
                tokens[child] = child_token
                child_type = entity_type(child)
                if child_type:
                    children.append(child)
                    if child_type[0] in {'c', 'r', 'd', 's'}:
                        entities.append(child)

        children.reverse()

        parent_type = token_type(token)
        if parent_type == '' or parent_type is None:
            return None, None

        # build atom
        if parent_type[0] == 'p' and parent_type != 'pm':
            if len(parent_type) == 1:
                parent_type = 'pd'  # TODO: questions, imperative...
            args = [arg_type(tokens[entity]) for entity in entities]
            args_string = ''.join([arg for arg in args if arg != '?'])
            parent_atom = build_atom(token.text.lower(),
                                     '%s.%s' % (parent_type, args_string))
        else:
            parent_atom = build_atom(token.text.lower(), parent_type)

        parent = parent_atom

        relative_to_concept = []

        # process children
        for child in children:
            child_type = entity_type(child)

            logging.debug('TARGET <-: [%s] %s', parent_type, parent)
            logging.debug('<- ORIG: [%s] %s', child_type, child)

            if child_type[0] in {'c', 'r', 'd', 's'}:
                if parent_type[0] == 'c':
                    if (connector_type(child) in {'pc', 'pr'} or
                            is_relative_concept(tokens[child])):
                        logging.debug('CHOICE #1')
                        relative_to_concept.append(child)
                    elif connector_type(child)[0] == 'b':
                        if connector_type(parent)[0] == 'c':
                            logging.debug('CHOICE #2')
                            parent = nest(parent, child, positions[child])
                        else:
                            logging.debug('CHOICE #3')
                            parent = apply_fun_to_atom(
                                lambda target:
                                    nest(target, child, positions[child]),
                                    parent_atom, parent)
                    elif connector_type(child)[0] in {'x', 't'}:
                        logging.debug('CHOICE #4')
                        parent = nest(parent, child, positions[child])
                    else:
                        if ((entity_type(parent_atom)[0] == 'c' and
                                connector_type(child)[0] == 'c') or
                                is_compound(tokens[child])):
                            if connector_type(parent)[0] == 'c':
                                if connector_type(child)[0] == 'c':
                                    logging.debug('CHOICE #5a')
                                    parent = sequence(parent, child,
                                                      positions[child])
                                else:
                                    logging.debug('CHOICE #5b')
                                    parent = sequence(parent, child,
                                                      positions[child],
                                                      flat=False)
                            else:
                                logging.debug('CHOICE #6')
                                parent = apply_fun_to_atom(
                                    lambda target:
                                        sequence(target, child,
                                                 positions[child]),
                                        parent_atom, parent)
                        else:
                            logging.debug('CHOICE #7')
                            parent = apply_fun_to_atom(
                                lambda target:
                                    connect(target, (child,)),
                                    parent_atom, parent)
                elif parent_type[0] in {'p', 'r', 'd', 's'}:
                    logging.debug('CHOICE #8')
                    parent = insert_after_predicate(parent, child)
                else:
                    logging.debug('CHOICE #9')
                    parent = insert_first_argument(parent, child)
            elif child_type[0] == 'b':
                if connector_type(parent) == 'c':
                    logging.debug('CHOICE #10')
                    parent = connect(child, parent)
                else:
                    logging.debug('CHOICE #11')
                    parent = nest(parent, child, positions[child])
            elif child_type[0] == 'p':
                # TODO: Pathological case
                # e.g. "Some subspecies of mosquito might be 1s..."
                if child_type == 'pm':
                    logging.debug('CHOICE #12')
                    # parent = nest(parent, child, positions[child])
                    parent = (child,) + parens(parent)
                else:
                    logging.debug('CHOICE #13')
                    parent = connect(parent, (child,))
            elif child_type[0] == 'm':
                logging.debug('CHOICE #14')
                parent = (child, parent)
            elif child_type[0] in {'x', 't'}:
                logging.debug('CHOICE #15')
                parent = (child, parent)
            elif child_type[0] == 'a':
                logging.debug('CHOICE #16')
                parent = nest_predicate(parent, child, positions[child])
            elif child_type == 'w':
                if parent_type[0] in {'d', 's'}:
                    logging.debug('CHOICE #17')
                    parent = nest_predicate(parent, child, positions[child])
                    # pass
                else:
                    logging.debug('CHOICE #18')
                    parent = nest(parent, child, positions[child])
            else:
                # TODO: warning ?
                logging.debug('CHOICE #19')
                pass

            parent_type = entity_type(parent)

            logging.debug('=== [%s] %s', parent_type, parent)

        if len(relative_to_concept) > 0:
            relative_to_concept.reverse()
            parent = (':/b/.', parent) + tuple(relative_to_concept)

        return post_process(parent), extra_edges

    def parse_sentence(self, sent):
        main_edge, extra_edges = self.parse_token(sent.root)
        return {'main_edge': main_edge,
                'extra_edges': extra_edges,
                'text': str(sent),
                'spacy_sentence': sent}

    def parse(self, text):
        doc = self.nlp(text)
        return tuple(self.parse_sentence(sent) for sent in doc.sents)


if __name__ == '__main__':
    text = """
    The Ethereum network will be undergoing a planned hard fork at block number
    4.37mil (4,370,000), which will likely occur between 12:00 UTC and 13:00
    UTC on Monday, October 16, 2017.
    """

    parser = Parser(lang='en')
    edge, sent = parser.parse(text)[0]
    print_tree(sent.root)
    print(ent2str(edge))
