#!/usr/bin/env python

from setuptools import setup, find_packages
from setuptools.extension import Extension


# True to enable building extensions using Cython.
# False to build extensions from the C files that were previously
# created by Cython.
USE_CYTHON = True

# "If True, will produce a HTML file for each of the .pyx or .py files
# compiled. The HTML file gives an indication of how much Python interaction
# there is in each of the source code lines, compared to plain C code."
# https://cython.readthedocs.io/en/latest/src/userguide/
# source_files_and_compilation.html#cythonize-arguments
CYTHON_ANNOTATE = False

# Force compilation of all Cython code.
CYTHON_FORCE_COMPILATION = True

# Current Graphbrain version
with open('VERSION', 'r') as version_file:
    VERSION = version_file.read()


if USE_CYTHON:
    from Cython.Build import cythonize


if USE_CYTHON:
    ext_modules = [
        Extension('graphbrain.hyperedge', ['graphbrain/hyperedge.pyx']),
        Extension('graphbrain.patterns', ['graphbrain/patterns.pyx']),
        Extension('graphbrain.memory.keyvalue', ['graphbrain/memory/keyvalue.pyx']),
        Extension('graphbrain.memory.sqlite', ['graphbrain/memory/sqlite.pyx']),
        Extension('graphbrain.memory.leveldb', ['graphbrain/memory/leveldb.pyx']),
        Extension('graphbrain.memory.permutations', ['graphbrain/memory/permutations.pyx']),
        Extension('graphbrain.parsers.alpha', ['graphbrain/parsers/alpha.pyx']),
        Extension('graphbrain.parsers.alpha_beta', ['graphbrain/parsers/alpha_beta.pyx']),
        Extension('graphbrain.parsers.parser_en', ['graphbrain/parsers/parser_en.pyx'])
    ]
    ext_modules = cythonize(ext_modules,
                            annotate=CYTHON_ANNOTATE,
                            force=CYTHON_FORCE_COMPILATION,
                            compiler_directives={'language_level': '3'})
else:
    ext_modules = [
        Extension('graphbrain.hyperedge', ['graphbrain/hyperedge.c'], include_dirs=['.']),
        Extension('graphbrain.patterns', ['graphbrain/patterns.c'], include_dirs=['.']),
        Extension('graphbrain.memory.keyvalue', ['graphbrain/memory/keyvalue.c'], include_dirs=['.']),
        Extension('graphbrain.memory.sqlite', ['graphbrain/memory/sqlite.c'], include_dirs=['.']),
        Extension('graphbrain.memory.leveldb', ['graphbrain/memory/leveldb.c'], include_dirs=['.']),
        Extension('graphbrain.memory.permutations', ['graphbrain/memory/permutations.c'], include_dirs=['.']),
        Extension('graphbrain.parsers.alpha', ['graphbrain/parsers/alpha.c'], include_dirs=['.']),
        Extension('graphbrain.parsers.alpha_beta', ['graphbrain/parsers/alpha_beta.c'], include_dirs=['.']),
        Extension('graphbrain.parsers.parser_en', ['graphbrain/parsers/parser_en.c'], include_dirs=['.'])
    ]


with open('README.md', 'r', encoding='utf8') as fh:
    long_description = fh.read()


python_requires = '>=3.9'

install_requires = [
        'asciitree',
        'editdistance',
        'flask',
        'gensim',
        'ipython',
        'mwparserfromhell',
        'networkx',
        'numpy',
        'plyvel',
        'progressbar2',
        'scikit-learn',
        'spacy-experimental==0.6.0',
        'spacy',
        'termcolor',
        'gensim',
        'thinc',
        'torch',
        'spacy-transformers',
    ]

setup(
    name='graphbrain',
    version=VERSION,
    author='Telmo Menezes et al.',
    author_email='telmo@telmomenezes.net',
    description='Knowledge System + Natural Language Understanding',
    long_description=long_description,
    long_description_content_type='text/markdown',
    url='https://graphbrain.net',
    license='MIT',
    keywords=['NLP', 'AI', 'Knowledge Representation', 'Knowledge Systems', 'Natural Language Understanding',
              'Text Analysis', 'Cognition'],
    classifiers=[
        'Development Status :: 4 - Beta',
        'Programming Language :: Python :: 3',
        'License :: OSI Approved :: MIT License',
        'Operating System :: OS Independent',
        'Environment :: Console',
        'Intended Audience :: Science/Research',
        'Topic :: Scientific/Engineering :: Artificial Intelligence',
        'Topic :: Scientific/Engineering :: Information Analysis',
        'Topic :: Sociology'
    ],
    python_requires=python_requires,
    packages=find_packages(),
    install_requires=install_requires,
    extras_require={
        'dev': [
            'cython >=0.25',
            'pytest',
            'Sphinx',
            'sphinx_rtd_theme'
        ]
    },
    package_data={'': ['data/*.csv']},
    entry_points='''
        [console_scripts]
        graphbrain=graphbrain.__main__:cli
    ''',
    ext_modules=ext_modules
)
