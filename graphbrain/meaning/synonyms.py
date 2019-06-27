import random
import string
from graphbrain.constants import (synonym_pred, syn_set_id_key,
                                  main_synonym_pred)


def _new_syn_id():
    chars = string.ascii_letters + string.digits
    # Note: the size of the id can be increased to reduce the probability
    # of collision.
    return ''.join(random.choice(chars) for i in range(5))


def _set_syn_id(hg, ent, synid):
    hg.set_attribute(ent, syn_set_id_key, synid)


def _change_syn_id(hg, ent, synid):
    for syn in synonym_set(hg, ent):
        _set_syn_id(hg, syn, synid)


def _update_main_syn(hg, ent):
    synid = syn_id(hg, ent)
    synonyms = synonym_set(hg, ent)

    best_synonym = None
    best_degree = -1
    for syn in synonyms:
        d = hg.degree(syn)
        if d > best_degree:
            best_degree = d
            best_synonym = syn

    edge = (main_synonym_pred, synid, best_synonym)
    if not hg.exists(edge):
        old = set(hg.pat2ents((main_synonym_pred, synid, '*')))
        for old_edge in old:
            hg.remove(old_edge)
        hg.add(edge, primary=False)


def synonym_set(hg, ent, synonyms=None):
    if synonyms is None:
        synonyms = {ent}
    for edge in hg.edges_with_ents([synonym_pred, ent]):
        if len(edge) == 3 and edge[0] == synonym_pred:
            for item in edge[1:]:
                if item not in synonyms:
                    synonyms.add(item)
                    synonym_set(hg, item, synonyms)
    return synonyms


def are_synonyms(hg, ent1, ent2, synonyms=None):
    if synonyms is None:
        synonyms = {ent1}
    for edge in hg.edges_with_ents([synonym_pred, ent1]):
        if len(edge) == 3 and edge[0] == synonym_pred:
            for item in edge[1:]:
                if item not in synonyms:
                    if item == ent2:
                        return True
                    synonyms.add(item)
                    if are_synonyms(hg, item, ent2, synonyms):
                        return True
    return False


def syn_id(hg, ent):
    return hg.get_str_attribute(ent, syn_set_id_key)


def main_syn(hg, ent):
    synid = syn_id(hg, ent)
    if synid is None:
        return ent
    for edge in hg.pat2ents((main_synonym_pred, synid, '*')):
        return edge[2]
    return None


def make_synonyms(hg, ent1, ent2):
    syn_id_1 = syn_id(hg, ent1)
    syn_id_2 = syn_id(hg, ent2)

    if syn_id_1 is None:
        if syn_id_2 is None:
            new_syn_id = _new_syn_id()
        else:
            new_syn_id = syn_id_2
    elif syn_id_2 is None:
        new_syn_id = syn_id_1
    else:
        count1 = len(synonym_set(hg, ent1))
        count2 = len(synonym_set(hg, ent2))
        if count2 > count1:
            new_syn_id = syn_id_2
        else:
            new_syn_id = syn_id_1

    update = False
    if syn_id_1 != new_syn_id:
        _change_syn_id(hg, ent1, new_syn_id)
        update = True
    if syn_id_2 != new_syn_id:
        _change_syn_id(hg, ent2, new_syn_id)
        update = True

    hg.add((synonym_pred, ent1, ent2), primary=False)

    if update:
        _update_main_syn(hg, ent1)
