import csv
import logging
import re
import sys

import progressbar

from graphbrain.cognition.agent import Agent


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
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)
        # TODO: make this configurable
        self.text = 'title'
        self.parser = None

    def on_start(self):
        csv.field_size_limit(sys.maxsize)

    def _parse_row(self, row):
        parser = self.system.get_parser(self)

        parts = text_parts(row[self.text])

        for part in parts:
            for op in self.system.parse_results2ops(parser.parse(part)):
                yield op

    def run(self):
        infile = self.system.get_infile(self)

        lines = file_lines(infile) - 1
        i = 0
        with progressbar.ProgressBar(max_value=lines) as bar:
            with open(infile, 'r') as f:
                csv_reader = csv.DictReader(f)
                for row in csv_reader:
                    for wedge in self._parse_row(row):
                        yield wedge
                    i += 1
                    bar.update(i)
