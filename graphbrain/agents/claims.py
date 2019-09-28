from collections import Counter
import progressbar
from graphbrain import *
from graphbrain.meaning.concepts import *
from graphbrain.meaning.lemmas import deep_lemma
from graphbrain.meaning.corefs import main_coref
from graphbrain.agents.agent import Agent


CLAIM_PRED_LEMMAS = {'say', 'claim'}


def _subject_preposition(claim):
    subjects = claim.edges_with_argrole('s')
    if len(subjects) == 1:
        subject = strip_concept(subjects[0])
        if subject.type() == 'ci':
            atom = subject.atom_with_type('c')
            return atom.root()
    return None


def replace_subject(edge, new_subject):
    connector = edge[0]
    new_edge = list(edge)

    for pos, role in enumerate(connector.argroles()):
        if role == 's':
            new_edge[pos + 1] = new_subject
    return hedge(new_edge)


class Claims(Agent):
    def __init__(self, hg):
        super().__init__(hg)
        self.actors = None
        self.female = None
        self.group = None
        self.male = None
        self.non_human = None
        self.female_counter = None
        self.group_counter = None
        self.male_counter = None
        self.non_human_counter = None
        self.claims = None
        self.anaphoras = 0

    def name(self):
        return 'claims'

    def languages(self):
        return {'en'}

    def _is_actor(self, edge):
        if edge in self.actors:
            return True

        if self.hg.exists(('actor/p/.', edge)):
            self.actors.add(edge)
            return True
        else:
            return False

    def _gender(self, actor):
        counts = (('female', self.female_counter[actor]),
                  ('group', self.group_counter[actor]),
                  ('male', self.male_counter[actor]),
                  ('non-human', self.non_human_counter[actor]))
        counts = sorted(counts, key=lambda x: x[1], reverse=True)
        if counts[0][1] > 0 and counts[0][1] > counts[1][1]:
            return counts[0][0]
        else:
            return None

    def start(self):
        self.actors = set()
        self.female_counter = Counter()
        self.group_counter = Counter()
        self.male_counter = Counter()
        self.non_human_counter = Counter()
        self.claims = []
        self.anaphoras = 0

    def _process_claim(self, actor, claim, edge):
        # gender detection
        prep = _subject_preposition(claim)
        if prep:
            if prep == 'she':
                self.female_counter[actor] += 1
            elif prep == 'they':
                self.group_counter[actor] += 1
            elif prep == 'he':
                self.male_counter[actor] += 1
            elif prep == 'it':
                self.non_human_counter[actor] += 1

        # record claim
        self.claims.append({'actor': actor, 'claim': claim, 'edge': edge})

    def input_edge(self, edge):
        if not edge.is_atom():
            ct = edge.connector_type()
            if ct[:2] == 'pd':
                pred = edge[0]
                if (len(edge) > 2 and
                        deep_lemma(self.hg, pred).root() in CLAIM_PRED_LEMMAS):
                    subjects = edge.edges_with_argrole('s')
                    claims = edge.edges_with_argrole('r')
                    if len(subjects) == 1 and len(claims) >= 1:
                        subject = strip_concept(subjects[0])
                        if subject and has_proper_concept(subject):
                            actor = main_coref(self.hg, subjects[0])
                            if self._is_actor(actor):
                                for claim in claims:
                                    self._process_claim(actor, claim, edge)

    def end(self):
        # assign genders
        self.female = set()
        self.group = set()
        self.male = set()
        self.non_human = set()

        print('assigning genders')
        i = 0
        with progressbar.ProgressBar(max_value=len(self.actors)) as bar:
            for actor in self.actors:
                gender = self._gender(actor)
                if gender == 'female':
                    self.female.add(actor)
                elif gender == 'group':
                    self.group.add(actor)
                elif gender == 'male':
                    self.male.add(actor)
                elif gender == 'non-human':
                    self.non_human.add(actor)

                # write gender
                if gender:
                    gender_atom = '{}/p/.'.format(gender)
                    self.add((gender_atom, actor))

                i += 1
                bar.update(i)

        # write claims
        print('writing claims')
        i = 0
        with progressbar.ProgressBar(max_value=len(self.claims)) as bar:
            for claim_data in self.claims:
                actor = claim_data['actor']
                claim = claim_data['claim']
                edge = claim_data['edge']

                # anaphora resolution
                prep = _subject_preposition(claim)
                if prep:
                    resolve = False
                    if prep == 'she':
                        resolve = actor in self.female
                    elif prep == 'they':
                        resolve = actor in self.group
                    elif prep == 'he':
                        resolve = actor in self.male
                    elif prep == 'it':
                        resolve = actor in self.non_human

                    if resolve:
                        # print('ANAPHORA')
                        # print('actor: {}'.format(actor))
                        # print('before: {}'.format(claim))
                        claim = replace_subject(claim, actor)
                        # print('after: {}'.format(claim))
                        self.anaphoras += 1

                # write claim
                self.add(('claim/p/.', actor, claim, edge))

                i += 1
                bar.update(i)

    def report(self):
        rep_claims = 'claims: {}'.format(len(self.claims))
        rep_anaph = 'anaphora resolutions: {}'.format(self.anaphoras)
        counts = (len(self.female), len(self.group), len(self.male),
                  len(self.non_human))
        cs = tuple([str(x) for x in counts])
        rep_gen = 'female: {}; group: {}; male: {}; non-human: {}'.format(*cs)
        return '\n'.join((rep_claims, rep_anaph, rep_gen))
