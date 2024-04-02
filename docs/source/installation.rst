============
Installation
============

Graphbrain requires Python >=3.9 and runs on macOS, Linux and Windows.

We currently have binary releases for macOS only. Given a working build environment, installation on Linux should be equally simple. Installation on Windows is at the moment less straightforward, because of our dependency on LevelDB, which in turn requires a bit of effort to install in this OS. `For such cases or to build from the sources, check the prerequisites for your OS <#prerequisites>`_.

.. warning::
   *Important for macOS users:* You will need to install a specific version of LevelDB before installing Graphbrain. `Please refer to the prerequisites section below for details <#macos>`_.

Install with pip
================

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

Choose language model
---------------------

It is still necessary to download a spaCy language model. Graphbrain supports both the transformer model (modern, GPU optimized) and a CPU optimized model. The transformer model can be installed in the following way::

   $ python -m spacy download en_core_web_trf

Or, to install the CPU optimized model (a faster option if your computer does not have a GPU)::

   $ python -m spacy download en_core_web_lg

In case both models are present, Graphbrain will use the transformer model. A spaCy language model can be uninstalled as a regular pip package, for example::

   $ pip uninstall en_core_web_trf

Coreference resolution
----------------------

If you want to perform coreference resolution, you will need to install the transformer model and also the following model::

   pip install https://github.com/explosion/spacy-experimental/releases/download/v0.6.1/en_coreference_web_trf-3.4.0a2-py3-none-any.whl


Coreference resolution currently requires version 0.6.1 of the package ``spacy-experimental`` (`more info <https://github.com/explosion/spacy-experimental/releases/tag/v0.6.1>`_). Graphbrain requirements specify this version, but if you run into problems make sure you have the correct one installed.

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

It is then necessary to download a spaCy language model and optionally the coreference resolution model, as detailed above.

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

* LevelDB version 1.22

This version of LevelDB is no longer supported by Homebrew, but it is possible to install it using a custom formula that we provide::

   $ brew tap graphbrain/homebrew-formulae git@github.com:graphbrain/homebrew-formulae.git
   $ brew extract --version=1.22 leveldb graphbrain/formulae
   $ brew install leveldb@1.22

(this formula was forked from this repository: https://github.com/bagonyi/homebrew-formulae, our thanks to David Bagonyi for creating it!)

Users with M-Series Macs must add the homebrew path (to .bashrc or .zshrc) (see https://github.com/wbolster/plyvel/issues/100#issuecomment-1162625134)::

   $ export LIBRARY_PATH="$LIBRARY_PATH:$(brew --prefix)/lib"
   $ export CPATH="$CPATH:$(brew --prefix)/include"
