from nltk.corpus import wordnet as wn
from graphbrain.funs import *
import graphbrain.constants as const


def lemma2symbol(lemma):
    lemma_id = 'wn.%s' % lemma.synset().name()
    return build_symbol(lemma.name().lower(), lemma_id)


def pos2symbol(pos):
    if pos == 'n':
        return const.noun
    if pos == 'v':
        return const.verb
    if (pos == 'a') or (pos == 's'):
        return const.adjective
    if pos == 'r':
        return const.adverb


def add_relationship(hg, rel, lemma1, lemma2):
    edge = (rel, lemma2symbol(lemma1), lemma2symbol(lemma2))
    # print(edge)
    hg.add_belief(const.wordnet, edge)


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
    edge = (const.pos, lemma2symbol(root_lemma), pos)
    # print(edge)
    hg.add_belief(const.wordnet, edge)

    # process synonyms
    for lemma in lemmas[1:]:
        add_relationship(hg, const.are_synonyms, root_lemma, lemma)

    # process other synset relationships
    process_relationships(hg, root_lemma, synset.hypernyms(), const.is_type_of)
    process_relationships(hg, root_lemma, synset.instance_hypernyms(), const.is_instance_of)
    process_relationships(hg, root_lemma, synset.part_meronyms(), const.has_part)
    process_relationships(hg, root_lemma, synset.member_meronyms(), const.has_member)
    process_relationships(hg, root_lemma, synset.substance_meronyms(), const.has_substance)
    process_relationships(hg, root_lemma, synset.attributes(), const.has_attribute)
    process_relationships(hg, root_lemma, synset.entailments(), const.entails)
    process_relationships(hg, root_lemma, synset.causes(), const.causes)
    process_relationships(hg, root_lemma, synset.also_sees(), const.are_related)
    process_relationships(hg, root_lemma, synset.verb_groups(), const.verb_group)
    process_relationships(hg, root_lemma, synset.similar_tos(), const.are_similar)

    # process lemma relationships
    for lemma in lemmas:
        process_lemma_relationships(hg, lemma, lemma.antonyms(), const.are_antonyms)
        process_lemma_relationships(hg, lemma, lemma.pertainyms(), const.pertains_to)
        process_lemma_relationships(hg, lemma, lemma.derivationally_related_forms(),
                                    const.has_derivationally_related_form)


def read(hg):
    for synset in list(wn.all_synsets()):
        process_synset(hg, synset)
