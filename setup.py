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
CYTHON_FORCE_COMPILATION = False


# Current Graphbrain version
with open('VERSION', 'r') as version_file:
    VERSION = version_file.read()


if USE_CYTHON:
    from Cython.Build import cythonize


if USE_CYTHON:
    ext_modules = [
        Extension('graphbrain.hyperedge', ['graphbrain/hyperedge.pyx'],),
        Extension('graphbrain.memory.leveldb',
                  ['graphbrain/memory/leveldb.pyx']),
        Extension('graphbrain.memory.permutations',
                  ['graphbrain/memory/permutations.pyx']),
        Extension('graphbrain.parsers.alpha_beta',
                  ['graphbrain/parsers/alpha_beta.pyx']),
        Extension('graphbrain.parsers.parser_en',
                  ['graphbrain/parsers/parser_en.pyx']),
        Extension('graphbrain.parsers.parser_de',
                  ['graphbrain/parsers/parser_de.pyx'])
    ]
    ext_modules = cythonize(ext_modules,
                            annotate=CYTHON_ANNOTATE,
                            force=CYTHON_FORCE_COMPILATION,
                            compiler_directives={'language_level': '3'})
else:
    ext_modules = [
        Extension('graphbrain.hyperedge', ['graphbrain/hyperedge.c'], ),
        Extension('graphbrain.memory.leveldb',
                  ['graphbrain/memory/leveldb.c']),
        Extension('graphbrain.memory.permutations',
                  ['graphbrain/memory/permutations.c']),
        Extension('graphbrain.parsers.alpha_beta',
                  ['graphbrain/parsers/alpha_beta.c']),
        Extension('graphbrain.parsers.parser_en',
                  ['graphbrain/parsers/parser_en.c']),
        Extension('graphbrain.parsers.parser_de',
                  ['graphbrain/parsers/parser_de.c'])
    ]


with open('README.md', 'r') as fh:
    long_description = fh.read()


setup(
    name='graphbrain',
    version=VERSION,
    author='Telmo Menezes et al.',
    author_email='telmo@telmomenezes.net',
    description='Knowledge System + Natural Language Understanding',
    long_description=long_description,
    long_description_content_type='text/markdown',
    url='http://graphbrain.net',
    license='MIT',
    keywords=['NLP', 'AI', 'Knowledge Representation', 'Knowledge Systems',
              'Natural Language Understanding', 'Text Analysis', 'Cognition'],
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
    python_requires='>=3.6',
    packages=find_packages(),
    install_requires=[
        'numpy',
        'spacy==2.1',
        'neuralcoref==4.0.0',
        'plyvel',
        'python-igraph',
        'termcolor',
        'asciitree',
        'ipython',
        'progressbar2',
        'unidecode'
    ],
    extras_require={
        'dev': [
            'cython',
            'Sphinx',
            'sphinx_rtd_theme'
        ]
    },
    include_package_data=True,
    entry_points='''
        [console_scripts]
        graphbrain=graphbrain.__main__:cli
    ''',
    ext_modules=ext_modules,
    test_suite='graphbrain.tests'
)
