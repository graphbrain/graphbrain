from setuptools import setup, find_packages
from setuptools.extension import Extension
from Cython.Build import cythonize


PACKAGE_DATA = {'': ['*.model']}


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
        'jupyter',
        'tensorflow',
        'keras',
        'bleach==1.5.0',
        'h5py',
        'progressbar2',
        'cython',
        'unidecode'
    ],
    extras_require={
        'dev': [
            'Sphinx',
            'sphinx_rtd_theme',
        ]
    },
    entry_points='''
        [console_scripts]
        gbrain=gb.gbrain:cli
    ''',
    ext_modules=cythonize(extensions)
)
