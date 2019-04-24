from importlib import import_module
import argparse
from termcolor import colored
from . import constants as const


def _show_logo():
    for line in const.ascii_logo.split('\n'):
        print(colored(line[:29], 'cyan'), end='')
        print(colored(line[29:], 'green'))
    print()


def wrapper(fun, command=False, text=None):
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
    parser.add_argument('--pattern', type=str, help='edge pattern',
                        default='*')

    args = parser.parse_args()

    if text is None and command:
        text = 'command: {}'.format(args.command)
    if text:
        print(colored('{}\n'.format(text), 'white'))

    if args.hg:
        print('hypergraph: {}'.format(args.hg))
    if args.infile:
        print('input file: {}'.format(args.infile))
    if args.outfile:
        print('output file: {}'.format(args.outfile))

    print()

    fun(args)

    print()


def _cli(args):
    command = args.command
    try:
        cmd_module = import_module('graphbrain.commands.{}'.format(command))
        cmd_module.run(args)
    except ImportError as error:
        print('unkown command: {}'.format(command))
        print(error)


def cli():
    wrapper(_cli, command=True)
