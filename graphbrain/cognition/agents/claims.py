import logging
from collections import Counter

import progressbar

from graphbrain import hedge
from graphbrain.cognition.agent import Agent
from graphbrain.meaning.concepts import has_proper_concept
from graphbrain.meaning.concepts import strip_concept
from graphbrain.meaning.corefs import main_coref
from graphbrain.meaning.lemmas import deep_lemma
from graphbrain.op import create_op


CLAIM_PRED_LEMMAS = {'say', 'claim'}


def _subject_preposition(claim):
    subjects = claim.edges_with_argrole('s')
    if len(subjects) == 1:
        subject = strip_concept(subjects[0])
        if subject:
            atom = subject.atom_with_type('C')
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
    def __init__(self, name, progress_bar=True, logging_level=logging.INFO):
        super().__init__(
            name, progress_bar=progress_bar, logging_level=logging_level)
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

    def languages(self):
        return {'en'}

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

    def on_start(self):
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
        if prep and set(edge[0].argroles()) == {'s', 'r'}:
            if prep == 'she':
                # print('she {}'.format(actor))
                self.female_counter[actor] += 1
            elif prep == 'they':
                # print('they {}'.format(actor))
                self.group_counter[actor] += 1
            elif prep == 'he':
                # print('he {}'.format(actor))
                self.male_counter[actor] += 1
            elif prep == 'it':
                # print('it {}'.format(actor))
                self.non_human_counter[actor] += 1

        # record claim
        self.claims.append({'actor': actor, 'claim': claim, 'edge': edge})

    def process_edge(self, edge, depth):
        hg = self.system.get_hg(self)

        if not edge.is_atom():
            ct = edge.connector_type()
            if ct[0] == 'P':
                pred = edge[0]
                if (len(edge) > 2 and
                        deep_lemma(
                            hg,
                            pred,
                            same_if_none=True).root() in CLAIM_PRED_LEMMAS):
                    subjects = edge.edges_with_argrole('s')
                    claims = edge.edges_with_argrole('r')
                    if len(subjects) == 1 and len(claims) >= 1:
                        subject = strip_concept(subjects[0])
                        if subject and has_proper_concept(subject):
                            actor = main_coref(hg, subject)
                            self.actors.add(actor)
                            for claim in claims:
                                # if specificatin, claim is inside
                                if claim.type()[0] == 'S':
                                    self._process_claim(actor, claim[1], edge)
                                else:
                                    self._process_claim(actor, claim, edge)

    def on_end(self):
        # assign genders
        self.female = set()
        self.group = set()
        self.male = set()
        self.non_human = set()

        self.logger.debug('assigning genders')
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
                    gender_atom = '{}/P/.'.format(gender)
                    yield create_op((gender_atom, actor))

                i += 1
                bar.update(i)

        # write claims
        self.logger.debug('writing claims')
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
                        self.logger.debug('ANAPHORA')
                        self.logger.debug('actor: {}'.format(actor))
                        self.logger.debug('before: {}'.format(claim))
                        claim = replace_subject(claim, actor)
                        self.logger.debug('after: {}'.format(claim))
                        self.anaphoras += 1

                # write claim
                yield create_op(('claim/P/.', actor, claim, edge))

                i += 1
                bar.update(i)

    def report(self):
        rep_claims = 'claims: {}'.format(len(self.claims))
        rep_anaph = 'anaphora resolutions: {}'.format(self.anaphoras)
        counts = (len(self.female), len(self.group), len(self.male),
                  len(self.non_human))
        cs = tuple([str(x) for x in counts])
        rep_gen = 'female: {}; group: {}; male: {}; non-human: {}'.format(*cs)
        return '; '.join((rep_claims, rep_anaph, rep_gen))
