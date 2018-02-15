#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


import logging
import argparse
from termcolor import colored
import gb.constants as const
from gb.hypergraph.hypergraph import HyperGraph
# import gb.importers.wordnet as wn
import gb.importers.wikidata as wd
import gb.importers.dbpedia as dbp
import gb.importers.dbpedia_wordnet as dbpwn
from gb.tools.shell import Shell
from gb.ui.server import start_ui
from gb.retrievers.reddit import RedditRetriever
import gb.reader.reader_tests as rtests
from gb.reader.reddit import RedditReader
import gb.reader.stages.hypergen_case_generator as hypergen_cg
import gb.reader.stages.hypergen as hypergen
import gb.tools.json as json_tools
from gb.filters.filters import AllFilter
import gb.synonyms.synonyms as synonyms
import gb.inference.headlines as hl


def show_logo():
    print(colored(const.ascii_logo, 'cyan'))
    print()


# commands
def create(params):
    print('creating hypergraph...')
    HyperGraph(params)
    print('done.')


# def wordnet(params):
#     print('reading wordnet...')
#     hg = HyperGraph(params)
#     wn.read(hg)
#     print('done.')


def wikidata(params):
    print('reading wikidata...')
    hg = HyperGraph(params)
    infile = params['infile']
    wd.read(hg, infile)
    print('done.')


def dbpedia(params):
    print('reading DBPedia...')
    hg = HyperGraph(params)
    infile = params['infile']
    dbp.read(hg, infile)
    print('done.')


def dbpedia_wordnet(params):
    print('reading DBPedia...')
    hg = HyperGraph(params)
    infile = params['infile']
    dbpwn.read(hg, infile)
    print('done.')


def info(params):
    hg = HyperGraph(params)
    print('symbols: %s' % hg.symbol_count())
    print('edges: %s' % hg.edge_count())
    print('total degree: %s' % hg.total_degree())


def shell(params):
    hg = HyperGraph(params)
    sh = Shell(hg)
    sh.run()


def reader_tests(params):
    hg = HyperGraph(params)
    infile = params['infile']
    show_namespaces = params['show_namespaces']
    rtests.reader_tests(hg, infile, show_namespaces)


def ui(params):
    hg = HyperGraph(params)
    start_ui(hg)


def reddit_retriever(params):
    subreddit = params['source']
    outfile = params['outfile']
    startdate = params['startdate']
    enddate = params['enddate']
    rr = RedditRetriever(subreddit, outfile, startdate, enddate)
    rr.run()


def reddit_reader(params):
    hg = HyperGraph(params)
    infile = params['infile']
    comments = params['comments']
    RedditReader(hg, comments=comments).read_file(infile)


def interactive_edge_builder(params):
    outfile = params['outfile']
    hypergen_cg.interactive_edge_builder(outfile)


def generate_hypergen_cases(params):
    infile = params['infile']
    outfile = params['outfile']
    hypergen_cg.generate_cases(infile, outfile)


def learn_hypergen(params):
    infile = params['infile']
    model_type = params['model_type']
    hypergen.learn(infile, model_type=model_type)


def test_hypergen(params):
    infile = params['infile']
    model_type = params['model_type']
    hypergen.test(infile, model_type=model_type)


def extract_json_fields(params):
    infile = params['infile']
    outfile = params['outfile']
    fields = params['fields']
    json_tools.extract_fields(infile, outfile, fields)


def all2json(params):
    hg = HyperGraph(params)
    outfile = params['outfile']
    filt = AllFilter(hg)
    filt.write_edges(outfile)


def generate_synonyms(params):
    hg = HyperGraph(params)
    synonyms.generate(hg)


def headlines_inference(params):
    hg = HyperGraph(params)
    infile = params['infile']
    hl.headlines_inference(hg, infile)


def cli():
    parser = argparse.ArgumentParser()

    parser.add_argument('command', type=str, help='command to execute')
    parser.add_argument('--backend', type=str, help='hypergraph backend (leveldb, null)', default='leveldb')
    parser.add_argument('--hg', type=str, help='hypergraph name', default='gb.hg')
    parser.add_argument('--infile', type=str, help='input file', default=None)
    parser.add_argument('--outfile', type=str, help='output file', default=None)
    parser.add_argument('--startdate', type=str, help='start date', default=None)
    parser.add_argument('--enddate', type=str, help='end date', default=None)
    parser.add_argument('--source', type=str, help='source can have multiple meanings.', default=None)
    parser.add_argument('--log', type=str, help='logging level.', default='WARNING')
    parser.add_argument('--comments', help='include comments', action='store_true')
    parser.add_argument('--fields', type=str, help='field names', default=None)
    parser.add_argument('--model_type', type=str, help='machine learning model type', default='rf')
    parser.add_argument('--show_namespaces', help='show namespaces', action='store_true')

    args = parser.parse_args()

    params = {
        'backend': args.backend,
        'hg': args.hg,
        'infile': args.infile,
        'outfile': args.outfile,
        'startdate': args.startdate,
        'enddate': args.enddate,
        'source': args.source,
        'log': args.log,
        'comments': args.comments,
        'fields': args.fields,
        'model_type': args.model_type,
        'show_namespaces': args.show_namespaces
    }

    # configure logging
    numeric_level = getattr(logging, args.log.upper(), None)
    if not isinstance(numeric_level, int):
        raise ValueError('Invalid log level: %s' % args.log)
    logging.basicConfig(level=numeric_level)

    command = args.command

    if command == 'create':
        create(params)
    # elif command == 'wordnet':
    #     wordnet(params)
    elif command == 'wikidata':
        wikidata(params)
    elif command == 'dbpedia':
        dbpedia(params)
    elif command == 'dbpedia_wordnet':
        dbpedia_wordnet(params)
    elif command == 'info':
        info(params)
    elif command == 'shell':
        shell(params)
    elif command == 'reader_tests':
        reader_tests(params)
    elif command == 'ui':
        ui(params)
    elif command == 'reddit_retriever':
        reddit_retriever(params)
    elif command == 'reddit_reader':
        reddit_reader(params)
    elif command == 'interactive_edge_builder':
        interactive_edge_builder(params)
    elif command == 'generate_hypergen_cases':
        generate_hypergen_cases(params)
    elif command == 'learn_hypergen':
        learn_hypergen(params)
    elif command == 'test_hypergen':
        test_hypergen(params)
    elif command == 'extract_json_fields':
        extract_json_fields(params)
    elif command == 'all2json':
        all2json(params)
    elif command == 'generate_synonyms':
        generate_synonyms(params)
    elif command == 'headlines_inference':
        headlines_inference(params)
    else:
        print('unkown command: %s' % command)


show_logo()


if __name__ == '__main__':
    cli()
