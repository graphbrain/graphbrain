from graphbrain.agents.agent import Agent
from graphbrain.agents.system import wrap_edge


def paragraphs(file_name):
    with open(file_name, 'r') as f:
        for line in f.readlines():
            paragraph = line.strip()
            if len(paragraph) > 0:
                yield paragraph


class TxtParser(Agent):
    def __init__(self):
        super().__init__()
        self.edges = 0

    def name(self):
        return 'txt_parser'

    def on_start(self):
        if not self.sequence:
            raise RuntimeError('Sequence name must be specified.')

    def run(self):
        infile = self.system.get_infile(self)
        parser = self.system.get_parser(self)
        sequence = self.system.get_sequence(self)

        pos = 0
        for paragraph in paragraphs(infile):
            parse_results = parser.parse(paragraph)
            for parse in parse_results['parses']:
                main_edge = parse['resolved_corefs']

                # add main edge
                if main_edge:
                    # attach text to edge
                    text = parse['text']
                    attr = {'text': text}

                    yield wrap_edge(main_edge, sequence=sequence, position=pos,
                                    attributes=attr)
                    self.edges += 1
                    pos += 1

                    # add extra edges
                    for edge in parse['extra_edges']:
                        yield wrap_edge(edge)
            for edge in parse_results['inferred_edges']:
                print('inferred edge: {}'.format(edge.to_str()))
                yield wrap_edge(edge, count=True)

    def report(self):
        rep_str = ('edges found: {}'.format(self.edges))
        return '{}\n\n{}'.format(rep_str, super().report())
