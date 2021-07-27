============================
Hypergraph database backends
============================

In the future, we expect other hypergraph database backends to be included in the library (for example, fully in-memory hypergraphs for even higher performance at relatively small sizes, or distributed hypergraphs for huge datasets, fault-tolerance, etc.). Graphbrain is an open source project, so contributions from people interested in developing such implementations are very welcome!


SQLite 3
========

**File extensions:** ``.db``, ``.sqlite``, ``.sqlite3`` 

**Pros:** universal support; native to Python; very fast; concurrent access; single file storage

**Cons:** not space-efficient; possibly not as fast as LevelDB

This backend comes with vamilla Graphbrain.


LevelDB
=======

**File extensions:** ``.hg``, ``.leveldb``

**Pros:** very space-efficient; very fast

**Cons:** no concurrent access; hard to install outside of Linux; directory structure not as nice as single file

LevelDB is a local filesytem-based high-performance key-value store. To support LevelDB, you will need to build Graphbrain from source with a special option set, `as explained in the installation instructions </installation.html#building-graphbrain-with-support-for-leveldb-hypergraph-databases>`_. 

The *plyvel* library is required for LevelDB support. Unfortunately, this library is currently hard to install outsisde of Linux systems. Below is the current best information we have for macOS and Windows.

macOS
-----

LevelDB can be installed with brew, but currently the latest version is not compatible with the Python wrapper (plyvel). The solution is to install LevelDB version 1.22. With brew, this can be achieved in the following way::

   $ brew tap bagonyi/homebrew-formulae git@github.com:bagonyi/homebrew-formulae.git
   $ brew extract --version=1.22 leveldb bagonyi/formulae
   $ brew install leveldb@1.22

(from https://github.com/bagonyi/homebrew-formulae)

The first command above might cause an error, but it appears it can be ignored. If a preexisting installation of LevelDB exists, it should be removed first::

   $ brew uninstall leveldb

Another option is to perform your own custom build of LevelDB 1.23, with the following line removed from its ``CMakeLists.txt``::

   set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-rtti")

For more details: https://github.com/wbolster/plyvel/issues/114.

Anaconda on macOS
-----------------

We have found cases where the plyvel library that is installed as a dependency by Graphbrain does not work in Anaconda environments. This was observed on macOS Big Sur, on MacBook Pro laptops with the M1 chip. It is unknown if it might apply in other cases.

You might get error messages such as this one::

   ImportError: dlopen(/opt/anaconda3/lib/python3.8/site-packages/plyvel/plyvel.cpython-38-darwin.so, 2): Symbol not found: _ZTIN7leveldb10ComparatorE
   Referenced from: /opt/anaconda3/lib/python3.8/site-packages/plyvel/_plyvel.cpython-38-darwin.so
   Expected in: /usr/local/opt/leveldb/lib/libleveldb.1.dylib
   in /opt/anaconda3/lib/python3.8/site-packages/plyvel/_plyvel.cpython-38-darwin.so

The solution is to replace plyvel with one from the Anaconda repositories::

   $ pip uninstall plyvel
   $ conda install -c conda-forge plyvel

Windows
-------

To install *plyvel*, *boost.lib* and *leveldb.lib* must be compiled first. 

* **boost.lib (x64)**

**step 1** download and unzip the *Boost* library:  https://www.boost.org/

**step 2** make sure *cl* is the environment variable

**step 3** open *x64 Native Tools Command Prompt* for *VS2017*. Get into the directory of the unzipped *Boost*

**step 4** run command::

$ bootstrap.bat 

*b2.exe* and *bjam.exe* will be added after this step.
    
**step 5** open and modify *project-config.jam* in the current directory::
   

   $ import option ;
   $ using msvc : 14.1 : “E: \VS2017\VC\Tools\MSVC\14.1-.250117\bin\HostX64\x64\cl.exe” ;
   $ option.set keep-going : false ;


Please note that you should: i) use your own VS version number (e.g. 14.1) ii) use your own directory of _cl.exe_.

**step 6** *(note: this step may take 1 - 3 hours)* create a folder named build in the current directory, then run command:: 

   $ b2.exe stage --toolset=msvc-14.1 address-model=64 --stagedir="E:\mylib\boost\bin1.64.0\VC14.
   $ 1" threading=multi --build-type=complete --build-dir="E:\mylib\boost\boost_1_64_0\build"

Please note that you should: i) use your own directory names ii) be careful about the VS version number


* **leveldb.lib**

**step 1** download the source code of *LevelDB* for windows: https://github.com/vaskomitanov/leveldb  

**step 2** open *Visual Studio* (2017). Create a project: 

      •	file – new – project from existing code
      •	choose the type of project: *Visual C++*
      •	project file location: the directory of *LevelDB*
      •	project name: *LevelDB*
      •	project type: *LIB*
      •	preprocessor definition: *LEVELDB_PLATFORM_WINDOWS;OS_WIN; WIN32*
      •	include search path: *E:\LIB\leveldb-windows;E:\LIB\leveldb-windows\include* (the directory of *LevelDB* and its *include*)
      •	click *finish*

**step 3** set *LevelDB.lib*:

      •	project – properties – configuration properties – configuration type: *LIB*
      •	configuration properties – C/C++ - General – preprocessor – preprocessor definition: *LEVELDB_PLATFORM_WINDOWS;OS_WIN; WIN32*
      •	linker – general – additional library directory: *E:\LIB\boost64\stage\lib* (use your own path)

**step 4** open *solution explorer*, exclude the following files *(tip: you can search and right click on the target files)*:
      
      •	files ends with *_test.cc* and *_bench.cc*
      •	*port/port_android.cc*
      •	*port/port_posix.cc*
      •	*util/env_posix.cc*

**step 5** modify code:

in *db\c.cc*::
   
   $ delete #include < unistd.h>
   
in *port\port.h* - add the lines below before first ``#endif``::
      
   $ elif defined(LEVELDB_PLATFORM_WINDOWS)
   $ include "port/port_win.h"
   
**step 6** note: remember to compile as *release x64*


* **plyvel**

modify *setup.py*::
     
    Extension(
		    ...
		    libraries=['leveldb vaskomitanov-r x64',
				       'libboost_chrono-vc141-mt-x64-1_69',
				       'libboost_date_time-vc141-mt-x64-1_69',
				       'libboost_filesystem-vc141-mt-x64-1_69',
				       'libboost_system-vc141-mt-x64-1_69',
				       'libboost_thread-vc141-mt-x64-1_69'],
		    ...
        )
       
* **Cmake**

you may also need to install Cmake, please check the official guidance here: https://cgold.readthedocs.io/en/latest/first-step/installation.html#windows