from graphbrain import *
from graphbrain.parsers import *
from graphbrain.agents.agent import Agent


def paragraphs(file_name):
    paragraphs = []
    cur_paragraph = []
    with open(file_name, 'r') as f:
        lines = [line.strip() for line in f.readlines()]
        for line in lines:
            if len(line) == 0:
                paragraph = ' '.join(cur_paragraph)
                paragraphs.append(paragraph)
                cur_paragraph = []
            else:
                cur_paragraph.append(line)
    return paragraphs


class TxtParser(Agent):
    def __init__(self, hg, lang, sequence=None):
        super().__init__(hg, lang, sequence)
        self.parser = None
        self.edges = 0

    def name(self):
        return 'txt_parser'

    def languages(self):
        return set()

    def start(self):
        if not self.sequence:
            raise RuntimeError('Sequence name must be specified.')

        self.parser = create_parser(
            name=self.lang, lemmas=True, resolve_corefs=True)

    def input_file(self, file_name):
        pos = 0
        for paragraph in paragraphs(file_name):
            parse_results = self.parser.parse(paragraph)
            for parse in parse_results['parses']:
                main_edge = parse['resolved_corefs']

                # add main edge
                if main_edge:
                    self.hg.add_to_sequence(self.sequence, pos, main_edge)
                    self.edges += 1
                    pos += 1

                    # attach text to edge
                    print('{}\n{}\n'.format(parse['text'], main_edge.to_str()))
                    self.hg.set_attribute(main_edge, 'text', parse['text'])

                    # add extra edges
                    for edge in parse['extra_edges']:
                        self.add(edge)
            for edge in parse_results['inferred_edges']:
                print('inferred edge: {}'.format(edge.to_str()))
                self.add(edge, count=True)

    def report(self):
        rep_str = ('edges found: {}'.format(self.edges))
        return '{}\n\n{}'.format(rep_str, super().report())
