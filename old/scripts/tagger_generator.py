#!/usr/bin/env python


import nltk.corpus, nltk.tag, itertools
import nltk.tag
from nltk.tag import brill
import sys
import cPickle as pickle


# corpora
brown_sents = nltk.corpus.brown.tagged_sents()
conll_sents = nltk.corpus.conll2000.tagged_sents()
treebank_sents = nltk.corpus.treebank.tagged_sents()

# training set
train_sents = brown_sents + treebank_sents + conll_sents


word_patterns = [
    (r'^-?[0-9]+(.[0-9]+)?$', 'CD'),
    (r'.*ould$', 'MD'),
    (r'.*ing$', 'VBG'),
    (r'.*ed$', 'VBD'),
    (r'.*ness$', 'NN'),
    (r'.*ment$', 'NN'),
    (r'.*ful$', 'JJ'),
    (r'.*ious$', 'JJ'),
    (r'.*ble$', 'JJ'),
    (r'.*ic$', 'JJ'),
    (r'.*ive$', 'JJ'),
    (r'.*ic$', 'JJ'),
    (r'.*est$', 'JJ'),
    (r'^a$', 'PREP'),
]


def backoff_tagger(tagged_sents, tagger_classes, backoff=None):
    if not backoff:
        backoff = tagger_classes[0](tagged_sents)
        del tagger_classes[0]
 
    for cls in tagger_classes:
        tagger = cls(tagged_sents, backoff=backoff)
        backoff = tagger
 
    return backoff


raubt_tagger = backoff_tagger(train_sents, [nltk.tag.AffixTagger,
    nltk.tag.UnigramTagger, nltk.tag.BigramTagger, nltk.tag.TrigramTagger],
    backoff=nltk.tag.RegexpTagger(word_patterns))

 
templates = [
    brill.SymmetricProximateTokensTemplate(brill.ProximateTagsRule, (1,1)),
    brill.SymmetricProximateTokensTemplate(brill.ProximateTagsRule, (2,2)),
    brill.SymmetricProximateTokensTemplate(brill.ProximateTagsRule, (1,2)),
    brill.SymmetricProximateTokensTemplate(brill.ProximateTagsRule, (1,3)),
    brill.SymmetricProximateTokensTemplate(brill.ProximateWordsRule, (1,1)),
    brill.SymmetricProximateTokensTemplate(brill.ProximateWordsRule, (2,2)),
    brill.SymmetricProximateTokensTemplate(brill.ProximateWordsRule, (1,2)),
    brill.SymmetricProximateTokensTemplate(brill.ProximateWordsRule, (1,3)),
    brill.ProximateTokensTemplate(brill.ProximateTagsRule, (-1, -1), (1,1)),
    brill.ProximateTokensTemplate(brill.ProximateWordsRule, (-1, -1), (1,1))
]


# train 
trainer = brill.FastBrillTaggerTrainer(raubt_tagger, templates)
braubt_tagger = trainer.train(train_sents, max_rules=100, min_score=3)


# pickle tagger
f = open('tagger.pickle', 'wb')
pickle.dump(braubt_tagger, f)
f.close()
