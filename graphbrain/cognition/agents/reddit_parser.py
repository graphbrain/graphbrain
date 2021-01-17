import json
import logging
import re

import progressbar

from graphbrain import build_atom
from graphbrain.cognition.agent import Agent
from graphbrain.op import create_op


def file_lines(filename):
    with open(filename, 'r') as f:
        for i, _ in enumerate(f, 1):
            pass
    return i


def title_parts(title):
    parts = re.split('\|| - | -- |^\[([^\]]*)\] | \[([^\]]*)\]$', title)
    parts = [part.strip() for part in parts if part]
    return parts


class RedditParser(Agent):
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)
        self.titles_parsed = 0
        self.titles_added = 0

    def on_start(self):
        self.titles_parsed = 0
        self.titles_added = 0

    def _parse_title(self, text, author):
        parser = self.system.get_parser(self)

        parts = title_parts(text)

        title_edge = ['title/P/.reddit', author]
        for part in parts:
            parse_results = parser.parse(part)
            for op in self.system.parse_results2ops(parse_results):
                yield op

            for parse in parse_results['parses']:
                if 'resolved_corefs' in parse:
                    main_edge = parse['resolved_corefs']
                else:
                    main_edge = parse['main_edge']

                if main_edge:
                    title_edge.append(main_edge)

        if len(title_edge) > 2:
            # add title edge
            yield create_op(title_edge)
            self.titles_added += 1

        self.titles_parsed += 1

    def _parse_post(self, post):
        author = build_atom(post['author'], 'C', 'reddit.user')
        for wedge in self._parse_title(post['title'], author):
            yield wedge

    def run(self):
        infile = self.system.get_infile(self)
        lines = file_lines(infile)
        i = 0
        with progressbar.ProgressBar(max_value=lines) as bar:
            with open(infile, 'r') as f:
                for line in f:
                    post = json.loads(line)
                    for wedge in self._parse_post(post):
                        yield wedge
                    i += 1
                    bar.update(i)

    def report(self):
        return 'titles parsed: {}; titles added: {}'.format(
            self.titles_parsed, self.titles_added)
