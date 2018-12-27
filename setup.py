#!/usr/bin/env python

from setuptools import setup, find_packages
from setuptools.extension import Extension


# True to enable building extensions using Cython.
# False to build extensions from the C files that were previously created by Cython.
USE_CYTHON = False


PACKAGE_DATA = {'': ['*.model']}


if USE_CYTHON:
    from Cython.Distutils import build_ext


if USE_CYTHON:
    ext_modules = [
        Extension('gb.funs', ['gb/funs.pyx'],),
        Extension('gb.backends.leveldb', ['gb/backends/leveldb.pyx'])
    ]
    cmdclass = {'build_ext': build_ext}
else:
    ext_modules = [
        Extension('gb.funs', ['gb/funs.c'], ),
        Extension('gb.backends.leveldb', ['gb/backends/leveldb.c'])
    ]
    cmdclass = {}


with open('README.md', 'r') as fh:
    long_description = fh.read()


setup(
    name='graphbrain',
    version='0.0.3',
    author='Telmo Menezes et al.',
    author_email='telmo@telmomenezes.net',
    description='Knowledge System + Natural Language Understanding',
    long_description=long_description,
    long_description_content_type='text/markdown',
    url='https://github.com/ai4socialscience/graphbrain',
    license='MIT',
    keywords=['NLP', 'AI', 'Knowledge Representation', 'Natural Language Understanding', 'Text Analysis'],
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
    packages=find_packages(),
    package_data=PACKAGE_DATA,
    install_requires=[
        'numpy',
        'scipy',
        'sklearn',
        'pandas',
        'colorama',
        'termcolor',
        'matplotlib',
        'python-igraph',
        'spacy==2.0.18',
        'msgpack-python==0.5.4',
        'asciitree',
        'ujson',
        'plyvel',
        'ipython',
        'tensorflow',
        'keras',
        # 'bleach==1.5.0',
        'bleach',
        'h5py',
        'progressbar2',
        'unidecode'
    ],
    extras_require={
        'dev': [
            'Sphinx',
            'sphinx_rtd_theme',
            'cython'
        ]
    },
    entry_points='''
        [console_scripts]
        gbrain=gb.gbrain:cli
    ''',
    cmdclass=cmdclass,
    ext_modules=ext_modules,
    test_suite='gb.tests'
)
