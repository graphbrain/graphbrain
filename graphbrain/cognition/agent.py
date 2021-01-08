import logging

import progressbar


class Agent(object):
    """Base class for Graphbrain cognitive agents.

    These agents perform some change to an hypergraph, either by processing
    some external source of information of by inferring new knowledge from
    what is already contained in the hypregraph.
    """

    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        self.name = name
        self.progress_bar = progress_bar

        self.logger = logging.getLogger(self.name)
        self.logger.setLevel(logging_level)

        self.search_pattern = '*'
        self.recursive = True

        self.system = None
        self.running = False

    def languages(self):
        """Returns set of languages supported by the agent, or an empty set
        if the agent is language-agnostic.
        """
        return set()

    def process_edge(self, edge, depth):
        """Feeds the agent an edge to process."""
        # do nothing by default
        return None

    def input_edge(self, edge, depth=0):
        """Feeds the agent an edge to process."""
        ops = self.process_edge(edge, depth)
        if ops:
            for op in ops:
                yield op
        # recursive step
        if self.recursive:
            if not edge.is_atom():
                for subedge in edge:
                    for op in self.input_edge(subedge, depth + 1):
                        yield op

    def on_start(self):
        """Called before a cycle of activity is started."""

    def on_end(self):
        """Called at the end of a cycle of activity."""
        return []

    def input(self):
        """Input to the agent all the edges corresponding to its current
        search pattern. A typical use is in knowledge inference agents, which
        infer new knowledge from edges already present in the hypergraph.
        """
        edge_count = self.system.hg.count(self.search_pattern)
        i = 0

        if self.progress_bar:
            pbar = progressbar.ProgressBar(max_value=edge_count).start()
        else:
            pbar = None

        for edge in self.system.hg.search(self.search_pattern):
            ops = self.input_edge(edge)
            if ops:
                for op in ops:
                    yield op
            if i < edge_count:
                i += 1
                if self.progress_bar:
                    pbar.update(i)

        if self.progress_bar:
            pbar.finish()

    def report(self):
        """Produce a report of the agent's activities."""
        return ''

    def run(self):
        """High-level method to run an agent."""
        for op in self.input():
            yield op
