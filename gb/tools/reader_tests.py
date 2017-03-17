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


class ReaderTests(object):
    def __init__(self, hg):
        self.hg = hg
        self.extractor = Extractor(hg, stages=('alpha', 'beta-simple', 'gamma', 'delta'))
        self.extractor.debug = True

    def generate_parsed_sentences_file(self, infile, outfile):
        with open(infile, 'r') as f:
            text = f.read()
        sents_parses = self.extractor.read_text(text)
        with open(outfile, 'w') as f:
            for sent_parse in sents_parses:
                f.write('%s\n%s\n' % (sent_parse[0], sent_parse[1].tree))
