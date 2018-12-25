from setuptools import setup, find_packages
from setuptools.extension import Extension
from Cython.Build import cythonize


with open('README.md', 'r') as fh:
    long_description = fh.read()


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
    version='0.0.1',
    author='Telmo Menezes et al.',
    author_email='telmo@telmomenezes.net',
    description='Symbolic generators for complex networks',
    long_description=long_description,
    long_description_content_type='text/markdown',
    url='http://graphbrain.org',
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
        'jupyter',
        'tensorflow',
        'keras',
        # 'bleach==1.5.0',
        'bleach',
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
