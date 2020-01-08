from graphbrain import *
from graphbrain.agents import create_agent


def run(args):
    hg = hgraph(args.hg)
    agent = create_agent(args.agent, hg, args.lang, args.sequence)
    if agent is None:
        print('ERROR: unknown agent: {}'.format(args.agent))
    else:
        agent.run(infile=args.infile)
