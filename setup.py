from setuptools import setup, find_packages
from setuptools.extension import Extension
from Cython.Build import cythonize


extensions = [
    Extension(
        'gb.funs',
        ['gb/funs.pyx'],
        include_dirs=[],
        libraries=[],
        library_dirs=[]
    ),
    Extension(
        'gb.backends.leveldb',
        ['gb/backends/leveldb.pyx'],
        include_dirs=[],
        libraries=[],
        library_dirs=[]
    )
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
        'msgpack-python==0.5.4',
        'asciitree',
        'ujson',
        'plyvel',
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
        'networkx'
    ],
    entry_points='''
        [console_scripts]
        gbrain=gb.gbrain:cli
    ''',
    ext_modules=cythonize(extensions)
)
