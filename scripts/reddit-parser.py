import re
import time
import json
import progressbar
from graphbrain import *
from graphbrain.meaning import *
from graphbrain.cli import wrapper


def file_lines(filename):
    with open(filename, 'r') as f:
        for i, _ in enumerate(f, 1):
            pass
    return i


def title_parts(title):
    parts = re.split('\|| - | -- |^\[([^\]]*)\] | \[([^\]]*)\]$', title)
    parts = [part.strip().lower() for part in parts if part]
    return parts


class RedditParser(object):
    def __init__(self, hg):
        self.hg = hg
        self.parser = Parser(lang='en', pos=True, lemmas=True)
        self.main_edges = 0
        self.extra_edges = 0
        self.time_acc = 0
        self.items_processed = 0

    def parse_title(self, text, author):
        # print('\n{}'.format(text))

        parts = title_parts(text)

        title_edge = ['title/p/.reddit', author]
        tags = []
        for part in parts:
            parses = self.parser.parse(part)
            for parse in parses:
                main_edge = parse['main_edge']

                # add main edge
                self.hg.add(main_edge, deep=True)
                self.main_edges += 1

                # attach text to edge
                text = parse['text']
                self.hg.set_attribute(main_edge, 'text', text)

                # add extra edges
                for edge in parse['extra_edges']:
                    # print(ent2str(edge))
                    self.hg.add(edge)
                    self.extra_edges += 1

                if entity_type(main_edge)[0] == 'r':
                    title_edge.append(main_edge)
                else:
                    tags.append(main_edge)

        if len(title_edge) > 2:
            # add title edge
            # print(ent2str(title_edge))
            self.hg.add(title_edge)

            # add title tags
            if len(tags) > 0:
                tags_edge = ['tags/p/.reddit', title_edge] + tags
                # print(ent2str(tags_edge))
                self.hg.add(tags_edge)

    def parse_post(self, post):
        start_t = time.time()

        author = build_atom(post['author'], 'c', 'reddit.user')

        self.parse_title(post['title'], author)

        if self.items_processed > 0:
            delta_t = time.time() - start_t
            self.time_acc += delta_t
            items_per_min = float(self.items_processed) / float(self.time_acc)
            items_per_min *= 60.
            # print('total items: %s' % self.items_processed)
            # print('items per minute: %s' % items_per_min)
        self.items_processed += 1

    def parse_file(self, filename):
        lines = file_lines(filename)
        i = 0
        with progressbar.ProgressBar(max_value=lines) as bar:
            with open(filename, 'r') as f:
                for line in f:
                    post = json.loads(line)
                    self.parse_post(post)
                    i += 1
                    bar.update(i)

        print('main edges created: %s' % self.main_edges)
        print('extra edges created: %s' % self.extra_edges)


def _parse(args):
    print('Reddit parser')
    print('parsing file: {}'.format(args.infile))
    hgraph = hypergraph(args.hg)
    RedditParser(hgraph).parse_file(args.infile)


if __name__ == '__main__':
    wrapper(_parse)
