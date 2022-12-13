import logging
import inspect
from pathlib import Path

import graphbrain
from graphbrain import hgraph


logger = logging.getLogger()
logging.basicConfig(format='[{asctime}] {name}: {message}', style='{', level=logging.INFO)


hg_dir = Path(inspect.getfile(graphbrain)).parents[2] / 'hypergraphs'
HG_NAME = "reddit-worldnews-01012013-01082017.hg"


def get_hgraph(hg_name: str = HG_NAME):
    return hgraph(str(hg_dir / HG_NAME))


result_dir = Path(__file__).parent / 'results'
