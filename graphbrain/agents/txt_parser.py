import progressbar
from graphbrain import *
from graphbrain.parsers import *
from graphbrain.agents.agent import Agent


def file_lines(filename):
    with open(filename, 'r') as f:
        for i, _ in enumerate(f, 1):
            pass
    return i


class TxtParser(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        # TODO: make parser type configurable
        self.parser = None
        self.lines = 0
        self.edges = 0

    def name(self):
        return 'txt_parser'

    def languages(self):
        return set()

    def start(self):
        self.parser = create_parser(name='en', lemmas=True)
        self.titles_parsed = 0
        self.titles_added = 0

    def _parse_line(self, line):
        self.lines += 1
        parses = self.parser.parse(line)
        for parse in parses:
            main_edge = parse['main_edge']

            # add main edge
            self.add(main_edge)
            self.edges += 1

            # attach text to edge
            # text = parse['text']
            # self.hg.set_attribute(main_edge, 'text', text)

            # add extra edges
            for edge in parse['extra_edges']:
                self.add(edge)

    def input_file(self, file_name):
        lines = file_lines(file_name)
        i = 0
        with progressbar.ProgressBar(max_value=lines) as bar:
            with open(file_name, 'r') as f:
                for line in f:
                    self._parse_line(line)
                    i += 1
                    bar.update(i)

    def report(self):
        rep_str = ('lines parsed: {}\nedges found: {}'.format(
            self.lines, self.edges))
        return '{}\n\n{}'.format(rep_str, super().report())
