import logging

from graphbrain import hgraph
from graphbrain.parsers import create_parser

logger = logging.getLogger()
logging.basicConfig(format='[{asctime}] {name}: {message}', style='{', level=logging.DEBUG)

HG_PATH = "../../../hypergraphs/reddit-worldnews-01012013-01082017.hg"

hg = hgraph(HG_PATH)
parser = create_parser(lang='en')

search_pattern = '((semsim say/P.{s-}) mother/C VAR)'
# search_pattern = '((lemma say/P.{s-}) mother/C VAR)'


search_results = list(hg.search(search_pattern))

print(f"N of results: {len(search_results)}")
print(f"Results:\n{search_results}")


