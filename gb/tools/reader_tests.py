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


from termcolor import colored
from gb.reader.extractor import Extractor


FIND_CASE = 0
READ_SENTENCE = 1
READ_PARSES = 2


class ReaderTests(object):
    def __init__(self, hg, disamb):
        self.hg = hg
        if disamb:
            beta = 'beta'
        else:
            beta = 'beta-naive'
        self.extractor = Extractor(hg, stages=('alpha-forest', beta, 'gamma', 'delta', 'epsilon'), show_namespaces=True)
        # self.extractor.debug = True
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
        line_number = 0
        for line in content:
            if len(line) > 0:
                if line[0] == ':':
                    key = line[1:]
                    if key == 'sentence':
                        state = READ_SENTENCE
                    elif key == 'parses':
                        state = READ_PARSES
                    else:
                        raise RuntimeError('Unkown keyword in reader test file: %s (line %s)' % (key, str(line_number)))
                else:
                    if state == READ_SENTENCE:
                        cur_sentence = line
                        self.cases[cur_sentence] = []
                    elif state == READ_PARSES:
                        self.cases[cur_sentence].append(line)
                    else:
                        raise RuntimeError('Malformed reader test file (line %s)' % str(line_number))
            line_number += 1

    def run_tests(self, infile):
        self.read_dataset(infile)
        correct = 0
        for sentence in self.cases:
            sent_parses = self.extractor.read_text(sentence)
            result = sent_parses[0][1].tree.to_hyperedge_str(with_namespaces=False)
            if result in self.cases[sentence]:
                correct += 1
            else:
                print()
                print('failed test for sentence:')
                print(colored(sentence, 'cyan'))
                print('expected:')
                for parse in self.cases[sentence]:
                    print(colored(parse, 'green'))
                    print('result:')
                    print(print(result, 'red'))
        total = len(self.cases)
        percentage = (float(correct) / float(total)) * 100.
        print('%s out of %s correct parses (%.2f%%)' % (correct, total, percentage))

    def reader_debug(self, infile):
        with open(infile, 'r') as f:
            text = f.read()
        self.extractor.debug = True
        self.extractor.read_text(text)
