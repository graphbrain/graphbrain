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


from setuptools import setup, find_packages
from setuptools.extension import Extension
from Cython.Build import cythonize


extensions = [
    Extension(
        'gb.hypergraph.edge',
        ['gb/hypergraph/edge.pyx'],
        include_dirs=[],
        libraries=[],
        library_dirs=[],
    ),
    Extension(
        'gb.hypergraph.symbol',
        ['gb/hypergraph/symbol.pyx'],
        include_dirs=[],
        libraries=[],
        library_dirs=[],
    ),
]


setup(
    name='graphbrain',
    version='0.1',
    packages=find_packages(),
    install_requires=[
        'numpy',
        'scipy',
        'sklearn',
        'pandas',
        'colorama',
        'termcolor',
        'matplotlib',
        'python-igraph',
        'nltk',
        'spacy',
        'asciitree',
        'ujson',
        'plyvel',
        'bottle',
        'praw',
        'jupyter',
        'Sphinx',
        'sphinx_rtd_theme',
        'tensorflow',
        'keras',
        'bleach==1.5.0',
        'h5py',
        'progressbar2',
        'cython',
        'unidecode',
        'progressbar2',
        'networkx',
        'visJS2jupyter'
    ],
    entry_points='''
        [console_scripts]
        gbrain=gb.gbrain:cli
    ''',
    ext_modules=cythonize(extensions)
)
