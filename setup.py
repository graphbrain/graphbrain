#!/usr/bin/env python

from setuptools import setup, find_packages
from setup_utils import get_ext_modules

# True to enable building extensions using Cython.
# False to build extensions from the C files that were previously
# created by Cython.
USE_CYTHON = True

# "If True, will produce an HTML file for each of the .pyx or .py files
# compiled. The HTML file gives an indication of how much Python interaction
# there is in each of the source code lines, compared to plain C code."
# https://cython.readthedocs.io/en/latest/src/userguide/
# source_files_and_compilation.html#cythonize-arguments
CYTHON_ANNOTATE = False

# Force compilation of all Cython code.
CYTHON_FORCE_COMPILATION = True

EXT_MODULES = [
    'graphbrain.hyperedge',
    'graphbrain.memory',
    'graphbrain.parsers',
    'graphbrain.patterns',
    'graphbrain.patterns.semsim',
]

ext_modules = get_ext_modules(EXT_MODULES, USE_CYTHON)

if USE_CYTHON:
    from Cython.Build import cythonize  # noqa
    ext_modules = cythonize(
        ext_modules,
        annotate=CYTHON_ANNOTATE,
        force=CYTHON_FORCE_COMPILATION,
        compiler_directives={'language_level': '3'}
    )

# Current Graphbrain version
with open('VERSION') as version_file:
    VERSION = version_file.read()

with open('README.md', encoding='utf8') as fh:
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
        # semsim might require 'spacy-experimental==0.6.4'?
        'spacy-experimental==0.6.1',
        'spacy',
        'termcolor',
        'trafilatura',
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
