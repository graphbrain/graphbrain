import os
from pathlib import Path

gensim_data_path: Path = Path(__file__).parent / 'gensim-data'
os.environ['GENSIM_DATA_DIR'] = str(gensim_data_path)


# is going to be moved anyway
from .semsim import match_semsim
