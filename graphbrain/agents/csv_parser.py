import re
import csv
import sys
import progressbar
from graphbrain import *
from graphbrain.parsers import *
from graphbrain.agents.agent import Agent


def file_lines(filename):
    with open(filename, 'r') as f:
        for i, _ in enumerate(f, 1):
            pass
    return i


def text_parts(title):
    parts = re.split('\|| - | -- |^\[([^\]]*)\] | \[([^\]]*)\]$', title)
    parts = [part.strip() for part in parts if part]
    return parts


class CsvParser(Agent):
    def __init__(self, hg, lang, sequence=None, text='title'):
        super().__init__(hg, lang, sequence)
        self.text = text
        self.parser = None

    def name(self):
        return 'csv_parser'

    def languages(self):
        return set()

    def start(self):
        csv.field_size_limit(sys.maxsize)
        self.parser = create_parser(name=self.lang, lemmas=True)

    def _parse_row(self, row):
        parts = text_parts(row[self.text])

        for part in parts:
            parse_results = self.parser.parse(part)
            for parse in parse_results['parses']:
                main_edge = parse['main_edge']

                # add main edge
                if main_edge:
                    self.add(main_edge)

                    # attach text to edge
                    text = parse['text']
                    self.hg.set_attribute(main_edge, 'text', text)

                    # add extra edges
                    for edge in parse['extra_edges']:
                        self.add(edge)

    def input_file(self, file_name):
        lines = file_lines(file_name) - 1
        i = 0
        with progressbar.ProgressBar(max_value=lines) as bar:
            with open(file_name, 'r') as f:
                csv_reader = csv.DictReader(f)
                for row in csv_reader:
                    self._parse_row(row)
                    i += 1
                    bar.update(i)
