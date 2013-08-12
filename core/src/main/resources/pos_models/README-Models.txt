Stanford POS Tagger, v. 3.1.0 - 2011-12-16
Copyright (c) 2002-2011 The Board of Trustees of
The Leland Stanford Junior University. All Rights Reserved.

This document contains (some) information about the models included in
this release and that may be downloaded for the POS tagger website at
http://nlp.stanford.edu/software/tagger.shtml .  If you have downloaded
the full tagger, all of the models mentioned in this document are in the
downloaded package in the same directory as this readme.  Otherwise,
included in the download are two 
English taggers, and the other taggers may be downloaded from the
website.  All taggers are accompanied by the props files used to create
them; please examine these files for more detailed information about the
creation of the taggers.

For English, the bidirectional taggers are slightly more accurate, but
tag much more slowly; choose the appropriate tagger based on your
speed/performance needs.

English taggers
---------------------------
bidirectional-distsim-wsj-0-18.tagger
Trained on WSJ sections 0-18 using a bidirectional architecture and
including word shape and distributional similarity features.
Penn Treebank tagset.
Performance:
97.28% correct on WSJ 19-21
(90.46% correct on unknown words)

left3words-wsj-0-18.tagger
Trained on WSJ sections 0-18 using the left3words architecture and
includes word shape features.  Penn tagset.
Performance:
96.97% correct on WSJ 19-21
(88.85% correct on unknown words)

left3words-distsim-wsj-0-18.tagger
Trained on WSJ sections 0-18 using the left3words architecture and
includes word shape and distributional similarity features. Penn tagset.
Performance:
97.01% correct on WSJ 19-21
(89.81% correct on unknown words)


Chinese tagger
---------------------------
chinese.tagger
Trained on a combination of Chinese Treebank texts from Chinese and Hong
Kong sources. 
LDC Chinese Treebank POS tag set.
Performance:
94.13% on a combination of Chinese and Hong Kong texts
(78.92% on unknown words)

Arabic tagger
---------------------------
arabic-accurate.tagger
Trained on the *entire* ATB p1-3.
When trained on the train part of the ATB p1-3 split done for the 2005
JHU Summer Workshop (Diab split), using (augmented) Bies tags, it gets
the following performance:
Performance:
96.50% on dev portion according to Diab split
(80.59% on unknown words)

arabic-fast.tagger
4x speed improvement over "accurate".
Performance:
96.34% on dev portion according to Diab split
(80.28% on unknown words)


French tagger
---------------------------
french.tagger
Trained on the French treebank.

German tagger
---------------------------
german-hgc.tagger
Trained on the first 80% of the Negra corpus, which uses the STTS tagset.
The Stuttgart-Tübingen Tagset (STTS) is a set of 54 tags for annotating
German text corpora with part-of-speech labels, which was jointly
developed by the Institut für maschinelle Sprachverarbeitung of the
University of Stuttgart and the Seminar für Sprachwissenschaft of the
University of Tübingen. See: 
http://www.ims.uni-stuttgart.de/projekte/CQPDemos/Bundestag/help-tagset.html
This model uses features from the distributional similarity clusters
built over the HGC.
Performance:
96.90% on the first half of the remaining 20% of the Negra corpus (dev set)
(90.33% on unknown words)

german-dewac.tagger
This model uses features from the distributional similarity clusters
built from the deWac web corpus.

german-fast.tagger
Lacks distributional similarity features, but is several times faster
than the other alternatives.
Performance:
96.61% overall / 86.72% unknown.
