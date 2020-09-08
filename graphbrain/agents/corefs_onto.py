from graphbrain.meaning.ontology import subtypes
from graphbrain.meaning.corefs import make_corefs_ops
from graphbrain.agents.agent import Agent


class CorefsOnto(Agent):
    def __init__(self):
        super().__init__()
        self.corefs = 0

    def name(self):
        return 'corefs_onto'

    def on_start(self):
        self.corefs = 0

    def input_edge(self, edge):
        hg = self.system.get_hg(self)

        if edge.type()[0] == 'C':
            subs = tuple(subtypes(hg, edge))

            # check if the concept should be assigned to a synonym set
            if len(subs) > 0:
                # find set with the highest degree and normalize set
                # degrees by total degree
                sub_degs = [hg.deep_degree(sub) for sub in subs]
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
                dd = hg.deep_degree(edge)

                if dd > sdd:
                    sdd_dd = float(sdd) / float(dd)
                    if max_ratio >= .7 and sdd_dd < .5:
                        # print(edge.to_str())
                        # print(subs)
                        # print('# subs: {}'.format(len(subs)))
                        # print('max_ratio: {}'.format(max_ratio))
                        # print('sdd: {}'.format(sdd))
                        # print('dd: {}'.format(dd))
                        # print('sdd_dd: {}'.format(sdd_dd))

                        self.corefs += 1
                        for op in make_corefs_ops(hg, edge, subs[best_pos]):
                            yield op

    def report(self):
        return '{} coreferences were added.'.format(str(self.corefs))
