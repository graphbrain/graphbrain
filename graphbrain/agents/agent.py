import progressbar


class Agent(object):
    """Base class for Graphbrain cognitive agents.

    These agents perform some change to an hypergraph, either by processing
    some external source of information of by inferring new knowledge from
    what is already contained in the hypregraph.
    """

    def __init__(self, hg):
        self.hg = hg
        self.search_pattern = '*'
        self.edges_added = 0
        self.edges_existed = 0

    def name(self):
        """Returns the agent's name."""
        raise NotImplementedError()

    def languages(self):
        """Returns set of languages supported by the agent, or an empty set
        if the agent is language-agnostic.
        """
        raise NotImplementedError()

    def input_edge(self, edge):
        """Feeds the agent an edge to process."""
        raise NotImplementedError()

    def input_file(self, file_name):
        """Feeds the agent a path to a file to process."""
        raise NotImplementedError()

    def start(self):
        """Tell agent that a new cycle of activity is starting. What this
        means in practice depends on the agent. For example, for a
        knowledge inference agent this could mean one full pass through all
        the edges in the hypergraph. For an agent that extracts knowledge
        from external files, this could mean one pass through an entire
        file.
        """

    def end(self):
        """End the agent's current cycle of activities."""

    def _reset_counters(self):
        self.edges_added = 0
        self.edges_existed = 0

    def add(self, edge, primary=True):
        """Tell agent to add this edge to the hypergraph."""
        if self.hg.exists(edge):
            self.edges_existed += 1
        else:
            self.edges_added += 1
            return self.hg.add(edge, primary=primary)

    def input(self):
        """Input to the agent all the edges corresponding to its current
        search pattern. A typical use is in knowledge inference agents, which
        infer new knowledge from edges already present in the hypergraph.
        """
        edge_count = self.hg.search_count(self.search_pattern)
        i = 0
        with progressbar.ProgressBar(max_value=edge_count) as bar:
            for edge in self.hg.search(self.search_pattern):
                self.input_edge(edge)
                if i < edge_count:
                    i += 1
                    bar.update(i)

    def report(self):
        """Produce a report of the agent's activities."""
        added_s = '{} edges were added.'.format(str(self.edges_added))
        existed_s = '{} edges already existed.'.format(str(self.edges_existed))
        return '{}\n{}'.format(added_s, existed_s)

    def run(self, infile=None):
        """High-level method to run an agent. It will start a cycle of
        activities, provide a full set of inputs, end the cycle of activities
        and print a report.

        Keyword argument:

        infile -- if provided, agent inputs information from a file, otherwise
        it goes through all the edges present in the hypergraph that are
        selected by its internal pattern (defaulting to '*', meaning
        all of them).
        """
        print('running agent: "{}"'.format(self.name()))
        self._reset_counters()
        self.start()
        if infile:
            self.input_file(infile)
        else:
            self.input()
        self.end()
        print(self.report())
