from .parser_en import ParserEN


def create_parser(name, lemmas=False):
    if name == 'en':
        return ParserEN(lemmas=lemmas)
    else:
        raise RuntimeError('Unknown parser: {}'.format(name))
