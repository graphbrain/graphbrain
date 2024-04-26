import argparse
import json
import logging
import sys

from termcolor import colored

import graphbrain.constants as const
from graphbrain import hgraph, hedge
from graphbrain.learner.learner import Learner
from graphbrain.parsers import parser_lang
from graphbrain.processors.actors import Actors
from graphbrain.processors.claims import Claims
from graphbrain.processors.conflicts import Conflicts
from graphbrain.processors.names import CorefsNames
from graphbrain.processors.number import Number
from graphbrain.processors.onto import CorefsOnto
from graphbrain.processors.taxonomy import Taxonomy
from graphbrain.readers.txt import TxtReader
from graphbrain.readers.dir import DirReader
from graphbrain.readers.csv import CsvReader
from graphbrain.readers.wikipedia import WikipediaReader
from graphbrain.readers.reddit import RedditReader
from graphbrain.readers.url import URLReader


def _show_logo():
    for line in const.ascii_logo.split('\n'):
        print(colored(line[:29], 'cyan'), end='')
        print(colored(line[29:], 'green'))
    print()


def error_msg(msg):
    print('{} {}'.format(colored('error:', 'red'), msg))


def cli():
    _show_logo()

    parser = argparse.ArgumentParser()

    parser.add_argument('command', type=str, help='command to execute')
    parser.add_argument('--classdir', type=str, help='classifiers directory', default='classifiers')
    parser.add_argument('--col', type=str, help='table column', default=None)
    parser.add_argument('--corefs', help='perform coreference resolution', action='store_true')
    parser.add_argument('--hg', type=str, help='hypergraph db', default='gb.db')
    parser.add_argument('--host', type=str, help='host IP address', default='')
    parser.add_argument('--indir', type=str, help='input directory', default=None)
    parser.add_argument('--infile', type=str, help='input file', default=None)
    parser.add_argument('--infsrcs', help='add inference sources to hypergraph', action='store_true')
    parser.add_argument('--lang', type=str, help='language', default=None)
    parser.add_argument('--outdir', type=str, help='output directory')
    parser.add_argument('--outfile', type=str, help='output file', default=None)
    parser.add_argument('--parser', type=str, help='parser', default=None)
    parser.add_argument('--sequence', type=str, help='sequence name', default=None)
    parser.add_argument('--url', type=str, help='url', default=None)

    args = parser.parse_args()

    # init logging
    logging.basicConfig(stream=sys.stderr, level=logging.INFO)

    # determine language
    if args.parser:
        plang = parser_lang(args.parser)
        if args.lang:
            if args.lang != plang:
                msg = 'specified language ({}) and parser language ({}) do not match'.format(args.lang, plang)
                error_msg(msg)
                sys.exit(-1)
        else:
            args.lang = plang
    # if not lang or parser is specified, default to 'en'
    elif not args.lang:
        args.lang = 'en'

    print(colored('{}\n'.format('command: {}'.format(args.command)), 'white'))

    if args.classdir != 'classifiers':
        print('classifiers directory: {}'.format(args.classdir))
    if args.col:
        print('column: {}'.format(args.col))
    if args.corefs:
        print('coreferences: {}'.format(args.corefs))
    if args.hg:
        print('hypergraph: {}'.format(args.hg))
    if args.host != '':
        print('host: {}'.format(args.host))
    if args.indir:
        print('input directory: {}'.format(args.indir))
    if args.infile:
        print('input file: {}'.format(args.infile))
    if args.infsrcs:
        print('add inference sources: {}'.format(args.infsrcs))
    if args.lang:
        print('language: {}'.format(args.lang))
    if args.outdir:
        print('output directory: {}'.format(args.outdir))
    if args.outfile:
        print('output file: {}'.format(args.outfile))
    if args.parser:
        print('parser: {}'.format(args.parser))
    if args.sequence:
        print('sequence: {}'.format(args.sequence))
    if args.url:
        print('url: {}'.format(args.url))

    print()

    if args.command == 'create':
        hgraph(args.hg)
        print('Hypergraph database created.')
    elif args.command == 'export':
        print('exporting hypergraph...')
        hg = hgraph(args.hg)
        n = 0
        with open(args.outfile, 'w') as f:
            for edge, attributes in hg.all_attributes():
                row = [str(edge), attributes]
                f.write('{}\n'.format(json.dumps(row, ensure_ascii=False)))
                n += 1
        print('{} edges exported.'.format(n))
    elif args.command == 'import':
        print('importing hypergraph...')
        hg = hgraph(args.hg)
        n = 0
        with open(args.infile, 'r') as f:
            for line in f:
                edge_str, attributes = json.loads(line)
                hg.add_with_attributes(hedge(edge_str), attributes)
                n += 1
        print('{} edges imported.'.format(n))
    elif args.command == 'txt':
        TxtReader(args.infile,
                  hg=hgraph(args.hg),
                  sequence=args.sequence,
                  lang=args.lang,
                  corefs=args.corefs,
                  infsrcs=args.infsrcs).read()
    elif args.command == 'url':
        URLReader(args.url,
                  hg=hgraph(args.hg),
                  sequence=args.sequence,
                  lang=args.lang,
                  corefs=args.corefs,
                  infsrcs=args.infsrcs,
                  outfile=args.outfile).read()
    elif args.command == 'dir':
        DirReader(args.indir,
                  hg=hgraph(args.hg),
                  sequence=args.sequence,
                  lang=args.lang,
                  corefs=args.corefs,
                  infsrcs=args.infsrcs).read()
    elif args.command == 'csv':
        CsvReader(args.infile,
                  args.col,
                  hg=hgraph(args.hg),
                  sequence=args.sequence,
                  lang=args.lang,
                  corefs=args.corefs,
                  infsrcs=args.infsrcs).read()
    elif args.command == 'wikipedia':
        WikipediaReader(args.url,
                        hg=hgraph(args.hg),
                        sequence=args.sequence,
                        lang=args.lang,
                        corefs=args.corefs,
                        infsrcs=args.infsrcs).read()
    elif args.command == 'reddit':
        RedditReader(args.infile,
                     hg=hgraph(args.hg),
                     sequence=args.sequence,
                     lang=args.lang,
                     corefs=args.corefs,
                     infsrcs=args.infsrcs).read()
    elif args.command == 'actors':
        Actors(hg=hgraph(args.hg), sequence=args.sequence).run()
    elif args.command == 'claims':
        Claims(hg=hgraph(args.hg), sequence=args.sequence).run()
    elif args.command == 'conflicts':
        Conflicts(hg=hgraph(args.hg), sequence=args.sequence).run()
    elif args.command == 'names':
        CorefsNames(hg=hgraph(args.hg), sequence=args.sequence).run()
    elif args.command == 'number':
        Number(hg=hgraph(args.hg), sequence=args.sequence).run()
    elif args.command == 'onto':
        CorefsOnto(hg=hgraph(args.hg), sequence=args.sequence).run()
    elif args.command == 'taxonomy':
        Taxonomy(hg=hgraph(args.hg), sequence=args.sequence).run()
    elif args.command == 'web':
        from graphbrain.web import app

        learner = Learner(args.hg, args.classdir)
        app.config['LEARNER'] = learner
        app.config['HG'] = args.hg
        # Set the secret key to some random bytes. Doesn't really matter at this point.
        app.secret_key = b'_5#y2L"F4Q8z\n\xec]/'
        if args.host == '':
            app.run()
        else:
            app.run(host=args.host)
    elif args.command == 'generate_datasets':
        learner = Learner(args.hg, args.classdir)
        learner.generate_datasets(args.outdir)
    else:
        raise RuntimeError('Unknown command: {}'.format(args.command))

    print()
