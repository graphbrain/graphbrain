============
Installation
============

Graphbrain is compatible with 64-bit CPython >=3.6, <3.9 and runs on macOS, Linux and Windows. 

We currently have binary releases for macOS only. Given a working build environment, installation on Linux should be equally simple. Installation on Windows is at the moment less straightforward, because of our dependency on LevelDB, which in turn requires a bit of effort to install in this OS. `For such cases or to build from the sources, check the prerequisites for your OS <#prerequisites>`_.

Make sure you are using Python 3, with version >=3.6, <3.9.

Install with pip (recommended)
==============================

::

   $ pip install graphbrain


It is advisable to work with virtual environments. This avoids conflicts with the system-wide installed packages and creates a nice self-contained environment for you to work on. It is particularly important with graphbrain because we maintain dependencies to specific versions of downstream machine learning tools, as to keep results consistent.

To create a virtual environment in the current directory you can do this::

   $ virtualenv -p <path to python3> venv

Common locations for python3:

* /usr/local/bin/python3
* /usr/bin/python3

If you are on macOS, do this instead (for the reason `explained here <http://matplotlib.org/faq/osx_framework.html#osxframework-faq>`_)::

   $ python3 -m venv venv

Then to switch to the virtual environment and install graphbrain::

   $ source venv/bin/activate
   $ pip install graphbrain

It is still necessary to download a spaCy language model::

   $ python -m spacy download en_core_web_lg


Install from the code repository (github)
=========================================

Another way to install graphbrain is to build it from the GitHub repository source. You should use this method if you wish to make changes to the code base.

Start by cloning the source code to your current local directory.
::

   $ git clone https://github.com/graphbrain/graphbrain.git
   $ cd graphbrain

It is advisable to work with virtual environments, for reasons that are explained in the previous section. Please refer to it for details.

Install Cython::

   $ pip install cython

Now we can build and install graphbrain::

   $ pip install .

It is then necessary to download a spaCy language model::

   $ python -m spacy download en_core_web_lg


Building Graphbrain with parser-level coreference resolution support
--------------------------------------------------------------------

Graphbrain's parser-level coreference resolution support relies on neuralcoref. This library currently introduces some aditional restrictions on the dependencies:

* Python version == 3.7.*
* spaCy version >=2.1.0, <3.0.0

To accomodate this, the best solution is to install an appropriate spaCy version first::

   $ pip install spacy==2.3.7

It is still necessary to download a spaCy language model::

   $ python -m spacy download en_core_web_lg

The next step is to install neuralcoref from source, follwing the instructions in the repository:
https://github.com/huggingface/neuralcoref

Finally, Graphbrain can be installed from source, as per above.

Building Graphbrain with support for LevelDB hypergraph databases
-----------------------------------------------------------------

If you want to build graphbrain with support for LevelDB hypergraph databases, you should set the ``LEVELDB`` environment variable when building::

   $ LEVELDB=true pip install .

For developers
--------------

If you want to help develop graphbrain, you should also install the development dependencies::

   $ pip install '.[dev]'

Run tests
---------

From the project's root directory::

  $ pytest graphbrain

Notice that this requires the `pytest` package to be installed. The `pytest` package is included in the above mentioned development dependencies.

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

* Python 3

If python 3 is not already included with your current version os macOS::

   $ brew install python3

* pip (Python package manager)::

   $ sudo easy_install pip

* virtualenv (Virtual Python Environment builder)::

   $ sudo -H pip install virtualenv