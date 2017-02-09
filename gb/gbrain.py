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


import click
import gb.hypergraph as hyper
from gb.hypergraph.hypergraph import HyperGraph
import gb.readers.wordnet as wn
import gb.readers.wikidata as wd
from gb.tools.shell import Shell
from gb.ui.server import start_ui


@click.group()
@click.option('--backend', help='Hypergraph Backend (sqlite, mysql, null).', default='leveldb')
@click.option('--hg', help='Hypergraph name.', default='gb.hg')
@click.option('--infile', help='Input file.')
@click.pass_context
def cli(ctx, backend, hg, infile):
    ctx.obj = {
        'backend': backend,
        'hg': hg,
        'infile': infile
    }


def show_logo():
    click.echo(click.style(hyper.constants.ascii_logo, fg='cyan'))
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
    click.echo('reading wordnet...')
    hg = HyperGraph(ctx.obj)
    infile = ctx.obj['infile']
    wd.read(hg, infile)
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
def ui(ctx):
    hg = HyperGraph(ctx.obj)
    start_ui(hg)


show_logo()


if __name__ == '__main__':
    cli()
