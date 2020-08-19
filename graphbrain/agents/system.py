from collections import defaultdict


def wrap_edge(edge, primary=True, count=False):
    return {'edge': edge,
            'primary': primary,
            'count': count}


class System(object):
    def __init__(self, infile=None):
        self.infile = infile
        self.agents = {}
        self.outputs = defaultdict(set)
        self.dependants = defaultdict(set)
        self.roots = set()
        self.agent_seq = []

    def add_agent(self, name, agent, input=None, depends_on=None):
        self.agents[name] = agent
        if input:
            self.outputs[input].add(name)
        if depends_on:
            self.dependants[depends_on].add(name)
        if not (input and depends_on):
            self.roots.add(name)

    def run(self):
        # start by running the roots
        for root in self.roots:
            self._run_agent(root)
        # terminate all agents
        for agent in self.agent_seq:
            agent.end()
            print(agent.report())

    def _start_agent(self, agent):
        if agent not in self.agent_seq:
            self.agent_seq.append(agent)
            print('starting agent: {}'.format(agent.name()))
            agent._reset_counters()
            agent.start()

    def _add_edge(self, edge, primary=True, count=False):
        if self.hg.exists(edge):
            self.edges_existed += 1
            if count:
                return self.hg.add(edge, primary=primary, count=True)
        else:
            self.edges_added += 1
            return self.hg.add(edge, primary=primary, count=count)

    def _process_wedge(self, agent_name, wedge):
        edge = wedge['edge']
        primary = wedge['primary']
        count = wedge['count']
        self._add_edge(edge, primary=primary, count=count)
        for output in self.outputs[agent_name]:
            self._start_agent(output)
            output.input_edge(edge)

    def _run_agent(self, agent_name):
        agent = self.agents[agent_name]
        self._start_agent(agent)

        for wedge in agent.run(infile=self.infile):
            self._process_wedge(agent_name, wedge)

        for dependant in self.dependants[agent_name]:
            self._run_agent(dependant)
