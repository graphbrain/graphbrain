import json
import os
from collections import Counter

from graphbrain import hedge


class Tests:
    def __init__(self, name):
        self.name = name
        self.cases = {}
        self.free_edges = []
        self._edge_to_text = {}
        if os.path.exists(self._edge_to_text_path()):
            self._load_edge_to_text()
        if os.path.exists(self._cases_path()):
            self._load_cases()
        self._update_free_edges()
    
    @property
    def edge_to_text(self):
        return self._edge_to_text

    @edge_to_text.setter
    def edge_to_text(self, value):
        self._edge_to_text = value
        self._update_free_edges()
        self._save_edge_to_text()

    def add_case(self, edge, classes):
        self.cases[edge] = classes
        self._update_free_edges()
        self._save_cases()
        
    def info(self):
        top_text = '{} edges; {} cases'.format(
            len(self._edge_to_text), len(self.cases))
        cla_counts = Counter()
        no_classes = 0
        for _, classes in self.cases.items():
            if len(classes) == 0:
                no_classes += 1
            for cla in classes:
                cla_counts[cla] += 1
        cla_text = '\n'.join(
            ['class={}: {}'.format(cla, count)
             for cla, count in cla_counts.most_common()])
        no_cla_text = 'no classes: {}'.format(no_classes)
        return '\n'.join((top_text, cla_text, no_cla_text))
            
    def _update_free_edges(self):
        self.free_edges = list(
            set(self._edge_to_text.keys()) - set(self.cases.keys()))
            
    def _edge_to_text_path(self):
        return 'tests-{}-edges.json'.format(self.name)
    
    def _cases_path(self):
        return 'tests-{}-cases.json'.format(self.name)
    
    def _load_edge_to_text(self):
        with open(self._edge_to_text_path(), 'rt') as f:
            data = json.loads(f.read())
        for sedge, text in data.items():
            self._edge_to_text[hedge(sedge)] = text
    
    def _load_cases(self):
        with open(self._cases_path(), 'rt') as f:
            data = json.loads(f.read())
        for sedge, classes in data.items():
            self.cases[hedge(sedge)] = set(classes)
            
    def _save_edge_to_text(self):
        data = {}
        for edge, text in self._edge_to_text.items():
            data[str(edge)] = text
        with open(self._edge_to_text_path(), 'wt') as f:
            f.write(json.dumps(data))
            
    def _save_cases(self):
        data = {}
        for edge, classes in self.cases.items():
            data[str(edge)] = list(classes)
        with open(self._cases_path(), 'wt') as f:
            f.write(json.dumps(data))
