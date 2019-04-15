from importlib import import_module
import argparse
from termcolor import colored
from . import constants as const


def _show_logo():
    for line in const.ascii_logo.split('\n'):
        print(colored(line[:29], 'cyan'), end='')
        print(colored(line[29:], 'green'))
    print()


def wrapper(fun, command=False):
    _show_logo()

    parser = argparse.ArgumentParser()

    if command:
        parser.add_argument('command', type=str, help='command to execute')
    parser.add_argument('--hg', type=str,
                        help='hypergraph db', default='gb.hg')
    parser.add_argument('--infile', type=str, help='input file', default=None)
    parser.add_argument('--outfile', type=str,
                        help='output file', default=None)
    parser.add_argument('--fields', type=str, help='field names', default=None)
    parser.add_argument('--show_namespaces',
                        help='show namespaces', action='store_true')
    parser.add_argument('--lang', type=str, help='language', default='en')

    args = parser.parse_args()

    fun(args)


def _cli(args):
    command = args.command
    try:
        cmd_module = import_module('graphbrain.commands.%s' % command)
        cmd_module.run(args)
    except ImportError:
        print('unkown command: %s' % command)


def cli():
    wrapper(_cli, command=True)
