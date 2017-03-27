#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


from gb.reader.extractor import Extractor


FIND_CASE = 0
READ_SENTENCE = 1
READ_PARSES = 2


class ReaderTests(object):
    def __init__(self, hg):
        self.hg = hg
        self.extractor = Extractor(hg, stages=('alpha', 'beta-simple', 'gamma', 'delta'))
        self.extractor.debug = True
        self.cases = None

    def generate_parsed_sentences_file(self, infile, outfile):
        with open(infile, 'r') as f:
            text = f.read()
        sents_parses = self.extractor.read_text(text)
        with open(outfile, 'w') as f:
            for sent_parse in sents_parses:
                f.write(':sentence\n')
                f.write('%s\n' % sent_parse[0])
                f.write(':parses\n')
                f.write('%s\n' % sent_parse[1].tree)

    def read_dataset(self, infile):
        with open(infile) as f:
            content = f.readlines()
        content = [x.strip() for x in content]

        self.cases = {}
        state = FIND_CASE
        cur_sentence = None
        for line in content:
            if len(line) > 0:
                if line[0] == ':':
                    key = line[1:]
                    if key == 'sentence':
                        state = READ_SENTENCE
                    elif key == 'parses':
                        state = READ_PARSES
                    else:
                        # error!
                        pass
                else:
                    if state == READ_SENTENCE:
                        cur_sentence = line
                        self.cases[cur_sentence] = []
                    elif state == READ_PARSES:
                        self.cases[cur_sentence].append(line)
                    else:
                        # error!
                        pass
