import spacy
from graphbrain.nlp.word import Word
from graphbrain.nlp.nlp_token import Token
from graphbrain.nlp.sentence import Sentence


class Parser:
    """Generic NLP parser."""

    def __init__(self, lang='en'):
        self.token_table = {}
        if lang == 'en':
            self.parser = spacy.load('en_core_web_lg')
        elif lang == 'fr':
            self.parser = spacy.load('fr_core_news_md')
        else:
            raise RuntimeError('unkown language: %s' % lang)

    def __spacy2token(self, stoken, depth=0):
        if stoken is None:
            return None
        elif stoken not in self.token_table:
            token = Token()
            token.word = stoken.orth_.lower().strip()
            token.lemma = stoken.lemma_
            token.shape = stoken.shape_
            token.logprob = stoken.prob
            token.pos = stoken.pos_
            token.dep = stoken.dep_
            token.tag = stoken.tag_
            token.vector = stoken.vector
            if depth < 20:
                token.left_children = [self.__spacy2token(t, depth + 1) for t in stoken.lefts]
                token.right_children = [self.__spacy2token(t, depth + 1) for t in stoken.rights]
            else:
                token.left_children = []
                token.right_children = []
            for t in token.left_children:
                t.parent = token
            for t in token.right_children:
                t.parent = token
            token.entity_type = stoken.ent_type_
            self.token_table[stoken] = token

        return self.token_table[stoken]

    def make_word(self, text):
        word = Word()
        word.text = text.lower()
        sword = self.parser.vocab[word.text]
        word.prob = sword.prob
        word.vector = sword.vector
        word.sword = sword
        return word

    def parse_text(self, text):
        parsed_data = self.parser(text)

        self.token_table = {}

        sents = []
        for span in parsed_data.sents:
            sentence_text = text[span.start_char:span.end_char].strip()
            token_seq = [self.__spacy2token(parsed_data[i]) for i in range(span.start, span.end)]
            for i in range(len(token_seq)):
                token_seq[i].position_in_sentence = i
            sents.append((sentence_text, Sentence(token_seq)))

        return sents

    def print_trees(self, text):
        parses = self.parse_text(text)

        for p in parses:
            s = p[1]
            print(p[0])
            s.print_tree()
            print('')


if __name__ == '__main__':
    test_text = u"""Some subspecies of mosquito might be 1st to be genetically wiped out."""
    # test_text = u"""Des millions de Français n’ont pas accès à une connexion."""

    print('Starting parser...')
    parser = Parser()
    print('Parsing...')
    result = parser.parse_text(test_text)

    for r in result:
        print(r[0])
        r[1].print_tree()
        print('')
