#!/usr/bin/env python
from pathlib import Path
from itertools import chain
from setuptools import setup, find_packages
from setuptools.extension import Extension


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

# Current Graphbrain version
with open('VERSION') as version_file:
    VERSION = version_file.read()


def get_source_file_paths(module_name: str, use_cython: bool) -> list[Path]:
    module_base_path = Path(module_name.replace('.', '/'))
    module_source_file = Path(f"{module_base_path}.{'pyx' if use_cython else 'c'}")
    if module_source_file.exists():
        return [module_source_file]
    return list(Path(f"{module_base_path}").glob(f"**/*.{'pyx' if use_cython else 'c'}"))


def get_sub_module_names(source_file_paths: list[Path]):
    return [
        str(source_file_path.with_suffix('')).replace("/", ".") for source_file_path in source_file_paths
    ]


def get_ext_modules(module_names: list[str], use_cython: bool) -> list[Extension]:
    source_file_paths = list(chain(*[get_source_file_paths(module_name, use_cython) for module_name in module_names]))
    sub_module_names = get_sub_module_names(source_file_paths)

    ext_modules_ = []
    for module_name, source_file_path in zip(sub_module_names, source_file_paths):
        if use_cython:
            ext_modules_.append(Extension(module_name, [str(source_file_path)]))
        else:
            ext_modules_.append(Extension(module_name, [str(source_file_path)], include_dirs=['.']))
    return ext_modules_


EXT_MODULES = [
    "graphbrain.hyperedge",
    "graphbrain.patterns",
    "graphbrain.memory",
    "graphbrain.parsers",
]

ext_modules = get_ext_modules(EXT_MODULES, USE_CYTHON)


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
        'spacy-experimental==0.6.0',
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
