from graphbrain import *
from graphbrain.meaning import *
from graphbrain.cli import wrapper
import graphbrain.synonyms.synonyms as synonyms


def _synonyms(args):
    hg = hypergraph(args.hg)
    iter = hg.pattern2edges(['title/p/.reddit', None, None], open_ended=True):
    synonyms.generate(hg)


if __name__ == '__main__':
    wrapper(_synonyms, text='reddit synonym generator')
