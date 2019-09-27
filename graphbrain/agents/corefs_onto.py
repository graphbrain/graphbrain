from graphbrain.meaning.ontology import subtypes
from graphbrain.meaning.corefs import make_corefs
from graphbrain.meaning.lemmas import lemma_degrees
from graphbrain.agents.agent import Agent


class CorefsOnto(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        self.corefs = 0

    def name(self):
        return 'corefs_onto'

    def languages(self):
        return set()

    def start(self):
        self.corefs = 0

    def input_edge(self, edge):
        if edge.type()[0] == 'c':
            subs = tuple(subtypes(self.hg, edge))

            # check if the concept should be assigned to a synonym set
            if len(subs) > 0:
                # find set with the highest degree and normalize set
                # degrees by total degree
                sub_degs = [self.hg.degree(sub) for sub in subs]
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
                sdd = self.hg.deep_degree(subs[best_pos])
                _, rdd = self.hg.root_degrees(edge)
                sub_to_root_dd = \
                    0. if rdd == 0 else float(sdd) / float(rdd)
                d = self.hg.degree(edge)
                dd = self.hg.deep_degree(edge)
                r = float(d) / float(dd)
                ld, ldd = lemma_degrees(self.hg, edge)
                lr = float(ld) / float(ldd)

                # use metric to decide
                if (rdd > 5 and max_ratio >= .7 and r >= .05 and
                        lr >= .05 and sub_to_root_dd >= .1 and
                        (not edge.is_atom() or len(edge.root()) > 2)):

                    make_corefs(self.hg, edge, subs[best_pos])
                    self.corefs += 1

    def report(self):
        return '{} coreferences were added.'.format(str(self.corefs))
