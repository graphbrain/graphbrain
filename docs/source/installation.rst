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

*To be done...*