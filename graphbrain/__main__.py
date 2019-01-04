import importlib
import logging
import argparse
from termcolor import colored
import graphbrain.constants as const


def _show_logo():
    print(colored(const.ascii_logo, 'cyan'))
    print()


def cli():
    _show_logo()

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

    try:
        cmd_module = importlib.import_module('graphbrain.commands.%s' % command)
        cmd_module.run(params)
    except ModuleNotFoundError:
        print('unkown command: %s' % command)


if __name__ == '__main__':
    cli()
