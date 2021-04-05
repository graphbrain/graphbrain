from graphbrain import hgraph
from graphbrain.cognition.system import run_agent
from graphbrain.cognition.system import run_system


def run(args):
    if args.agent:
        run_agent(args.agent,
                  lang=args.lang,
                  parser_class=args.parser,
                  hg=hgraph(args.hg),
                  infile=args.infile,
                  indir=args.indir,
                  url=args.url,
                  sequence=args.sequence,
                  corefs=args.corefs)
    elif args.system:
        run_system(args.system,
                   lang=args.lang,
                   parser_class=args.parser,
                   hg=hgraph(args.hg),
                   infile=args.infile,
                   indir=args.indir,
                   url=args.url,
                   sequence=args.sequence,
                   corefs=args.corefs)
    else:
        raise RuntimeError('Either agent or system must be specified.')
