GraphBrain is a software project aimed at the research of human knowledge as a collective phenomena. More specifically, it focuses on digital media and the application of computational approaches to the study of the relatively recent public space enabled by Internet technologies.

This project is aligned with a fundamentally interdisciplinary research program, that draws from fields such as the Social Sciences, Artificial Intelligence and Epistemology.

In its current state, Graphbrain provides the following:

* An hypergraph knowledge base, capable of representing and efficiently querying complex relationships between entities
* Knowledge extraction algorithms for popular data sets such as WordNet and DBPedia
* Entity disambiguation algorithms
* An entity extraction algorithm for free-form text, such as news items and blog posts
* A domain specific language to define knowledge extraction rules for free-form text
* A web-based interface to navigate the knowledge graph

The main ambition of this software project is to provide a robust base upon which new methods of socio-semantic analysis of digital media can be easily defined. At its core, GraphBrain contains an expressive knowledge representation system that easily lends itself to the organization of entities in terms of relationships, sources and conflicting beliefs.

The GraphBrain knowledge graph is designed to avoid notions of ground truth, instead allowing for the representation of all beliefs as relative to a source. Then, it is possible to define algorithms to determine consensus according to certain criteria or methods. This matches the current situation of digital spaces, where a very large amount of information is available, but knowledge discovery by human actors is constrained by centralized algorithms with poorly understood dynamics (e.g. search engines and recommendation systems).

Beyond the scientific study of collective knowledge phenomena in digital media, GraphBrain could enable the creation of systems of knowledge exploration that are less centralized, less dependent of private actors and more transparent to the users.

# Installation #

For now, these instructions are just for macOS. Installing on Linux should be quite similar. Everything is done on the terminal.

## Prerequisites ##

Some of these prerequisites may be already installed. In that case, just skip the appropriate step.

### XCode ###

Use the app store.

### Command Line Tools ###

    $ xcode-select --install

### Homebrew (macOS package manager) ###

http://brew.sh/

### Python 3 ###

    $ brew install python3

### pip (Python package manager) ###

    $ sudo easy_install pip

### virtualenv (Virtual Python Environment builder) ###

    $ sudo -H pip install virtualenv

### FreeType (library) ###

    $ brew install freetype

## Download, build and install ##

For now we only distribute GraphBrain as source code from our git repository.
Start by cloning the source code to your current local directory.

    $ git clone https://github.com/graphbrain/graphbrain.git
    $ cd graphbrain

It is advisable to work with virtual environments. To create one in the current directory you can do this:

    $ virtualenv -p /usr/local/bin/python3 venv

Then to activate it:

    $ source venv/bin/activate
    
Now we can build and install GraphBrain:

    $ pip install --editable .
    
It is still necessary to download spacy data files:
    $ python -m spacy.en.download all

## Run tests ##

Use the script provided:

    $ ./run_tests.sh
