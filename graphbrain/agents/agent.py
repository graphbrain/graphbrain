import progressbar


class Agent(object):
    def __init__(self, hg):
        self.hg = hg
        self.search_pattern = '*'
        self.edges_added = 0
        self.edges_existed = 0

    def name(self):
        raise NotImplementedError()

    def languages(self):
        raise NotImplementedError()

    def input_edge(self, edge):
        raise NotImplementedError()

    def input_file(self, file_name):
        raise NotImplementedError()

    def start(self):
        pass

    def end(self):
        pass

    def start_agent(self):
        self.edges_added = 0
        self.edges_existed = 0

    def add(self, edge, primary=True):
        if self.hg.exists(edge):
            self.edges_existed += 1
        else:
            self.edges_added += 1
            return self.hg.add(edge, primary=primary)

    def input(self):
        edge_count = self.hg.search_count(self.search_pattern)
        i = 0
        with progressbar.ProgressBar(max_value=edge_count) as bar:
            for edge in self.hg.search(self.search_pattern):
                self.input_edge(edge)
                if i < edge_count:
                    i += 1
                    bar.update(i)

    def report(self):
        added_s = '{} edges were added.'.format(str(self.edges_added))
        existed_s = '{} edges already existed.'.format(str(self.edges_existed))
        return '{}\n{}'.format(added_s, existed_s)

    def run(self, infile=None):
        print('running agent: "{}"'.format(self.name()))
        self.start_agent()
        self.start()
        if infile:
            self.input_file(infile)
        else:
            self.input()
        self.end()
        print(self.report())
