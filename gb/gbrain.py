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
import click
import gb.constants as const
from gb.hypergraph.hypergraph import HyperGraph
import gb.importers.wordnet as wn
import gb.importers.wikidata as wd
import gb.importers.dbpedia as dbp
import gb.importers.dbpedia_wordnet as dbpwn
from gb.tools.shell import Shell
from gb.tools.reader_tests import ReaderTests
from gb.ui.server import start_ui
from gb.retrievers.reddit import RedditRetriever
from gb.reader.reddit import RedditReader
import gb.reader.stages.alpha_case_generator as alpha_cg
import gb.reader.stages.alpha_forest as alpha_for


@click.group()
@click.option('--backend', help='Hypergraph Backend (leveldb, null).', default='leveldb')
@click.option('--hg', help='Hypergraph name.', default='gb.hg')
@click.option('--infile', help='Input file.')
@click.option('--outfile', help='Output file.')
@click.option('--startdate', help='Start date.')
@click.option('--enddate', help='End date.')
@click.option('--source', help='Source can have multiple meanings.')
@click.option('--log', help='Logging level.', default='WARNING')
@click.option('--disamb/--no_disamb', help='Perform sense.', default=False)
@click.option('--comments/--no_comments', help='Include comments.', default=False)
@click.pass_context
def cli(ctx, backend, hg, infile, outfile, startdate, enddate, source, log, disamb, comments):
    ctx.obj = {
        'backend': backend,
        'hg': hg,
        'infile': infile,
        'outfile': outfile,
        'startdate': startdate,
        'enddate': enddate,
        'source': source,
        'log': log,
        'disamb': disamb,
        'comments': comments
    }

    # configure logging
    numeric_level = getattr(logging, log.upper(), None)
    if not isinstance(numeric_level, int):
        raise ValueError('Invalid log level: %s' % log)
    logging.basicConfig(level=numeric_level)


def show_logo():
    click.echo(click.style(const.ascii_logo, fg='cyan'))
    click.echo()


@cli.command()
@click.pass_context
def create(ctx):
    click.echo('creating hypergraph...')
    HyperGraph(ctx.obj)
    click.echo('done.')


@cli.command()
@click.pass_context
def wordnet(ctx):
    click.echo('reading wordnet...')
    hg = HyperGraph(ctx.obj)
    wn.read(hg)
    click.echo('done.')


@cli.command()
@click.pass_context
def wikidata(ctx):
    click.echo('reading wikidata...')
    hg = HyperGraph(ctx.obj)
    infile = ctx.obj['infile']
    wd.read(hg, infile)
    click.echo('done.')


@cli.command()
@click.pass_context
def dbpedia(ctx):
    click.echo('reading DBPedia...')
    hg = HyperGraph(ctx.obj)
    infile = ctx.obj['infile']
    dbp.read(hg, infile)
    click.echo('done.')


@cli.command()
@click.pass_context
def dbpedia_wordnet(ctx):
    click.echo('reading DBPedia...')
    hg = HyperGraph(ctx.obj)
    infile = ctx.obj['infile']
    dbpwn.read(hg, infile)
    click.echo('done.')


@cli.command()
@click.pass_context
def info(ctx):
    hg = HyperGraph(ctx.obj)
    print('symbols: %s' % hg.symbol_count())
    print('edges: %s' % hg.edge_count())
    print('total degree: %s' % hg.total_degree())


@cli.command()
@click.pass_context
def shell(ctx):
    hg = HyperGraph(ctx.obj)
    sh = Shell(hg)
    sh.run()
    click.echo('done.')


@cli.command()
@click.pass_context
def generate_parsed_sentences_file(ctx):
    hg = HyperGraph(ctx.obj)
    rt = ReaderTests(hg, ctx.obj['disamb'])
    rt.generate_parsed_sentences_file(ctx.obj['infile'], ctx.obj['outfile'])
    click.echo('done.')


@cli.command()
@click.pass_context
def reader_tests(ctx):
    hg = HyperGraph(ctx.obj)
    rt = ReaderTests(hg, ctx.obj['disamb'])
    rt.run_tests(ctx.obj['infile'])
    click.echo('done.')


@cli.command()
@click.pass_context
def reader_debug(ctx):
    hg = HyperGraph(ctx.obj)
    rt = ReaderTests(hg, ctx.obj['disamb'])
    rt.reader_debug(ctx.obj['infile'])
    click.echo('done.')


@cli.command()
@click.pass_context
def ui(ctx):
    hg = HyperGraph(ctx.obj)
    start_ui(hg)


@cli.command()
@click.pass_context
def reddit_retriever(ctx):
    subreddit = ctx.obj['source']
    outfile = ctx.obj['outfile']
    startdate = ctx.obj['startdate']
    enddate = ctx.obj['enddate']
    rr = RedditRetriever(subreddit, outfile, startdate, enddate)
    rr.run()


@cli.command()
@click.pass_context
def reddit_reader(ctx):
    hg = HyperGraph(ctx.obj)
    infile = ctx.obj['infile']
    comments = ctx.obj['comments']
    RedditReader(hg, comments=comments).read_file(infile)


@cli.command()
@click.pass_context
def interactive_edge_builder(ctx):
    outfile = ctx.obj['outfile']
    alpha_cg.interactive_edge_builder(outfile)


@cli.command()
@click.pass_context
def generate_alpha_cases(ctx):
    infile = ctx.obj['infile']
    outfile = ctx.obj['outfile']
    alpha_cg.generate_cases(infile, outfile)


@cli.command()
@click.pass_context
def learn_alpha_forest(ctx):
    infile = ctx.obj['infile']
    outfile = ctx.obj['outfile']
    alpha_for.learn(infile, outfile)


show_logo()


if __name__ == '__main__':
    cli()
