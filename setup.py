#!/usr/bin/env python

from setuptools import setup, find_packages
from setuptools.extension import Extension


# Current GraphBrain version
with open('VERSION', 'r') as version_file:
    VERSION = version_file.read()


# True to enable building extensions using Cython.
# False to build extensions from the C files that were previously created by
# Cython.
USE_CYTHON = True


if USE_CYTHON:
    from Cython.Distutils import build_ext


if USE_CYTHON:
    ext_modules = [
        Extension('graphbrain.funs', ['graphbrain/funs.pyx'],),
        Extension('graphbrain.backends.leveldb',
                  ['graphbrain/backends/leveldb.pyx']),
        Extension('graphbrain.meaning.parser',
                  ['graphbrain/meaning/parser.pyx'])
    ]
    cmdclass = {'build_ext': build_ext}
else:
    ext_modules = [
        Extension('graphbrain.funs', ['graphbrain/funs.c'], ),
        Extension('graphbrain.backends.leveldb',
                  ['graphbrain/backends/leveldb.c']),
        Extension('graphbrain.meaning.parser',
                  ['graphbrain/meaning/parser.c'])
    ]
    cmdclass = {}


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
    url='https://github.com/graphbrain/graphbrain',
    license='MIT',
    keywords=['NLP', 'AI', 'Knowledge Representation', 'Knowledge Systems',
              'Natural Language Understanding', 'Text Analysis', 'Cognition'],
    classifiers=[
        'Development Status :: 3 - Alpha',
        'Programming Language :: Python :: 3',
        'License :: OSI Approved :: MIT License',
        'Operating System :: OS Independent',
        'Environment :: Console',
        'Intended Audience :: Science/Research',
        'Topic :: Scientific/Engineering :: Artificial Intelligence',
        'Topic :: Scientific/Engineering :: Information Analysis',
        'Topic :: Sociology'
    ],
    python_requires='>=3.4',
    packages=find_packages(),
    install_requires=[
        'numpy',
        'termcolor',
        'spacy',
        'asciitree',
        'plyvel',
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
    entry_points='''
        [console_scripts]
        graphbrain=graphbrain.__main__:cli
    ''',
    cmdclass=cmdclass,
    ext_modules=ext_modules,
    test_suite='graphbrain.tests'
)
