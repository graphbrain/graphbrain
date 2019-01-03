import argparse
import progressbar
import pandas as pd
from graphbrain.hypergraph import HyperGraph
from graphbrain.funs import *
import graphbrain.nlp.parser as par


def read_predicate_table(file_path):
    pred_table = {}
    pred_df = pd.read_csv(file_path, sep=';')
    for r in pred_df.iterrows():
        row = r[1]
        pred = row['predicate']
        pred_table[pred] = {}
        pred_table[pred]['3'] = row['3'] == 'x'
        pred_table[pred]['4'] = row['4'] == 'x'
        pred_table[pred]['actor'] = row['actor'] == 'x'
        pred_table[pred]['claim'] = row['claim'] == 'x'
        pred_table[pred]['conflict'] = row['conflict'] == 'x'
        pred_table[pred]['agreement'] = row['agreement'] == 'x'
        pred_table[pred]['positive-view'] = row['positive-view'] == 'x'
        pred_table[pred]['negative-view'] = row['negative-view'] == 'x'
        pred_table[pred]['positive-association'] = row['positive-association'] == 'x'
        pred_table[pred]['negative-association'] = row['negative-association'] == 'x'
    return pred_table


class Headlines(object):
    def __init__(self, hg, parser, pred_table_path):
        self.hg = hg
        self.parser = parser
        self.pred_table = read_predicate_table(pred_table_path)
        self.claims = 0
        self.conflicts = 0

    def add_mention(self, actor, concept, edge):
        mention = ('mention/gb.inf', actor, concept)
        self.hg.add(mention)
        self.hg.add(('source/gb.inf', mention, edge))

    def add_conflict_over(self, orig, targ, concept, edge):
        conflict = ('conflict/gb.inf', orig, targ, concept)
        self.hg.add(conflict)
        self.hg.add(('source/gb.inf', conflict, edge))

    def infer_from_edge(self, edge, arity):
        if len(edge) < 3:
            return
        pred = edge[0]
        if not self.pred_table[pred][str(arity)]:
            return
        actor_orig = edge[1]
        if self.pred_table[pred]['claim']:
            if is_edge(edge[2]):
                self.claims += 1
                for concept in edge[2:]:
                    self.add_mention(actor_orig, concept, edge)
        if self.pred_table[pred]['conflict']:
            if len(edge) > 3:
                actor_targ = edge[2]
                self.conflicts += 1
                for concept in edge[3:]:
                    self.add_conflict_over(actor_orig, actor_targ, concept, edge)

    def process(self):
        self.claims = 0
        self.conflicts = 0

        print('performing inferences...')
        i = 0
        with progressbar.ProgressBar(max_value=len(self.pred_table)) as bar:
            for pred in self.pred_table:
                edges = self.hg.pattern2edges((pred, None, None))
                for edge in edges:
                    self.infer_from_edge(edge, 3)
                edges = self.hg.pattern2edges((pred, None, None, None))
                for edge in edges:
                    self.infer_from_edge(edge, 4)
                i += 1
                bar.update(i)

        print('claims: %s' % self.claims)
        print('conflicts: %s' % self.conflicts)


def headlines_inference(hg, predicates_file):
    parser = par.Parser()
    Headlines(hg, parser, predicates_file).process()


if __name__ == '__main__':
    argparser = argparse.ArgumentParser()

    argparser.add_argument('--backend', type=str, help='hypergraph backend (leveldb, null)', default='leveldb')
    argparser.add_argument('--hg', type=str, help='hypergraph name', default='gb.hg')
    argparser.add_argument('--infile', type=str, help='input file', default=None)

    args = argparser.parse_args()

    params = {
        'backend': args.backend,
        'hg': args.hg,
        'infile': args.infile,
    }

    hgraph = HyperGraph(params)
    infile = params['infile']
    headlines_inference(hgraph, infile)
