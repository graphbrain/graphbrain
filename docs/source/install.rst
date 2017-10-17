=======
Install
=======

For now, these instructions are just for macOS. Installing on Linux should be quite similar. Everything is done on the terminal.

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

Download, build and install
===========================

For now we only distribute GraphBrain as source code from our git repository.
Start by cloning the source code to your current local directory.
::

   $ git clone https://github.com/graphbrain/graphbrain.git
   $ cd graphbrain

It is advisable to work with virtual environments. To create one in the current directory you can do this::

   $ virtualenv -p /usr/local/bin/python3 venv

If you are on macOS, do this instead (for the reason `explained here <http://matplotlib.org/faq/osx_framework.html#osxframework-faq>`_)::

   python3 -m venv venv

Then to activate it::

   $ source venv/bin/activate

Now we can build and install GraphBrain::

   $ pip install --editable .

It is still necessary to download spacy data files::

   $ python -m spacy.en.download all

Run tests
=========

Use the script provided::

  $ ./run_tests.sh
