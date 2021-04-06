import argparse
import sys

from importlib import import_module

from termcolor import colored

from graphbrain import constants as const
from graphbrain.parsers import parser_lang


def _show_logo():
    for line in const.ascii_logo.split('\n'):
        print(colored(line[:29], 'cyan'), end='')
        print(colored(line[29:], 'green'))
    print()


def error_msg(msg):
    print('{} {}'.format(colored('error:', 'red'), msg))


def wrapper(fun, command=False, text=None):
    _show_logo()

    parser = argparse.ArgumentParser()

    if command:
        parser.add_argument('command', type=str, help='command to execute')
    parser.add_argument('--agent', type=str, help='agent name', default=None)
    parser.add_argument('--corefs', type=str,
                        help='parser coreference resolution: '
                             '"resolve" (default), "replace" or "no")',
                        default='no')
    parser.add_argument('--fields', type=str, help='field names', default=None)
    parser.add_argument('--hg', type=str,
                        help='hypergraph db', default='gb.hg')
    parser.add_argument('--indir', type=str,
                        help='input directory', default=None)
    parser.add_argument('--infile', type=str, help='input file', default=None)
    parser.add_argument('--lang', type=str, help='language', default=None)
    parser.add_argument('--parser', type=str, help='parser', default=None)
    parser.add_argument('--outdir', type=str,
                        help='output directory', default=None)
    parser.add_argument('--outfile', type=str,
                        help='output file', default=None)
    parser.add_argument('--pattern', type=str, help='edge pattern',
                        default='*')
    parser.add_argument('--sequence', type=str, help='sequence name',
                        default=None)
    parser.add_argument('--show_namespaces',
                        help='show namespaces', action='store_true')
    parser.add_argument('--system', type=str, help='agent system file',
                        default=None)
    parser.add_argument('--text', type=str, help='text identifier',
                        default='title')
    parser.add_argument('--training_data', type=str,
                        help='training data for ML tasks', default=None)
    parser.add_argument('--url', type=str, help='url', default=None)

    args = parser.parse_args()

    # check arguments
    if args.corefs not in {'resolve', 'replace', 'no'}:
        msg = '--corefs must be either "resolve", "replace" or "no". '
        msg += '"{}" is not valid.'.format(args.corefs)
        error_msg(msg)
        sys.exit(-1)

    # determine language
    if args.parser:
        plang = parser_lang(args.parser)
        if args.lang:
            if args.lang != plang:
                msg = 'specified language ({}) and parser language ({}) do '\
                      'not match'.format(args.lang, plang)
                error_msg()
                sys.exit(-1)
        else:
            args.lang = plang
    # if not lang or parser is specified, default to 'en'
    elif not args.lang:
        args.lang = 'en'

    if text is None and command:
        text = 'command: {}'.format(args.command)
    if text:
        print(colored('{}\n'.format(text), 'white'))

    if args.agent:
        print('agent: {}'.format(args.agent))
    if args.corefs:
        print('coreferences: {}'.format(args.corefs))
    if args.hg:
        print('hypergraph: {}'.format(args.hg))
    if args.infile:
        print('input file: {}'.format(args.infile))
    if args.lang:
        print('language: {}'.format(args.lang))
    if args.parser:
        print('parser: {}'.format(args.parser))
    if args.outfile:
        print('output file: {}'.format(args.outfile))
    if args.sequence:
        print('sequence: {}'.format(args.sequence))
    if args.system:
        print('system: {}'.format(args.system))
    if args.training_data:
        print('training data: {}'.format(args.training_data))
    if args.url:
        print('url: {}'.format(args.url))

    print()

    fun(args)

    print()


def _cli(args):
    command = args.command
    cmd_module = import_module('graphbrain.commands.{}'.format(command))
    cmd_module.run(args)


def cli():
    wrapper(_cli, command=True)
