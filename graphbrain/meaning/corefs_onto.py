import progressbar
from graphbrain import *
from graphbrain.meaning.ontology import subtypes
from graphbrain.meaning.corefs import make_corefs


def lemma_degrees(hg, edge):
    if edge.is_atom():
        roots = {edge.root()}

        # find lemma
        for edge in hg.pat2edges(hedge((const.lemma_pred, edge, '*'))):
            roots.add(edge[2].root())

        # compute degrees
        d = 0
        dd = 0
        for r in roots:
            atoms = set(hg.atoms_with_root(r))
            d += sum([hg.degree(atom) for atom in atoms])
            dd += sum([hg.deep_degree(atom) for atom in atoms])

        return d, dd
    else:
        return hg.degree(edge), hg.deep_degree(edge)


def root_deep_degree(hg, edge):
    if edge.is_atom():
        atoms = hg.atoms_with_root(edge.root())
        return sum([hg.deep_degree(atom) for atom in atoms])
    else:
        return hg.deep_degree(edge)


def generate(hg):
    count = 0
    edge_count = hg.edge_count()
    i = 0
    with progressbar.ProgressBar(max_value=edge_count) as bar:
        for edge in hg.all():
            if edge.type()[0] == 'c':
                subs = subtypes(hg, edge)

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
                    rdd = root_deep_degree(hg, edge)
                    sub_to_root_dd = \
                        0. if rdd == 0 else float(sdd) / float(rdd)
                    d = hg.degree(edge)
                    dd = hg.deep_degree(edge)
                    r = float(d) / float(dd)
                    ld, ldd = lemma_degrees(hg, edge)
                    lr = float(ld) / float(ldd)

                    # use metric to decide
                    if (rdd > 5 and max_ratio >= .7 and r >= .05 and
                            lr >= .05 and sub_to_root_dd >= .1 and
                            (not edge.is_atom() or len(edge.root()) > 2)):

                        make_corefs(hg, edge, subs[best_pos])
                        count += 1
            if i < edge_count:
                i += 1
                bar.update(i)
    return count
