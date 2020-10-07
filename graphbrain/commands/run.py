from graphbrain import hgraph
from graphbrain.cognition.system import run_agent, run_system


def run(args):
    if args.agent:
        run_agent(args.agent,
                  lang=args.lang,
                  hg=hgraph(args.hg),
                  infile=args.infile,
                  url=args.url,
                  sequence=args.sequence)
    elif args.system:
        run_system(args.system,
                   lang=args.lang,
                   hg=hgraph(args.hg),
                   infile=args.infile,
                   url=args.url,
                   sequence=args.sequence)
    else:
        raise RuntimeError('Either agent or system must be specified.')
