import json
import logging
from collections import defaultdict
from importlib import import_module

from graphbrain.cognition.agent import Agent
from graphbrain.op import apply_op
from graphbrain.parsers import create_parser


def run_agent(agent, lang=None, hg=None, infile=None, indir=None, url=None,
              sequence=None, progress_bar=True, logging_level=logging.INFO):
    system = System(lang=lang, hg=hg, infile=infile, indir=indir, url=url,
                    sequence=sequence, logging_level=logging_level)
    if isinstance(agent, Agent):
        agent_obj = agent
    else:
        agent_obj = create_agent(
            agent, progress_bar=progress_bar, logging_level=logging_level)
    system.add(agent_obj)
    system.run()


def load_system(system_file, lang=None, hg=None, infile=None, indir=None,
                url=None, sequence=None, progress_bar=True,
                logging_level=logging.INFO):
    with open(system_file, 'r') as f:
        json_str = f.read()
    system_json = json.loads(json_str)
    system = System(name=system_file, lang=lang, hg=hg, infile=infile,
                    indir=indir, url=url, sequence=sequence,
                    logging_level=logging_level)
    for agent_name in system_json:
        module_str = system_json[agent_name]['agent']
        depends_on = None
        input = None
        write = True
        if 'depends_on' in system_json[agent_name]:
            depends_on = system_json[agent_name]['depends_on']
        if 'input' in system_json[agent_name]:
            input = system_json[agent_name]['input']
        if 'write' in system_json[agent_name]:
            write = system_json[agent_name]['write']
        agent = create_agent(module_str, name=agent_name,
                             progress_bar=progress_bar,
                             logging_level=logging_level)
        system.add(agent, input=input, depends_on=depends_on, write=write)
    return system


def run_system(system_file, lang=None, hg=None, infile=None, indir=None,
               url=None, sequence=None, progress_bar=True,
               logging_level=logging.INFO):
    system = load_system(system_file, lang=lang, hg=hg, infile=infile,
                         indir=indir, url=url, sequence=sequence,
                         progress_bar=progress_bar,
                         logging_level=logging_level)
    system.run()


def create_agent(agent_module_str, name=None,
                 progress_bar=True, logging_level=logging.INFO):
    if '.' in agent_module_str:
        module_str = agent_module_str
    else:
        module_str = 'graphbrain.cognition.agents.{}'.format(agent_module_str)
    class_name_parts = module_str.split('.')[-1].split('_')
    class_name = ''.join([part.title() for part in class_name_parts])
    class_obj = getattr(import_module(module_str), class_name)

    agent_name = name if name else agent_module_str

    return class_obj(
        agent_name, progress_bar=progress_bar, logging_level=logging_level)


def processor(x, lang=None, hg=None, infile=None, indir=None, url=None,
              sequence=None):
    if type(x) == str:
        if x[-4:] == '.sys':
            return load_system(x, lang=lang, hg=hg, infile=infile, indir=indir,
                               url=url, sequence=sequence)
        else:
            system = System(lang=lang, hg=hg, infile=infile, indir=indir,
                            url=url, sequence=sequence)
            agent = create_agent(x, progress_bar=False)
            system.add(agent)
            return system
    elif isinstance(x, Agent):
        system = System(lang=lang, hg=hg, infile=infile, indir=indir, url=url,
                        sequence=sequence)
        system.add(x)
        return system
    elif isinstance(x, System):
        if lang:
            x.lang = lang
        if hg:
            x.hg = hg
        if infile:
            x.infile = infile
        if url:
            x.url = url
        if sequence:
            x.sequence = sequence
        return x
    else:
        raise RuntimeError('Trying to create processor with invalid argument.')


class System(object):
    def __init__(self, name=None, lang=None, hg=None, infile=None, indir=None,
                 url=None, sequence=None, logging_level=logging.INFO):
        self.name = name
        self.lang = lang
        self.hg = hg
        self.infile = infile
        self.indir = indir
        self.url = url
        self.sequence = sequence

        self.logger = logging.getLogger('agent_system')
        self.logger.setLevel(logging_level)

        self.agents = {}
        self.outputs = defaultdict(set)
        self.dependants = defaultdict(set)
        self.roots = set()
        self.agent_seq = []
        self.parser = None
        self.counters = {}
        self.write = {}

    def add(self, agent, input=None, depends_on=None, write=True):
        agent.system = self
        self.agents[agent.name] = agent
        if input:
            self.outputs[input].add(agent.name)
        if depends_on:
            self.dependants[depends_on].add(agent.name)
        # if agent has no inputs and depends on no other agents, then it is
        # a root agent
        if not (input or depends_on):
            self.roots.add(agent.name)
        self.write[agent.name] = write

    def _end(self):
        # terminate all agents
        for agent_name in self.agent_seq:
            agent = self.agents[agent_name]
            self.logger.info('\nstopping agent "{}"...'.format(agent_name))
            if agent.running:
                for op in agent.on_end():
                    self._process_op(agent_name, op)
            self.logger.info('[*] agent "{}" stopped.'.format(agent_name))
            self.logger.info('{} edges were added.'.format(
                self.counters[agent_name][0]))
            self.logger.info('{} edges already existed.'.format(
                self.counters[agent_name][1]))
            report = agent.report()
            if len(report) > 0:
                self.logger.info(report)

    def run(self):
        # start by running the roots
        for root in self.roots:
            self._run_agent(root)

        self._end()

        if self.name:
            self.logger.info('\nsystem "{}" stopped.'.format(self.name))

    def _process(self, agent_name, edge):
        agent = self._start_agent(agent_name)

        ops = agent.input_edge(edge)
        if ops:
            for op in ops:
                opedge = op['edge']
                if agent_name in self.outputs:
                    for output in self.outputs[agent_name]:
                        for outedge in self._process(output, opedge):
                            yield outedge
                else:
                    yield opedge

    def process(self, edge):
        for root in self.roots:
            for edge in self._process(root, edge):
                yield edge

    def get_parser(self, agent):
        if self.parser is None:
            self.parser = create_parser(
                name=self.lang, lemmas=True, resolve_corefs=True)
        return self.parser

    def get_infile(self, agent):
        return self.infile

    def get_indir(self, agent):
        return self.indir

    def get_url(self, agent):
        return self.url

    def get_hg(self, agent):
        return self.hg

    def get_sequence(self, agent):
        return self.sequence

    def _reset_counters(self, agent_name):
        self.counters[agent_name] = [0, 0]

    def _start_agent(self, agent_name):
        agent = self.agents[agent_name]
        if not agent.running:
            self.agent_seq.append(agent_name)
            self.logger.info('\n[>] agent "{}" started.'.format(agent.name))
            self._reset_counters(agent_name)
            agent.on_start()
            agent.running = True
        return agent

    def _process_op(self, agent_name, op):
        if self.write[agent_name]:
            if apply_op(self.hg, op):
                self.counters[agent_name][0] += 1
            else:
                self.counters[agent_name][1] += 1
        for output in self.outputs[agent_name]:
            self._start_agent(output)
            downstream_ops = self.agents[output].input_edge(op['edge'])
            if downstream_ops:
                for dop in downstream_ops:
                    self._process_op(output, dop)

    def _run_agent(self, agent_name):
        agent = self.agents[agent_name]
        self._start_agent(agent_name)

        ops = agent.run()
        if ops:
            for op in ops:
                self._process_op(agent_name, op)

        ops = agent.on_end()
        if ops:
            for op in ops:
                self._process_op(agent_name, op)
        agent.running = False

        for dependant in self.dependants[agent_name]:
            self._run_agent(dependant)

    def __enter__(self):
        return self

    def __exit__(self, type, value, traceback):
        if traceback is None:
            self._end()
