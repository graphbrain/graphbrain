import logging
from urllib.parse import urlparse

import requests

from graphbrain.cognition.agent import Agent


def url2title_and_lang(url):
    p = urlparse(url)

    netloc = p.netloc.split('.')
    if len(netloc) < 3 or 'wikipedia' not in netloc:
        raise RuntimeError('{} is not a valid wikipedia url.'.format(url))
    lang = netloc[0]

    path = [part for part in p.path.split('/') if part != '']
    if len(path) != 2 or path[0] != 'wiki':
        raise RuntimeError('{} is not a valid wikipedia url.'.format(url))
    title = path[1]

    return title, lang


def read_wikipedia(title, lang='en'):
    params = {
        'action': 'query',
        'format': 'json',
        'titles': title,
        'prop': 'extracts|revisions',
        'explaintext': '',
        'rvprop': 'ids',
    }

    api_url = 'http://{}.wikipedia.org/w/api.php'.format(lang)
    r = requests.get(api_url, params=params)
    request = r.json()

    for page_id in request['query']['pages']:
        return request['query']['pages'][page_id]['extract']

    return None


class Wikipedia(Agent):
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)

    def run(self):
        url = self.system.get_url(self)
        parser = self.system.get_parser(self)
        sequence = self.system.get_sequence(self)

        title, lang = url2title_and_lang(url)
        text = read_wikipedia(title, lang)

        pos = 0
        for line in text.split('\n'):
            paragraph = line.strip()
            if len(paragraph) == 0:
                continue

            for op in self.system.parse_results2ops(parser.parse(paragraph),
                                                    sequence=sequence,
                                                    pos=pos):
                yield op
