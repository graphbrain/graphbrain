import random

import ipywidgets as widgets


class InteractiveCaseGenerator:
    def __init__(self, tests, extractor):
        self.tests = tests
        self.extractor = extractor
        self.checkboxes = []
        self.class_to_check = {}
        for cla in extractor.classes:
            cb = widgets.Checkbox(value=False, description=cla, indent=False)
            self.checkboxes.append(cb)
            self.class_to_check[cla] = cb
        self.output = widgets.Output()
        self.edge_classes = {}
        for edge in self.tests.free_edges:
            self.edge_classes[edge] = extractor.extract(edge)
        self.current_case = None
    
    def _class_positive_set(self, cla):
        return [edge for edge in self.tests.free_edges
                if cla in self.edge_classes[edge]]
    
    def propose_case(self, case=None, keywords=None):
        if case:
            edge = case
        elif keywords:
            edges = list(self.tests.cases.keys())
            kw_edges = []
            for edge in edges:
                if any(keyword in self.tests.edge_to_text[edge].lower()
                       for keyword in keywords):
                    kw_edges.append(edge)
            edge = random.choice(kw_edges)
        else:
            edge_list = []
            while len(edge_list) == 0:
                choice = random.randint(0, 1)
                if choice == 0:
                    edge_list = self.tests.free_edges
                elif choice == 1:
                    edge_list = self._class_positive_set(
                        random.choice(list(self.extractor.classes)))
            edge = random.choice(edge_list)
        self.current_case = edge
        classes = self.extractor.extract(edge)
        for cla in self.extractor.classes:
            self.class_to_check[cla].value = cla in classes
        with self.output:
            self.output.clear_output()
            print(self.tests.edge_to_text[edge])
            print()
            print(str(edge))

    def run(self, case=None, keywords=None):
        self.propose_case(case=case, keywords=keywords)
        checks_box = widgets.HBox(self.checkboxes)
        button = widgets.Button(description='Add Case')
        buttons_box = widgets.HBox([button])
        def button_callback(b):
            classes = set(cla for cla in self.extractor.classes
                          if self.class_to_check[cla].value)
            self.tests.add_case(self.current_case, classes)
            self.propose_case(keywords=keywords)
        button.on_click(button_callback)
        return widgets.VBox([self.output, checks_box, buttons_box])
