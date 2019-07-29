from collections import Counter
from graphbrain import *
from graphbrain.meaning.corefs import main_coref
from graphbrain.agents.agent import Agent


class Claims(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        self.actors = None
        self.female = None
        self.male = None
        self.non_human = None
        self.actor_counter = None
        self.female_counter = None
        self.male_counter = None
        self.non_human_counter = None

    def name(self):
        return 'claims'

    def languages(self):
        return {'en'}

    def _pred_lemma(self, pred):
        if pred.is_atom():
            for edge in self.hg.search((const.lemma_pred, pred, '*')):
                return edge[2]
            return pred
        else:
            return self._pred_lemma(pred[1])

    def start(self):
        self.actor_counter = Counter()
        self.female_counter = Counter()
        self.male_counter = Counter()
        self.non_human_counter = Counter()

    def input_edge(self, edge):
        if not edge.is_atom():
            ct = edge.connector_type()
            if ct[:2] == 'pd':
                pred = edge[0]
                if (len(edge) > 2 and self._pred_lemma(pred).root() == 'say'):
                    subjects = edge.edges_with_argrole('s')
                    claims = edge.edges_with_argrole('r')
                    if len(subjects) == 1 and len(claims) >= 1:
                        subject = main_coref(self.hg, subjects[0])
                        self.actor_counter[subject] += 1
                        for claim in claims:
                            claim_subjects = claim.edges_with_argrole('s')
                            if len(claim_subjects) == 1:
                                claim_subject = claim_subjects[0]
                                if claim_subject.type() == 'ci':
                                    atom = claim_subject.atom_with_type('c')
                                    ind = atom.root()
                                    if ind == 'she':
                                        self.female_counter[subject] += 1
                                    elif ind == 'he':
                                        self.male_counter[subject] += 1
                                    elif ind == 'it':
                                        self.non_human_counter[subject] += 1

    def _gender(self, actor):
        counts = (('female', self.female_counter[actor]),
                  ('male', self.male_counter[actor]),
                  ('non-human', self.non_human_counter[actor]))
        counts = sorted(counts, key=lambda x: x[1], reverse=True)
        if counts[0][1] > 0 and counts[0][1] > counts[1][1]:
            return counts[0][0]
        else:
            return None

    def end(self):
        self.actors = set()
        self.female = set()
        self.male = set()
        self.non_human = set()

        for actor in self.actor_counter:
            if self.actor_counter[actor] > 1:
                self.actors.add(actor)
                g = self._gender(actor)
                if g == 'female':
                    self.female.add(actor)
                elif g == 'male':
                    self.male.add(actor)
                elif g == 'non-human':
                    self.non_human.add(actor)

        for agent, count in self.actor_counter.most_common(250):
            atype = '?'
            if agent in self.female:
                atype = 'female'
            elif agent in self.male:
                atype = 'male'
            elif agent in self.non_human:
                atype = 'non-human'
            print('{} ({}) [{}]'.format(agent, atype, str(count)))

    def report(self):
        counts = (len(self.actors), len(self.female), len(self.male),
                  len(self.non_human))
        cs = tuple([str(x) for x in counts])
        return 'actors: {}; female: {}; male: {}; non-human: {}'.format(*cs)


if __name__ == '__main__':
    hg = hypergraph('reddit-worldnews-01012013-01082017-corefs.hg')
    ms = Claims(hg)
    ms.run()
