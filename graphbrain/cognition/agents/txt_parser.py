import logging

import progressbar

from graphbrain.cognition.agent import Agent


def read_paragraphs(file_name):
    with open(file_name, 'r') as f:
        for line in f.readlines():
            paragraph = line.strip()
            if len(paragraph) > 0:
                yield paragraph


class TxtParser(Agent):
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)
        self.edges = 0

    def on_start(self):
        if not self.system.get_sequence(self):
            raise RuntimeError('Sequence name must be specified.')

    def parse_text(self, infile, parser, sequence):
        paragraphs = list(read_paragraphs(infile))

        if self.progress_bar:
            pbar = progressbar.ProgressBar(max_value=len(paragraphs)).start()
        else:
            pbar = None

        for i, paragraph in enumerate(paragraphs):
            for op in self.system.parse_results2ops(parser.parse(paragraph),
                                                    sequence=sequence,
                                                    pos=i):
                yield op
            if self.progress_bar:
                pbar.update(i)

        if self.progress_bar:
            pbar.finish()

    def run(self):
        infile = self.system.get_infile(self)
        parser = self.system.get_parser(self)
        sequence = self.system.get_sequence(self)

        for op in self.parse_text(infile, parser, sequence):
            if op['sequence'] == sequence:
                self.edges += 1
            yield op

    def report(self):
        rep_str = ('edges found: {}'.format(self.edges))
        return '{}\n\n{}'.format(rep_str, super().report())
