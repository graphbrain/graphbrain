============
Installation
============

GraphBrain is compatible with 64-bit CPython 3.3+ and runs on Linux, macOS and Windows(untested). GraphBrain is available over pip. Installation requires a working build environment. `For both installation methods, check the prerequisites for your OS <#prerequisites>`_. Make sure you are using Python 3.

Install with pip (recommended)
==============================

::

   $ sudo pip install graphbrain


It is advisable to work with virtual environments. This avoids conflicts with the system-wide installed packages and creates a nice self-contained environment for you to work on. It is particularly important with GraphBrain because we maintain dependencies to specific versions of downstream machine learning tools, as to keep results consistent.

To create a virtual environment in the current directory you can do this::

   $ virtualenv -p <path to python3> venv

Common locations for python3:

* /usr/local/bin/python3
* /usr/bin/python3

If you are on macOS, do this instead (for the reason `explained here <http://matplotlib.org/faq/osx_framework.html#osxframework-faq>`_)::

   $ python3 -m venv venv

Then to switch to the virtual environment and install GraphBrain::

   $ source venv/bin/activate
   $ pip install graphbrain

It is still necessary to download a spaCy language model::

   $ python -m spacy download en_core_web_lg


Install from the code repository (github)
=========================================

Another way to install GraphBrain is to build it from the GitHub repository source. You should use this method if you wish to make changes to the code base.

Start by cloning the source code to your current local directory.
::

   $ git clone https://github.com/ai4socialscience/graphbrain.git
   $ cd graphbrain

It is advisable to work with virtual environments, for reasons that are explained in the previous section. Please refer to it for details.

Install Cython::

   $ pip install cython

Now we can build and install GraphBrain::

   $ pip install .

It is still necessary to download a spaCy language model::

   $ python -m spacy download en_core_web_lg

For developers
--------------

If you want to help develop GraphBrain, you should also install the development dependencies::

   $ pip install '.[dev]'

Run tests
---------

From the project's root directory::

  $ ./setup.py test


Prerequisites
=============

Naturally, you can ignore the prerequisites that are already installed in your machine.

Linux
-----

Linux distributions are diverse in terms of package managers. Please use the appropriate package manager for your
distribution to install the prerequisites.

* gcc
* Python 3
* pip (Python package manager)
* virtualenv (Virtual Python Environment builder)

macOS
-----

* XCode and the "Command Line Tools"::

   $ xcode-select --install

* The `Homebrew package manager <http://brew.sh/>`.

* Python 3::

   $ brew install python3

* pip (Python package manager)::

   $ sudo easy_install pip

* virtualenv (Virtual Python Environment builder)::

   $ sudo -H pip install virtualenv


* LevelDB (database engine)::

   $ brew install leveldb

Windows
-------

_plyvel_ is required for installing _graphbrain_ on Windows. To install _plyvel_, _boost.lib_ and _leveldb.lib_ should be compiled first. 

* boost.lib (x64)

(1) download and unzip Boost library:  https://www.boost.org/

(2) make sure _cl_ is the environment variable

(3) open _x64 Native Tools Command Prompt_ for VS 2017. Get into the directory of the unzipped Boost

(4) run command: (_b2.exe_ and _bjam.exe_ will be added after this step)
   $ bootstrap.bat

(5) open and modify _project-config.jam_ in the directory:
   $ import option ;
   $ using msvc : 14.1 : “E: \VS2017\VC\Tools\MSVC\14.1-.250117\bin\HostX64\x64\cl.exe” ;
   $ option.set keep-going : false ;

Please note that: i) use your own VS version number (e.g. 14.1) ii) use your own directory of _cl.exe_.

(6) (be careful: this step may take 1 - 3 hours depends on computers) create a folder named build in current directory. And run command: 
   $ b2.exe stage --toolset=msvc-14.1 address-model=64 --stagedir="E:\mylib\boost\bin1.64.0\VC14.
   $ 1" threading=multi --build-type=complete --build-dir="E:\mylib\boost\boost_1_64_0\build"

Please note that: i) use your own directory names ii) be careful about the VS version number

* leveldb.lib

(1) download source codes of _LevelDB_ for windows: https://github.com/vaskomitanov/leveldb  

(2) open Visual Studio (2017). Create a project: 
      •	file – new – project from existing code
      •	choose the type of project: Visual C++
      •	project file location: the directory of LevelDB
      •	project name: LevelDB
      •	project type: LIB
      •	preprocessor definition: LEVELDB_PLATFORM_WINDOWS;OS_WIN; WIN32
      •	include search path: E:\LIB\leveldb-windows;E:\LIB\leveldb-windows\include (the directory of LevelDB and its include)
      •	finish

(3) set _LevelDB.lib_:
      •	project – properties – configuration properties – configuration type: LIB
      •	configuration properties – C/C++ - General – preprocessor – preprocessor definition: LEVELDB_PLATFORM_WINDOWS;OS_WIN; WIN32
      •	linker – general – additional library directory: E:\LIB\boost64\stage\lib (use your own path)

(3) open _solution explorer_, exclude the following files (tips: you can search and right click on the target files):
      •	files ends with _%_test.cc and _bench.cc%_
      •	_port/port_android.cc_
      •	_port/port_posix.cc_
      •	_util/env_posix.cc_

(4) modify codes:
   i. _db\c.cc_: 
   $ delete #include < unistd.h>
   
   ii. _port\port.h_: add the lines below before first #endif
   $ elif defined(LEVELDB_PLATFORM_WINDOWS)
   $ include "port/port_win.h"
   
(5) note: remember to compile as _release x64_

* plyvel
modify _setup.py_:
   $ Extension(
		   $ ...
		   $ libraries=['leveldb vaskomitanov-r x64',
				      $ 'libboost_chrono-vc141-mt-x64-1_69',
				      $ 'libboost_date_time-vc141-mt-x64-1_69',
				      $ 'libboost_filesystem-vc141-mt-x64-1_69',
				      $ 'libboost_system-vc141-mt-x64-1_69',
				      $ 'libboost_thread-vc141-mt-x64-1_69'],
		   $ ...
       $ )
       
* you may also need to install Cmake, please check the official guidance here: https://cgold.readthedocs.io/en/latest/first-step/installation.html#windows
 





