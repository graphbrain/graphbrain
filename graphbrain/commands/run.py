from graphbrain import hgraph
from graphbrain.agents.system import run_agent


def run(args):
    run_agent(args.agent,
              lang=args.lang,
              hg=hgraph(args.hg),
              infile=args.infile,
              sequence=args.sequence)
