class Extractor:
    def __init__(self, name, classes, debug=False):
        self.name = name
        self.classes = classes
        self._debug = debug

    def extract(self, edge):
        return set()

    def debug(self, msg):
        if self._debug:
            print('[{} debug] {}'.format(self.name, msg))
    
    def test(self, tests):
        results = {}
        for edge in tests.cases:
            test_classes = tests.cases[edge]
            pred_classes = self.extract(edge)
            for cla in test_classes | pred_classes:
                if cla not in results:
                    results[cla] = {'false-positives': [],
                                    'false-negatives': [],
                                    'correct': []}
                if cla in test_classes:
                    if cla in pred_classes:
                        results[cla]['correct'].append(edge)
                    else:
                        results[cla]['false-negatives'].append(edge)
                else:
                    if cla in pred_classes:
                        results[cla]['false-positives'].append(edge)
                    else:
                        results[cla]['correct'].append(edge)
        return results
    
    def test_report(self, tests):
        results = self.test(tests)
        lines = []
        for cla in results:
            fp = len(results[cla]['false-positives'])
            fn = len(results[cla]['false-negatives'])
            cor = len(results[cla]['correct'])
            total = cor + fn + fp
            lines.append('{} {}/{} {}/{} {}/{}'.format(
                cla, fp, total, fn, total, cor, total))
        return '\n'.join(lines)
    
    def error_report(self, tests):
        results = self.test(tests)
        lines = []
        for cla in results:
            lines.append('CLASS {}'.format(cla))
            lines.append('\n* False Positives ({})'.format(
                len(results[cla]['false-positives'])))
            for edge in results[cla]['false-positives']:
                lines.append(str(edge))
            lines.append('\n* False Negatives ({})'.format(
                len(results[cla]['false-negatives'])))
            for edge in results[cla]['false-negatives']:
                lines.append(str(edge))
            lines.append('')
        return '\n'.join(lines)
