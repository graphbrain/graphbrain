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


import unittest
import gb.hypergraph.hypergraph as hyperg
import gb.hypergraph.constants as const


class TestHypergraph(unittest.TestCase):

    def test_beliefs(self):
        hg = hyperg.HyperGraph({'backend': 'sqlite',
                                'file_path': 'test.db'})
        hg.add_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(hg.sources(("is", "graphbrain/1", "great/1")), {"mary/1"})
        hg.add_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(hg.sources(("is", "graphbrain/1", "great/1")), {"mary/1", "john/1"})
        hg.remove_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertTrue(hg.exists(("is", "graphbrain/1", "great/1")))
        hg.remove_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertFalse(hg.exists(("is", "graphbrain/1", "great/1")))

    def test_timestamp_beliefs(self):
        hg = hyperg.HyperGraph({'backend': 'sqlite',
                                'file_path': 'test.db'})
        hg.destroy()
        self.assertEqual(hg.timestamp("graphbrain/1"), -1)
        hg.add_belief("mary/1", ("is", "graphbrain/1", "great/1"), timestamp=123456789)
        self.assertEqual(hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(hg.timestamp("great/1"), 123456789)
        self.assertEqual(hg.timestamp("mary/1"), 123456789)
        self.assertEqual(hg.timestamp(("is", "graphbrain/1", "great/1")), 123456789)
        self.assertEqual(hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "mary/1")), 123456789)
        hg.add_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(hg.timestamp("great/1"), 123456789)
        self.assertEqual(hg.timestamp("john/1"), -1)
        self.assertEqual(hg.timestamp(("is", "graphbrain/1", "great/1")), 123456789)
        self.assertEqual(hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "john/1")), -1)
        hg.remove_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(hg.timestamp("great/1"), 123456789)
        self.assertEqual(hg.timestamp("mary/1"), 123456789)
        self.assertEqual(hg.timestamp(("is", "graphbrain/1", "great/1")), 123456789)
        self.assertEqual(hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "mary/1")), -1)
        hg.remove_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(hg.timestamp("great/1"), 123456789)
        self.assertEqual(hg.timestamp(("is", "graphbrain/1", "great/1")), -1)
        self.assertEqual(hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "john/1")), -1)


if __name__ == '__main__':
    unittest.main()
