from graphbrain import *
from graphbrain.agents import create_agent


def run(args):
    hg = hypergraph(args.hg)
    agent = create_agent(args.agent, hg)
    agent.run()
