import re
import json
import progressbar
from graphbrain import build_atom
from graphbrain.agents.agent import Agent
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
    def __init__(self):
        super().__init__()
        self.titles_parsed = 0
        self.titles_added = 0

    def name(self):
        return 'reddit_parser'

    def on_start(self):
        self.titles_parsed = 0
        self.titles_added = 0

    def _parse_title(self, text, author):
        parser = self.system.get_parser(self)

        parts = title_parts(text)

        title_edge = ['title/P/.reddit', author]
        tags = []
        for part in parts:
            parse_results = parser.parse(part)
            for parse in parse_results['parses']:
                main_edge = parse['resolved_corefs']

                # add main edge
                if main_edge:
                    # attach text to edge
                    text = parse['text']
                    attr = {'text': text}
                    yield create_op(main_edge, attributes=attr)

                    # add extra edges
                    for edge in parse['extra_edges']:
                        yield create_op(edge)

                    if main_edge.type()[0] == 'R':
                        title_edge.append(main_edge)
                    else:
                        tags.append(main_edge)
            for edge in parse_results['inferred_edges']:
                yield create_op(edge, count=True)

        if len(title_edge) > 2:
            # add title edge
            yield create_op(title_edge)
            self.titles_added += 1

            # add title tags
            if len(tags) > 0:
                tags_edge = ['tags/P/.reddit', title_edge] + tags
                yield create_op(tags_edge)

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
        return 'titles parsed: {}\ntitles added: {}'.format(
            self.titles_parsed, self.titles_added)
