class ParserOutput(object):
    def __init__(self, sentence, tree):
        self.sentence = sentence
        self.tree = tree
        self.main_edge = None
        self.edges = []
        self.stage_outputs = []