import logging
import argparse
from termcolor import colored
import graphbrain.constants as const
from graphbrain.hypergraph import HyperGraph
import graphbrain.reader.stages.hypergen_case_generator as hypergen_cg
import graphbrain.reader.stages.hypergen as hypergen
from graphbrain.filters.filters import AllFilter
import graphbrain.synonyms.synonyms as synonyms


def show_logo():
    print(colored(const.ascii_logo, 'cyan'))
    print()


# commands
def create(params):
    print('creating hypergraph...')
    HyperGraph(params)
    print('done.')


def info(params):
    hg = HyperGraph(params)
    print('edge_symbols: %s' % hg.symbol_count())
    print('edges: %s' % hg.edge_count())
    print('total degree: %s' % hg.total_degree())


def interactive_edge_builder(params):
    outfile = params['outfile']
    lang = params['lang']
    hypergen_cg.interactive_edge_builder(outfile, lang=lang)


def generate_hypergen_cases(params):
    infile = params['infile']
    outfile = params['outfile']
    hypergen_cg.generate_cases(infile, outfile)


def learn_hypergen(params):
    infile = params['infile']
    model_type = params['model_type']
    outfile = params['outfile']
    hypergen.learn(infile, model_type=model_type, outfile=outfile)


def test_hypergen(params):
    infile = params['infile']
    model_type = params['model_type']
    model_file = params['model_file']
    hypergen.test(infile, model_type=model_type, model_file=model_file)


def all2json(params):
    hg = HyperGraph(params)
    outfile = params['outfile']
    filt = AllFilter(hg)
    filt.write_edges(outfile)


def generate_synonyms(params):
    hg = HyperGraph(params)
    synonyms.generate(hg)


def cli():
    parser = argparse.ArgumentParser()

    parser.add_argument('command', type=str, help='command to execute')
    parser.add_argument('--backend', type=str, help='hypergraph backend (leveldb, null)', default='leveldb')
    parser.add_argument('--hg', type=str, help='hypergraph name', default='gb.hg')
    parser.add_argument('--infile', type=str, help='input file', default=None)
    parser.add_argument('--outfile', type=str, help='output file', default=None)
    parser.add_argument('--log', type=str, help='logging level.', default='WARNING')
    parser.add_argument('--fields', type=str, help='field names', default=None)
    parser.add_argument('--model_type', type=str, help='machine learning model type', default='rf')
    parser.add_argument('--model_file', type=str, help='machine learning model file', default=None)
    parser.add_argument('--show_namespaces', help='show namespaces', action='store_true')
    parser.add_argument('--lang', type=str, help='language', default='en')

    args = parser.parse_args()

    params = {
        'backend': args.backend,
        'hg': args.hg,
        'infile': args.infile,
        'outfile': args.outfile,
        'log': args.log,
        'fields': args.fields,
        'model_type': args.model_type,
        'model_file': args.model_file,
        'show_namespaces': args.show_namespaces,
        'lang': args.lang
    }

    # configure logging
    numeric_level = getattr(logging, args.log.upper(), None)
    if not isinstance(numeric_level, int):
        raise ValueError('Invalid log level: %s' % args.log)
    logging.basicConfig(level=numeric_level)

    command = args.command

    if command == 'create':
        create(params)
    elif command == 'info':
        info(params)
    elif command == 'interactive_edge_builder':
        interactive_edge_builder(params)
    elif command == 'generate_hypergen_cases':
        generate_hypergen_cases(params)
    elif command == 'learn_hypergen':
        learn_hypergen(params)
    elif command == 'test_hypergen':
        test_hypergen(params)
    elif command == 'all2json':
        all2json(params)
    elif command == 'generate_synonyms':
        generate_synonyms(params)
    else:
        print('unkown command: %s' % command)


show_logo()


if __name__ == '__main__':
    cli()
