import re
import json
import progressbar
from graphbrain import *
from graphbrain.parsers import *
from graphbrain.agents.agent import Agent


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
    def __init__(self, hg):
        super().__init__(hg)
        # TODO: make parser type configurable
        self.parser = None
        self.titles_parsed = 0
        self.titles_added = 0

    def name(self):
        return 'reddit_parser'

    def languages(self):
        return set()

    def start(self):
        self.parser = create_parser(name='en', lemmas=True)
        self.titles_parsed = 0
        self.titles_added = 0

    def _parse_title(self, text, author):
        parts = title_parts(text)

        title_edge = ['title/p/.reddit', author]
        tags = []
        for part in parts:
            parses = self.parser.parse(part)
            for parse in parses:
                main_edge = parse['main_edge']

                # add main edge
                self.add(main_edge)

                # attach text to edge
                text = parse['text']
                self.hg.set_attribute(main_edge, 'text', text)

                # add extra edges
                for edge in parse['extra_edges']:
                    self.add(edge)

                if main_edge.type()[0] == 'r':
                    title_edge.append(main_edge)
                else:
                    tags.append(main_edge)

        if len(title_edge) > 2:
            # add title edge
            self.add(title_edge)
            self.titles_added += 1

            # add title tags
            if len(tags) > 0:
                tags_edge = ['tags/p/.reddit', title_edge] + tags
                self.add(tags_edge)

        self.titles_parsed += 1

    def _parse_post(self, post):
        author = build_atom(post['author'], 'c', 'reddit.user')
        self._parse_title(post['title'], author)

    def input_file(self, file_name):
        lines = file_lines(file_name)
        i = 0
        with progressbar.ProgressBar(max_value=lines) as bar:
            with open(file_name, 'r') as f:
                for line in f:
                    post = json.loads(line)
                    self._parse_post(post)
                    i += 1
                    bar.update(i)

    def report(self):
        rep_str = ('titles parsed: {}\n'
                   'titles added: {}'.format(self.titles_parsed,
                                             self.titles_added))
        return '{}\n\n{}'.format(rep_str, super().report())
