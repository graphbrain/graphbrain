# This is a rudimentary Makefile for rebuilding Stanford CoreNLP.
# We actually use ant (q.v.) or a Java IDE.

JAVAC = javac
JAVAFLAGS = -O -d classes

# Builds the classes' jar file
# Before using this, unjar the source jar file in the 'src' directory
corenlp:
	mkdir -p classes
	$(JAVAC) $(JAVAFLAGS) src/edu/stanford/nlp/*/*.java \
	    src/edu/stanford/nlp/*/*/*.java \
	    src/edu/stanford/nlp/*/*/*/*.java
	cd classes ; jar -cfm ../stanford-corenlp-`date +%Y-%m-%d`.jar ../src/edu/stanford/nlp/pipeline/stanford-corenlp-manifest.txt edu ; cd ..
	cp stanford-corenlp-`date +%Y-%m-%d`.jar stanford-corenlp.jar
	rm -rf classes
