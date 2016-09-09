#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


from nltk.corpus import wordnet as wn
import gb.hypergraph.symbol as sym


def lemma2symbol(lemma):
    lemma_id = 'wn.%s' % lemma.synset().name()
    return sym.build([lemma.name(), lemma_id])


def pos2symbol(pos):
    if pos == 'n':
        return 'noun/gb'
    if pos == 'v':
        return 'verb/gb'
    if (pos == 'a') or (pos == 's'):
        return 'adjective/gb'
    if pos == 'r':
        return 'adverb/gb'


def add_relationship(hg, rel, lemma1, lemma2):
    edge = (rel, lemma2symbol(lemma1), lemma2symbol(lemma2))
    # print(edge)
    hg.add_belief(u'wordnet/gb', edge)


def process_relationships(hg, root_lemma, synsets, rel):
    for s in synsets:
        lemma = s.lemmas()[0]
        add_relationship(hg, rel, root_lemma, lemma)


def process_lemma_relationships(hg, root_lemma, lemmas, rel):
    for lemma in lemmas:
        add_relationship(hg, rel, root_lemma, lemma)


def process_synset(hg, synset):
    print('synset: %s' % synset.name())

    lemmas = synset.lemmas()
    root_lemma = lemmas[0]

    # process part-of-speech
    pos = pos2symbol(synset.pos())
    edge = (u'pos/gb', lemma2symbol(root_lemma), pos)
    # print(edge)
    hg.add_belief(u'wordnet/gb', edge)

    # process synonyms
    for lemma in lemmas[1:]:
        add_relationship(hg, u'synonym/gb', root_lemma, lemma)

    # process other synset relationships
    process_relationships(hg, root_lemma, synset.hypernyms(), u'hypernym/gb')
    process_relationships(hg, root_lemma, synset.instance_hypernyms(), u'instance_hypernym/gb')
    process_relationships(hg, root_lemma, synset.member_meronyms(), u'member_meronym/gb')
    process_relationships(hg, root_lemma, synset.attributes(), u'attribute/gb')
    process_relationships(hg, root_lemma, synset.entailments(), u'entailment/gb')
    process_relationships(hg, root_lemma, synset.causes(), u'cause/gb')
    process_relationships(hg, root_lemma, synset.also_sees(), u'also_see/gb')
    process_relationships(hg, root_lemma, synset.verb_groups(), u'verb_group/gb')
    process_relationships(hg, root_lemma, synset.similar_tos(), u'similar_to/gb')

    # process lemma relationships
    for lemma in lemmas:
        process_lemma_relationships(hg, lemma, lemma.antonyms(), u'antonym/gb')
        process_lemma_relationships(hg, lemma, lemma.pertainyms(), u'pertainym/gb')
        process_lemma_relationships(hg, lemma, lemma.derivationally_related_forms(), u'derivationally_related_form/gb')


def read(hg):
    for synset in list(wn.all_synsets()):
        process_synset(hg, synset)
