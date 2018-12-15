=======
Install
=======

For now, these instructions are just for macOS and Linux. Everything is done on the terminal.
It should be possible to install GraphBrain on a Windows machine. If you are a Windows user and would like to help,
don't hesitate to contact us.

Prerequisites (macOS)
=====================

Some of these prerequisites may be already installed. In that case, just skip the appropriate step.

XCode
-----

Use the app store.

Command Line Tools
------------------

::

   $ xcode-select --install

Homebrew (macOS package manager)
--------------------------------

http://brew.sh/


Python 3
--------

::

   $ brew install python3

pip (Python package manager)
----------------------------

::

   $ sudo easy_install pip

virtualenv (Virtual Python Environment builder)
-----------------------------------------------

::

   $ sudo -H pip install virtualenv

FreeType (library)
------------------

::

   $ brew install freetype

LevelDB (database engine)
-------------------------

::

   $ brew install leveldb

Prerequisites (Linux)
=====================

Linux distributions are diverse in terms of package managers. Please use the appropriate package manager for your
distribution to install the prerequisites. Naturally, you can ignore the prerequisites that are already installed in
your machine.

If you have specific details for your distribution, don't hesitate to contact us.

* gcc
* Python 3
* pip (Python package manager)
* virtualenv (Virtual Python Environment builder)
* freetype
* LevelDB

Download, build and install
===========================

For now we only distribute GraphBrain as source code from our git repository.
Start by cloning the source code to your current local directory.
::

   $ git clone https://github.com/graphbrain/graphbrain.git
   $ cd graphbrain

It is advisable to work with virtual environments. To create one in the current directory you can do this::

   $ virtualenv -p <path to python3> venv

Common locations for python3:

* /usr/local/bin/python3
* /usr/bin/python3

If you are on macOS, do this instead (for the reason `explained here <http://matplotlib.org/faq/osx_framework.html#osxframework-faq>`_)::

   python3 -m venv venv

Then to activate it::

   $ source venv/bin/activate

Install Cython::

   $ pip install cython

Now we can build and install GraphBrain::

   $ pip install --editable .

Add these lines to your ~/.bash_profile::

   export LC_ALL=en_US.UTF-8
   export LANG=en_US.UTF-8

It is still necessary to download spacy models::

   $ python -m spacy download en_core_web_lg

Run tests
=========

Use the script provided::

  $ ./run_tests.sh

Known Issues and Solutions
==========================

LevelDB errors on macOS
-----------------------

https://github.com/wbolster/plyvel/issues/27#issuecomment-188709935

Solution::

   $ pip uninstall plyvel
   $ CFLAGS='-mmacosx-version-min=10.7 -stdlib=libc++' pip install --no-use-wheel plyvel

ValueError: unknown locale: UTF-8
---------------------------------

https://stackoverflow.com/questions/19961239/pelican-3-3-pelican-quickstart-error-valueerror-unknown-locale-utf-8

Solution::

   $ export LC_ALL=en_US.UTF-8
   $ export LANG=en_US.UTF-8

Unicode errors when using the Interactive Edge Generator
--------------------------------------------------------

Example::

   UnicodeEncodeError: 'utf-8' codec can't encode characters in position 5-6: surrogates not allowed

Solution::

   export PYTHONIOENCODING=utf-8

