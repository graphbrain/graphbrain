from gb.hypergraph import *
# from gb.funs import *
from gb.inference.rules import Rules


rules = Rules()


@rules.add_rule('is_a')
def is_a(_, edge, output):
    if len(edge) == 3 and edge[0] == 'is/nlp.be.verb':
        output.add(('is/gb', edge[1], edge[2]))


if __name__ == '__main__':
    hg = init_hypergraph('reddit-worldnews-01012013-01082017-new.hg')
    rules.apply_to(hg)
