#!/usr/bin/env python

from setuptools import setup, find_packages

# Current Graphbrain version
with open('VERSION') as version_file:
    VERSION = version_file.read()

with open('README.md', encoding='utf8') as fh:
    long_description = fh.read()

python_requires = '>=3.10'

install_requires = [
        'asciitree',
        'ipython',
        'mwparserfromhell',
        'networkx',
        # fixed for now because of spacy
        'numpy', # ==1.26.4',
        'plyvel',
        'progressbar2',
        'scikit-learn',
        'spacy',
        # 'spacy-experimental==0.6.1',
        'spacy-transformers',
        'termcolor',
        'thinc',
        'torch',
        'trafilatura',
    ]

setup(
    name='graphbrain',
    version=VERSION,
    author='Telmo Menezes et al.',
    author_email='telmo@telmomenezes.net',
    description='Semantic Hypergraphs Tools',
    long_description=long_description,
    long_description_content_type='text/markdown',
    url='https://graphbrain.net',
    license='MIT',
    keywords=['NLP', 'AI', 'Knowledge Representation', 'Knowledge Systems', 'Natural Language Understanding',
              'Text Analysis', 'Cognition'],
    classifiers=[
        'Development Status :: 4 - Beta',
        'Programming Language :: Python :: 3',
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
)
