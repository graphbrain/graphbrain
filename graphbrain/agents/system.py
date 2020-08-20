from collections import defaultdict
from graphbrain.parsers import create_parser


def wrap_edge(edge, primary=True, count=False, attributes={}, sequence=None,
              position=-1):
    return {'edge': edge,
            'primary': primary,
            'count': count,
            'attributes': attributes,
            'sequence': sequence,
            'position': position}


class System(object):
    def __init__(self, lang, hg=None, infile=None, sequence=None):
        self.lang = lang
        self.hg = hg
        self.infile = infile
        self.sequence = sequence
        self.agents = {}
        self.outputs = defaultdict(set)
        self.dependants = defaultdict(set)
        self.roots = set()
        self.agent_seq = []
        self.parser = None

    def add_agent(self, name, agent, input=None, depends_on=None):
        agent.system = self
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
        for agent_name in self.agent_seq:
            agent = self.agents[agent_name]
            if agent.running:
                for wedge in agent.on_end():
                    self._process_wedge(agent_name, wedge)
            print(agent.report())

    def get_parser(self, agent):
        if self.parser is None:
            self.parser = create_parser(
                name='en', lemmas=True, resolve_corefs=True)
        return self.parser

    def get_infile(self, agent):
        return self.infile

    def get_hg(self, agent):
        return self.hg

    def _start_agent(self, agent_name):
        agent = self.agents[agent_name]
        if not agent.running:
            self.agent_seq.append(agent_name)
            print('starting agent: {}'.format(agent.name()))
            agent._reset_counters()
            agent.on_start()
            agent.running = True

    def _add_wedge(self, wedge, primary=True, count=False):
        edge = wedge['edge']
        primary = wedge['primary']
        count = wedge['count']
        attributes = wedge['attributes']
        sequence = wedge['sequence']
        if sequence:
            position = wedge['position']
            self.hg.add_to_sequence(sequence, position, edge)
        else:
            if self.hg.exists(edge):
                self.edges_existed += 1
                if count:
                    self.hg.add(edge, primary=primary, count=True)
            else:
                self.edges_added += 1
                self.hg.add(edge, primary=primary, count=count)

        for attribute in attributes:
            self.hg.set_attribute(edge, attribute, attributes[attribute])

    def _process_wedge(self, agent_name, wedge):
        self._add_wedge(wedge)
        for output in self.outputs[agent_name]:
            self._start_agent(output)
            output.input_edge(wedge['edge'])

    def _run_agent(self, agent_name):
        agent = self.agents[agent_name]
        self._start_agent(agent_name)

        for wedge in agent.run():
            self._process_wedge(agent_name, wedge)

        for wedge in agent.on_end():
            self._process_wedge(agent_name, wedge)
        agent.running = False

        for dependant in self.dependants[agent_name]:
            self._run_agent(dependant)
