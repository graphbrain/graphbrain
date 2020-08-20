import progressbar


class Agent(object):
    """Base class for Graphbrain cognitive agents.

    These agents perform some change to an hypergraph, either by processing
    some external source of information of by inferring new knowledge from
    what is already contained in the hypregraph.
    """

    def __init__(self):
        self.search_pattern = '*'
        self.edges_added = 0
        self.edges_existed = 0
        self.system = None
        self.running = False

    def name(self):
        """Returns the agent's name."""
        raise NotImplementedError()

    def languages(self):
        """Returns set of languages supported by the agent, or an empty set
        if the agent is language-agnostic.
        """
        return set()

    def input_edge(self, edge):
        """Feeds the agent an edge to process."""
        raise NotImplementedError()

    def on_start(self):
        """Called before a cycle of activity is started."""

    def on_end(self):
        """Called at the end of a cycle of activity."""

    def _reset_counters(self):
        self.edges_added = 0
        self.edges_existed = 0

    def input(self):
        """Input to the agent all the edges corresponding to its current
        search pattern. A typical use is in knowledge inference agents, which
        infer new knowledge from edges already present in the hypergraph.
        """
        edge_count = self.system.hg.count(self.search_pattern)
        i = 0
        with progressbar.ProgressBar(max_value=edge_count) as bar:
            for edge in self.system.hg.search(self.search_pattern):
                for wedge in self.input_edge(edge):
                    yield wedge
                if i < edge_count:
                    i += 1
                    bar.update(i)

    def report(self):
        """Produce a report of the agent's activities."""
        added_s = '{} edges were added.'.format(str(self.edges_added))
        existed_s = '{} edges already existed.'.format(str(self.edges_existed))
        return '{}\n{}'.format(added_s, existed_s)

    def run(self):
        """High-level method to run an agent."""
        for wedge in self.input():
            yield wedge
