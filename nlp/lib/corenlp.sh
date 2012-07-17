#!/usr/bin/env bash
#
# Runs Stanford CoreNLP.
# Simple uses for xml and plain text output to files are:
#    ./corenlp.sh -file filename
#    ./corenlp.sh -file filename -outputFormat text 

scriptdir=`dirname $0`

echo -mx3g -cp "$scriptdir/*.jar" edu.stanford.nlp.pipeline.StanfordCoreNLP $*
java -mx3g -cp "$scriptdir/*.jar" edu.stanford.nlp.pipeline.StanfordCoreNLP $*
