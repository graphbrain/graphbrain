from graphbrain import *
from graphbrain.agents.agent import Agent


def pred_lemma(pred):
    if pred.is_atom():
        for edge in hg.search((const.lemma_pred, pred, '*')):
            return edge[2]
        return pred
    else:
        return pred_lemma(pred[1])


class Claims(Agent):
    def __init__(self, hg):
        super().__init__(hg)

    def name(self):
        return 'claims'

    def languages(self):
        return {'en'}

    def input_edge(self, edge):
        if not edge.is_atom():
            ct = edge.connector_type()
            if ct[:2] == 'pd':
                pred = edge[0]
                if (len(edge) > 2 and pred_lemma(pred).root() == 'say'):
                    subjects = edge.edges_with_argrole('s')
                    claims = edge.edges_with_argrole('r')
                    if len(subjects) == 1 and len(claims) >= 1:
                        subject = subjects[0].to_str()
                        for claim in claims:
                            claim_subjects = claim.edges_with_argrole('s')
                            if len(claim_subjects) == 1:
                                claim_subject = claim_subjects[0]
                                if claim_subject.type() == 'ci':
                                    atom = claim_subject.atom_with_type('c')
                                    ind = atom.root()
                                    if ind == 'he':
                                        print('MAN {}'.format(subject))
                                    elif ind == 'she':
                                        print('WOMAN {}'.format(subject))
                                    elif ind == 'it':
                                        print('THING {}'.format(subject))


if __name__ == '__main__':
    hg = hypergraph('reddit-worldnews-01012013-01082017-corefs.hg')
    ms = Claims(hg)
    ms.run()
    print(ms.matches)
