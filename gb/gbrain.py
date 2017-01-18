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
from gb.tools.shell import Shell


@click.group()
@click.option('--backend', help='Hypergraph Backend (sqlite, mysql, null).', default='sqlite')
@click.option('--db', help='Database name.', default='gb.db')
@click.option('--dbuser', help='Database user.', default='gb')
@click.option('--dbpass', help='Database password.', default='gb')
@click.pass_context
def cli(ctx, backend, db, dbuser, dbpass):
    ctx.obj = {
        'backend': backend,
        'db': db,
        'dbuser': dbuser,
        'dbpass': dbpass
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
def shell(ctx):
    hg = HyperGraph(ctx.obj)
    cli = Shell(hg)
    cli.run()
    click.echo('done.')


show_logo()


if __name__ == '__main__':
    cli()
