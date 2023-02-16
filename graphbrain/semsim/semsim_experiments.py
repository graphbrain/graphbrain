import logging
import inspect
from pathlib import Path

import graphbrain
from graphbrain import hgraph
from graphbrain.parsers import create_parser

logger = logging.getLogger()
logging.basicConfig(format='[{asctime}] {name}: {message}', style='{', level=logging.INFO)

hg_dir = Path(inspect.getfile(graphbrain)).parents[2] / 'hypergraphs'

HG_NAME = "reddit-worldnews-01012013-01082017.hg"

hg = hgraph(str(hg_dir / HG_NAME))
parser = create_parser(lang='en')


# search_pattern = '((semsim say/P.{s-}) mother/C VAR)'
# search_pattern = '((lemma say/P.{s-}) mother/C VAR)'
# search_pattern = '((semsim fight/P.{s-}) (semsim mother/C) VAR)'
search_pattern = '((semsim fight/P.{s-} mother/C) VAR)'  # what happens in this case? is mother ignored?


search_results = list(hg.search(search_pattern))


output_str = f"Pattern: {search_pattern}\n" \
             f"N of results: {len(search_results)}\n" \
             f"Results:\n"

print(output_str)
for result in search_results:
    print(result)



