#!/usr/bin/env python

import sys
import distutils
from setuptools import setup, find_packages
from setuptools.extension import Extension


# True to enable building extensions using Cython.
# False to build extensions from the C files that were previously
# created by Cython.
USE_CYTHON = False

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


# this stuff was adapted from neuralcoref's setup -----------------------------
def is_new_osx():
    """Check whether we're on OSX >= 10.10"""
    name = distutils.util.get_platform()
    if sys.platform != 'darwin':
        return False
    elif name.startswith('macosx-10'):
        minor_version = int(name.split('-')[1].split('.')[1])
        if minor_version >= 7:
            return True
        else:
            return False
    else:
        return False


# -fopenmp ?
COMPILE_OPTIONS = ['-O2', '-Wno-strict-prototypes', '-Wno-unused-function']
# -fopenmp ?
LINK_OPTIONS = []


if is_new_osx():
    # On Mac, use libc++ because Apple deprecated use of
    # libstdc
    COMPILE_OPTIONS.append('-stdlib=libc++')
    LINK_OPTIONS.append('-lc++')
    # g++ (used by unix compiler on mac) links to libstdc++ as a default lib.
    # See: https://stackoverflow.com/questions/1653047/avoid-linking-to-libstdc
    LINK_OPTIONS.append('-nodefaultlibs')
# -----------------------------------------------------------------------------


if USE_CYTHON:
    ext_modules = [
        Extension('graphbrain.hyperedge', ['graphbrain/hyperedge.pyx'],
                  include_dirs=['.']),
        Extension('graphbrain.memory.leveldb',
                  ['graphbrain/memory/leveldb.pyx'],
                  include_dirs=['.']),
        Extension('graphbrain.memory.permutations',
                  ['graphbrain/memory/permutations.pyx'],
                  include_dirs=['.']),
        Extension('graphbrain.parsers.alpha',
                  ['graphbrain/parsers/alpha.pyx'],
                  include_dirs=['.']),
        Extension('graphbrain.parsers.alpha_beta',
                  ['graphbrain/parsers/alpha_beta.pyx'],
                  include_dirs=['.']),
        Extension('graphbrain.parsers.parser_en',
                  ['graphbrain/parsers/parser_en.pyx'],
                  include_dirs=['.']),
        Extension('graphbrain.neuralcoref.neuralcoref',
                  ['graphbrain/neuralcoref/neuralcoref.pyx'],
                  language='c++',
                  include_dirs=['.', 'include'])
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
        Extension('graphbrain.parsers.alpha',
                  ['graphbrain/parsers/alpha.c']),
        Extension('graphbrain.parsers.alpha_beta',
                  ['graphbrain/parsers/alpha_beta.c']),
        Extension('graphbrain.parsers.parser_en',
                  ['graphbrain/parsers/parser_en.c']),
        Extension('graphbrain.neuralcoref.neuralcoref',
                  ['graphbrain/neuralcoref/neuralcoref.cpp'],
                  language='c++', include_dirs=['include'])
    ]


for ext_module in ext_modules:
    ext_module.extra_compile_args = COMPILE_OPTIONS
    ext_module.extra_link_args = LINK_OPTIONS


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
    python_requires='>=3.6, <3.9',
    packages=find_packages(),
    install_requires=[
        'numpy',
        'scikit-learn',
        'spacy >=2.1.0, <3.0.0',
        'plyvel',
        'python-igraph',
        'termcolor',
        'asciitree',
        'ipython',
        'progressbar2',
        'unidecode',
        # for neurocoref
        'boto3'
    ],
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
