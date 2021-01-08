import logging
import os
import re

from graphbrain.cognition.agent import Agent
from graphbrain.cognition.agents.txt_parser import parse_text


def build_sequence_name(path, name):
    seq_name = '|'.join((path, name))
    seq_name = seq_name.lower()
    seq_name = seq_name.replace('/', '|')
    seq_name = seq_name.replace(' ', '-')
    seq_name = re.sub('[^a-z0-9\_\-|]+', '', seq_name)
    return seq_name


class DirParser(Agent):
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)
        self.sequences = []

    def run(self):
        indir = self.system.get_indir(self)
        parser = self.system.get_parser(self)

        for dirpath, _, filenames in os.walk(indir):
            path = dirpath[len(indir) + 1:]
            for filename in filenames:
                name, extension = os.path.splitext(filename)
                if extension == '.txt':
                    while extension != '':
                        name, extension = os.path.splitext(name)
                    sequence = build_sequence_name(path, name)
                    self.sequences.append(sequence)
                    infile = os.path.join(dirpath, filename)
                    print(sequence)
                    for op in parse_text(infile, parser, sequence):
                        yield op

    def report(self):
        sequences = '\n'.join(self.sequences)
        return '{}\n\nSequences created:\n{}'.format(
            super().report(), sequences)
