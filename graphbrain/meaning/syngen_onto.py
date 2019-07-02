import progressbar
from graphbrain import *
from graphbrain.meaning.ontology import subtypes
from graphbrain.meaning.synonyms import make_synonyms


def lemma_degrees(hg, ent):
    if is_edge(ent):
        return hg.degree(ent), hg.deep_degree(ent)

    roots = {root(ent)}

    # find lemma
    for edge in hg.pat2ents((const.lemma_pred, ent, '*')):
        roots.add(root(edge[2]))

    # compute degrees
    d = 0
    dd = 0
    for r in roots:
        atoms = set(hg.atoms_with_root(r))
        d += sum([hg.degree(atom) for atom in atoms])
        dd += sum([hg.deep_degree(atom) for atom in atoms])

    return d, dd


def root_deep_degree(hg, ent):
    if is_edge(ent):
        return hg.deep_degree(ent)

    atoms = hg.atoms_with_root(root(ent))
    return sum([hg.deep_degree(atom) for atom in atoms])


def generate(hg):
    count = 0
    ent_count = hg.atom_count() + hg.edge_count() + 1
    i = 0
    with progressbar.ProgressBar(max_value=ent_count) as bar:
        for ent in hg.all():
            if entity_type(ent)[0] == 'c':
                subs = subtypes(hg, ent)

                # check if the concept should be assigned to a synonym set
                if len(subs) > 0:
                    # find set with the highest degree and normalize set
                    # degrees by total degree
                    sub_degs = [hg.degree(sub) for sub in subs]
                    total_deg = sum(sub_degs)
                    total_deg = 1 if total_deg == 0 else total_deg
                    sub_ratios = [sub_deg / total_deg for sub_deg in sub_degs]
                    max_ratio = 0.
                    best_pos = -1
                    for pos, ratio in enumerate(sub_ratios):
                        if ratio > max_ratio:
                            max_ratio = ratio
                            best_pos = pos

                    # compute some degree-related metrics
                    sdd = hg.deep_degree(subs[best_pos])
                    rdd = root_deep_degree(hg, ent)
                    sub_to_root_dd = \
                        0. if rdd == 0 else float(sdd) / float(rdd)
                    d = hg.degree(ent)
                    dd = hg.deep_degree(ent)
                    r = float(d) / float(dd)
                    ld, ldd = lemma_degrees(hg, ent)
                    lr = float(ld) / float(ldd)

                    # use metric to decide
                    if (rdd > 5 and max_ratio >= .7 and r >= .05 and
                            lr >= .05 and sub_to_root_dd >= .1 and
                            (is_edge(ent) or len(root(ent)) > 2)):

                        make_synonyms(hg, ent, subs[best_pos])
                        count += 1
                        # print('\n++++++====== {} ======++++++'.format(ent))
                        # print('SYNONYM: {}'.format(str(subs[best_pos])))
                        # print('root deep degree: {}'.format(rdd))
                        # print('sub/root ddegree: {}'.format(sub_to_root_dd))
                        # print('degree: {}; deep degree: {}; '
                        #       'ratio: {}'.format(d, dd, r))
                        # print('sub deep degree: {}'.format(sdd))
                        # print('lemma degree: {}; lemma deep degree: {};'
                        #       ' lemma ratio: {}'.format(ld, ldd, lr))
            i += 1
            bar.update(i)
    return count
