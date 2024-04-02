import json
import logging
import os
import random
from collections import Counter

import editdistance

from graphbrain import hgraph, hedge
from graphbrain.learner.classifier import Classifier
from graphbrain.learner.classifier import from_file as classifier_from_file
from graphbrain.patterns import contains_variable, match_pattern


def relax_pattern(edge):
    argroles = edge.argroles()
    # try to remove arguments that do not contain a variable
    if argroles != '' and edge.mt in {'R', 'C'}:
        if argroles[0] == '{':
            cur_argroles = argroles[1:-1]
        else:
            cur_argroles = argroles
        new_argroles = ''
        new_arguments = []
        while new_argroles == '':
            for argrole, argument in zip(cur_argroles, edge[1:]):
                if contains_variable(argument) or random.random() >= .2:
                    new_argroles += argrole
                    new_arguments.append(argument)
        edge = hedge([edge[0].replace_argroles(new_argroles)] + new_arguments)
    # try to replace entire hyperedge with wildcard
    if not contains_variable(edge) and random.random() < .2:
        mt = edge.mt
        # if it has argument roles, preserve them
        if argroles != '' and edge.mt in {'P', 'B'}:
            return hedge('*/{}.{}'.format(mt, argroles))
        return hedge('*')
    # otherwise if it is an atom, just return it
    elif edge.atom:
        return edge
    # otherwise recure through subedges
    else:
        conn_str = str(edge[0])
        if conn_str == 'var':
            return hedge((edge[0], relax_pattern(edge[1]), edge[2]))
        elif conn_str in {'lemma', 'atoms'}:
            return hedge([edge[0]] + [relax_pattern(subedge) for subedge in edge[1:]])
        else:
            return hedge([relax_pattern(subedge) for subedge in edge])


class Learner:
    def __init__(self, hg_path, dir_path):
        self.hg_path = hg_path
        self.dir_path = dir_path
        self.classifiers = {}
        self.edge_strs = []
        self.str2edge = {}
        self.hg = hgraph(self.hg_path)

        # load classifiers
        for root, _, files in os.walk(self.dir_path):
            for file in files:
                if file.endswith('.json'):
                    classifier_name = file.split('.')[0]
                    file_path = os.path.join(root, file)
                    self.classifiers[classifier_name] = classifier_from_file(file_path, hg=self.hg)
                    
        self.edges_path = '{}.edges'.format(
            self.hg_path.split('/')[-1].split('.')[0])

        if not os.path.isfile(self.edges_path):
            print('edges file not found, creating it...')
            with open(self.edges_path, 'wt') as f:
                for seq in self.hg.sequences():
                    for edge in self.hg.sequence(seq):
                        f.write('{}\n'.format(str(edge)))

        print('loading edges...')
        with open(self.edges_path, 'rt') as f:
            for line in f:
                # self.edges.append(hedge(line))
                self.edge_strs.append(line)
        random.shuffle(self.edge_strs)
        print('{} edges found.'.format(len(self.edge_strs)))

    def new_classifier(self, name):
        if name not in self.classifiers:
            file_path = '{}/{}.json'.format(self.dir_path, name)
            classifier = Classifier(file_path=file_path, hg=self.hg)
            classifier.save()
            self.classifiers[name] = classifier

    def get_edge(self, edge_str):
        if edge_str not in self.str2edge:
            self.str2edge[edge_str] = hedge(edge_str)
        return self.str2edge[edge_str]

    def select_edge(self):
        return self.get_edge(random.choice(self.edge_strs))

    def select_edge_predicates(self, predicate, count=1, max_tries=10000):
        edges = []
        tries = 0
        while True:
            if tries >= max_tries:
                break
            tries += 1
            edge = self.select_edge()
            if edge.cmt == 'P':
                pred_atom = edge.connector_atom()
                if pred_atom and pred_atom.root() == predicate:
                    edges.append(edge)
                    if len(edges) >= count:
                        return edges
        return edges

    def sample_edge(self, cls_name, max_tries=10000):
        cls = self.classifiers[cls_name]

        positive_rules = [rule for rule in cls.rules if rule.positive]
        if len(positive_rules) == 0:
            return self.select_edge()            

        positive = random.choice([True, False])
        target_rule = random.randint(1, len(positive_rules))
        tries = 0
        while tries < max_tries:
            edge = self.get_edge(random.choice(self.edge_strs))
            rule_triggered = cls.rule_triggered(edge)
            if positive:
                # look for edge that triggers a randomly seleceted rule
                if rule_triggered == target_rule:
                    return edge
            else:
                # look for a edge that is classified negatively
                if rule_triggered <= 0:
                    return edge
            tries += 1

        # give up...
        return self.get_edge(random.choice(self.edge_strs))

    def generate_case(self, cls_name, edge=None, pattern=None, predicate=None, max_tries=10000):
        if edge is None:
            if predicate:
                edge = self.select_edge_predicates(predicate)[0]
            else:
                edge = self.sample_edge(cls_name, max_tries=max_tries)
        if pattern:
            matches = match_pattern(edge, pattern, hg=self.hg)
        else:
            matches = self.classifiers[cls_name].classify(edge)
        positive = False
        if matches:
            # is pattern was provided, this is not a real match but an exploration (user might decide it's positive)
            positive = pattern is None
            variables = matches[0]
        else:
            variables = self.classifiers[cls_name].suggest_variables()
        variables = list(enumerate(variables.items()))
        nvariables = 0
        case = {
            'edge': edge,
            'text': self.hg.get_str_attribute(edge, 'text'),
            'positive': positive,
            'variables': variables,
            'nvariables': nvariables
        }
        return case

    def generate_expansion_case(self, pattern, max_tries=10000):
        logging.debug('Learner.generate_expansion_case()')
        logging.debug('Expansion pattern: {}'.format(pattern))
        while True:
            relax = relax_pattern(pattern)
            logging.debug('Generated relaxed pattern: {}'.format(relax))
            tries = 0
            while tries < max_tries:
                edge = self.select_edge()
                base_matches = match_pattern(edge, pattern, hg=self.hg)
                new_matches = match_pattern(edge, relax, hg=self.hg)
                # check if cases matches the relaxes pattern but not the original one (expansion)
                if len(base_matches) == 0 and len(new_matches) > 0:
                    logging.debug('Expansion case found: {}'.format(edge))
                    return edge, relax
                tries += 1

    def find_matches(self, cls, count, max_tries=10000):
        classifier = self.classifiers[cls]
        indices = [rule.index for rule in classifier.rules if rule.positive]

        matches = []
        matches_counters = {}
        for index in indices:
            matches_counters[index] = 0

        finished = False
        n = 0
        while not finished and n < max_tries:
            n += 1
            edge = self.select_edge()
            match = classifier.classify(edge)
            if match:
                rules_triggered = classifier.rules_triggered(edge)

                if any(matches_counters[index] < count
                       for index in rules_triggered):

                    for index in rules_triggered:
                        matches_counters[index] += 1

                    match = match[0]
                    variables = []
                    for variable in match:
                        variables.append((variable, self.hg.get_str_attribute(match[variable], 'text')))
                    matches.append(
                        {'edge': edge,
                         'text': self.hg.get_str_attribute(edge, 'text'),
                         'rules_triggered': rules_triggered,
                         'variables': variables})

                    finished = all(counter >= count for counter in matches_counters.values())

        ratio = len(matches) / n
        est_total_matches = int(len(self.edge_strs) * ratio)
        percentage = ratio * 100

        random.shuffle(matches)
        return matches, percentage, est_total_matches

    def top_predicates(self):
        predicates = Counter()
        for edge_str in self.edge_strs[:10000]:
            edge = self.get_edge(edge_str)
            if edge.not_atom and edge.cmt == 'P':
                predicate = edge.connector_atom()
                predicates[predicate.root()] += 1
        return predicates.most_common()

    def _text2subedge(self, edge, text):
        best_edge = edge
        input_text = text.lower()
        edge_txt = self.hg.get_str_attribute(edge, 'text').strip().lower()
        best_distance = editdistance.eval(edge_txt, input_text)
        best_length = len(edge_txt)

        if edge.not_atom:
            for subedge in edge:
                sedge, distance, length = self._text2subedge(subedge, input_text)
                if distance < best_distance or (distance == best_distance and length < best_length):
                    best_edge = sedge
                    best_distance = distance
                    best_length = length
        
        return best_edge, best_distance, best_length

    def text2subedge(self, edge, text):
        subedge, _, _ = self._text2subedge(edge, text)
        return subedge

    def generate_datasets(self, outdir):
        for classifier_name, classifier in self.classifiers.items():
            logging.debug('Generating dataset for classifier: {}'.format(classifier_name))
            file_path = '{}/{}-dataset.jsonl'.format(outdir, classifier_name)
            with open(file_path, 'wt') as f:
                n = 0
                for edge_str in self.edge_strs:
                    edge = hedge(edge_str)
                    text = self.hg.text(edge)
                    matches = classifier.classify(edge)
                    for match in matches:
                        for variable in match:
                            match_edge = match[variable]
                            match_text = self.hg.text(match_edge)
                            match[variable] = (match_text, str(match_edge))
                    positive = len(matches) > 0
                    case = {'text': text, 'positive': positive, 'edge': str(edge), 'matches': matches}
                    f.write('{}\n'.format(json.dumps(case)))
                    n += 1
                    if n % 10000 == 0:
                        logging.debug('{} cases wrote'.format(n))
