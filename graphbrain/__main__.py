from importlib import import_module
import argparse
from termcolor import colored
from . import constants as const


def _show_logo():
    print(colored(const.ascii_logo, 'cyan'))
    print()


def cli():
    _show_logo()

    parser = argparse.ArgumentParser()

    parser.add_argument('command', type=str, help='command to execute')
    parser.add_argument('--backend', type=str,
                        help='hypergraph backend (leveldb, null)',
                        default='leveldb')
    parser.add_argument('--hg', type=str,
                        help='hypergraph name', default='gb.hg')
    parser.add_argument('--infile', type=str, help='input file', default=None)
    parser.add_argument('--outfile', type=str,
                        help='output file', default=None)
    parser.add_argument('--fields', type=str, help='field names', default=None)
    parser.add_argument('--show_namespaces',
                        help='show namespaces', action='store_true')
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

    command = args.command

    try:
        cmd_module = import_module('graphbrain.commands.%s' % command)
        cmd_module.run(params)
    except ImportError:
        print('unkown command: %s' % command)


if __name__ == '__main__':
    cli()
