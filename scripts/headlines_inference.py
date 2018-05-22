import argparse
from collections import Counter
from unidecode import unidecode
import progressbar
import pandas as pd
from gb.hypergraph.hypergraph import HyperGraph
import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed
import gb.nlp.parser as par
import gb.synonyms.synonyms as syn


MAX_PROB = -12


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


def herfindhal_and_total(counter):
    weights = tuple(counter.values())
    total = sum(weights)
    h = 0.
    if total > 0:
        h_weights = [float(i) / float(total) for i in weights]
        h_weights = [i * i for i in h_weights]
        h = 1. / sum(h_weights)
    return h, total


class Headlines(object):
    def __init__(self, hg, parser, pred_table_path):
        self.hg = hg
        self.parser = parser
        self.pred_table = read_predicate_table(pred_table_path)
        self.actors = None
        self.entities = {}
        self.claims = 0
        self.conflicts = 0

    def find_actors(self):
        self.actors = {}

        print('finding actors...')
        i = 0
        with progressbar.ProgressBar(max_value=len(self.pred_table)) as bar:
            for pred in self.pred_table:
                if self.pred_table[pred]['actor']:
                    if self.pred_table[pred]['3']:
                        edges = self.hg.pattern2edges((pred, None, None))
                        for edge in edges:
                            actor = syn.main_synonym(self.hg, edge[1], in_adp=True)
                            if actor not in self.actors:
                                self.actors[actor] = 0
                            self.actors[actor] += 1
                    if self.pred_table[pred]['4']:
                        edges = self.hg.pattern2edges((pred, None, None, None))
                        for edge in edges:
                            actor = syn.main_synonym(self.hg, edge[1], in_adp=True)
                            if actor not in self.actors:
                                self.actors[actor] = 0
                            self.actors[actor] += 1
                i += 1
                bar.update(i)

        print('%s actors found.' % len(self.actors))

    def is_actor(self, entity):
        if entity in self.actors:
            if self.actors[entity] > 2:
                return True
        return False

    def get_concepts(self, edge):
        if sym.is_edge(edge):
            if ed.is_concept(edge) and self.hg.has_label(edge):
                concepts = {syn.main_synonym(self.hg, edge, in_adp=True)}
                return concepts
            else:
                concepts = set()
            if len(edge) > 1:
                for item in edge[1:]:
                    concepts |= self.get_concepts(item)
            return concepts
        else:
            word = self.parser.make_word(unidecode(ed.without_namespaces(edge)))
            if word.prob > MAX_PROB:
                return set()
            if edge[0] in {'`', '_', "'"}:
                return set()
            else:
                return {syn.main_synonym(self.hg, edge, in_adp=True)}

    def add_entity(self, entity):
        if entity not in self.entities:
            actor = self.is_actor(entity)
            self.entities[entity] = {'mentions': Counter(),
                                     'mentioned_by': Counter(),
                                     'actor': actor,
                                     'conflict_to': Counter(),
                                     'conflict_from': Counter(),
                                     'conflict_over': Counter(),
                                     'conflict_for': Counter()}

    def add_mention(self, actor, concept, edge):
        self.add_entity(actor)
        self.add_entity(concept)
        self.entities[actor]['mentions'][concept] += 1
        self.entities[concept]['mentioned_by'][actor] += 1

        mention = ('mention/gb.inf', actor, concept)
        self.hg.add(mention)
        self.hg.add(('source/gb.inf', mention, edge))

    def add_conflict(self, orig, targ):
        self.add_entity(orig)
        self.add_entity(targ)
        self.entities[orig]['conflict_to'][targ] += 1
        self.entities[targ]['conflict_from'][orig] += 1

    def add_conflict_over(self, orig, targ, concept, edge):
        self.add_entity(orig)
        self.add_entity(targ)
        self.add_entity(concept)
        self.entities[orig]['conflict_over'][concept] += 1
        self.entities[targ]['conflict_over'][concept] += 1
        self.entities[concept]['conflict_for'][orig] += 1
        self.entities[concept]['conflict_for'][targ] += 1

        conflict = ('conflict/gb.inf', orig, targ, concept)
        self.hg.add(conflict)
        self.hg.add(('source/gb.inf', conflict, edge))

    def infer_from_edge(self, edge, arity):
        pred = edge[0]
        if not self.pred_table[pred][str(arity)]:
            return
        actor_orig = syn.main_synonym(self.hg, edge[1], in_adp=True)
        if self.is_actor(actor_orig):
            actor_targs = set()
            concepts = set()
            for entity in edge[2:]:
                syn_entity = syn.main_synonym(self.hg, entity, in_adp=True)
                if self.is_actor(syn_entity):
                    actor_targs.add(syn_entity)
                else:
                    concepts |= self.get_concepts(entity)
            if self.pred_table[pred]['claim']:
                if len(edge) > 2 and sym.is_edge(edge[2]):
                    self.claims += 1
                    for concept in concepts:
                        self.add_mention(actor_orig, concept, edge)
            if self.pred_table[pred]['conflict']:
                for actor in actor_targs:
                    self.conflicts += 1
                    self.add_conflict(actor_orig, actor)
                    for concept in concepts:
                        self.add_conflict_over(actor_orig, actor, concept, edge)

    def infer(self):
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

    def write_metrics(self, entity):
        self.hg.add(('is_entity/gb.inf', entity))
        if self.entities[entity]['actor']:
            self.hg.add(('is_actor/gb.inf', entity))
        self.hg.set_attribute(entity, 'h_mentions', self.entities[entity]['h_mentions'])
        self.hg.set_attribute(entity, 'total_mentions', self.entities[entity]['total_mentions'])
        self.hg.set_attribute(entity, 'h_mentioned_by', self.entities[entity]['h_mentioned_by'])
        self.hg.set_attribute(entity, 'total_mentioned_by', self.entities[entity]['total_mentioned_by'])
        self.hg.set_attribute(entity, 'h_conflict_to', self.entities[entity]['h_conflict_to'])
        self.hg.set_attribute(entity, 'total_conflict_to', self.entities[entity]['total_conflict_to'])
        self.hg.set_attribute(entity, 'h_conflict_from', self.entities[entity]['h_conflict_from'])
        self.hg.set_attribute(entity, 'total_conflict_from', self.entities[entity]['total_conflict_from'])
        self.hg.set_attribute(entity, 'h_conflict', self.entities[entity]['h_conflict'])
        self.hg.set_attribute(entity, 'total_conflict', self.entities[entity]['total_conflict'])
        self.hg.set_attribute(entity, 'h_conflict_over', self.entities[entity]['h_conflict_over'])
        self.hg.set_attribute(entity, 'total_conflict_over', self.entities[entity]['total_conflict_over'])
        self.hg.set_attribute(entity, 'h_conflict_for', self.entities[entity]['h_conflict_for'])
        self.hg.set_attribute(entity, 'total_conflict_for', self.entities[entity]['total_conflict_for'])

    def compute_metrics(self):
        i = 0
        print('compute metrics...')
        with progressbar.ProgressBar(max_value=len(self.entities)) as bar:
            for entity in self.entities:
                h, total = herfindhal_and_total(self.entities[entity]['mentions'])
                self.entities[entity]['h_mentions'] = h
                self.entities[entity]['total_mentions'] = total

                h, total = herfindhal_and_total(self.entities[entity]['mentioned_by'])
                self.entities[entity]['h_mentioned_by'] = h
                self.entities[entity]['total_mentioned_by'] = total

                h, total = herfindhal_and_total(self.entities[entity]['conflict_to'])
                self.entities[entity]['h_conflict_to'] = h
                self.entities[entity]['total_conflict_to'] = total

                h, total = herfindhal_and_total(self.entities[entity]['conflict_from'])
                self.entities[entity]['h_conflict_from'] = h
                self.entities[entity]['total_conflict_from'] = total

                h, total = herfindhal_and_total(self.entities[entity]['conflict_to']
                                                + self.entities[entity]['conflict_from'])
                self.entities[entity]['h_conflict'] = h
                self.entities[entity]['total_conflict'] = total

                h, total = herfindhal_and_total(self.entities[entity]['conflict_over'])
                self.entities[entity]['h_conflict_over'] = h
                self.entities[entity]['total_conflict_over'] = total

                h, total = herfindhal_and_total(self.entities[entity]['conflict_for'])
                self.entities[entity]['h_conflict_for'] = h
                self.entities[entity]['total_conflict_for'] = total

                self.write_metrics(entity)

                i += 1
                bar.update(i)

    def process(self):
        self.find_actors()
        self.infer()
        self.compute_metrics()


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
