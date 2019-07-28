import progressbar


class Agent(object):
    def __init__(self, hg):
        self.hg = hg
        self.edges_added = 0

    def name(self):
        raise NotImplementedError()

    def languages(self):
        raise NotImplementedError()

    def input_edge(self, edge):
        raise NotImplementedError()

    def start(self):
        pass

    def end(self):
        pass

    def start_agent(self):
        self.edges_added = 0

    def add(self, edge, primary=True):
        self.edges_added += 1
        return self.hg.add(edge, primary=primary)

    def input(self):
        edge_count = self.hg.edge_count()
        i = 0
        with progressbar.ProgressBar(max_value=edge_count) as bar:
            for edge in self.hg.all():
                self.input_edge(edge)
                if i < edge_count:
                    i += 1
                    bar.update(i)

    def report(self):
        return '{} hyperedges were added.'.format(str(self.edges_added))

    def run(self):
        print('running agent: "{}"'.format(self.name()))
        self.start_agent()
        self.start()
        self.input()
        self.end()
        print(self.report())
