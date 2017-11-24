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
import gb.constants as const


class TestHypergraph(unittest.TestCase):

    def setUp(self):
        params = {'backend': 'leveldb',
                  'hg': 'test.hg'}
        self.hg = hyperg.HyperGraph(params)

    def tearDown(self):
        self.hg.close()

    def test_ego(self):
        self.hg.add(('is', 'graphbrain/1', 'great/1'))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.assertEqual(self.hg.ego('graphbrain/1'), {'graphbrain/1', 'is', 'great/1', 'great/2'})
        self.hg.remove(('is', 'graphbrain/1', 'great/1'))
        self.hg.remove(('is', 'graphbrain/1', 'great/2'))
        self.hg.add_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.ego('mary/1'), {'mary/1', 'src/gb', 'graphbrain/1', 'is', 'great/1'})
        self.hg.remove_belief("mary/1", ("is", "graphbrain/1", "great/1"))

    def test_beliefs(self):
        self.hg.add_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.sources(("is", "graphbrain/1", "great/1")), {"mary/1"})
        self.assertTrue(self.hg.is_belief(("is", "graphbrain/1", "great/1")))
        self.hg.add(('is', 'graphbrain/1', 'great/2'))
        self.assertFalse(self.hg.is_belief(("is", "graphbrain/1", "great/2")))
        self.hg.add_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.sources(("is", "graphbrain/1", "great/1")), {"mary/1", "john/1"})
        self.hg.remove_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertTrue(self.hg.exists(("is", "graphbrain/1", "great/1")))
        self.hg.remove_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertFalse(self.hg.exists(("is", "graphbrain/1", "great/1")))

    def test_timestamp_beliefs(self):
        self.hg.destroy()
        self.assertEqual(self.hg.timestamp("graphbrain/1"), -1)
        self.hg.add_belief("mary/1", ("is", "graphbrain/1", "great/1"), timestamp=123456789)
        self.assertEqual(self.hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(self.hg.timestamp("great/1"), 123456789)
        self.assertEqual(self.hg.timestamp("mary/1"), 123456789)
        self.assertEqual(self.hg.timestamp(("is", "graphbrain/1", "great/1")), 123456789)
        self.assertEqual(self.hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "mary/1")), 123456789)
        self.hg.add_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(self.hg.timestamp("great/1"), 123456789)
        self.assertEqual(self.hg.timestamp("john/1"), -1)
        self.assertEqual(self.hg.timestamp(("is", "graphbrain/1", "great/1")), 123456789)
        self.assertEqual(self.hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "john/1")), -1)
        self.hg.remove_belief("mary/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(self.hg.timestamp("great/1"), 123456789)
        self.assertEqual(self.hg.timestamp("mary/1"), 123456789)
        self.assertEqual(self.hg.timestamp(("is", "graphbrain/1", "great/1")), 123456789)
        self.assertEqual(self.hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "mary/1")), -1)
        self.hg.remove_belief("john/1", ("is", "graphbrain/1", "great/1"))
        self.assertEqual(self.hg.timestamp("graphbrain/1"), 123456789)
        self.assertEqual(self.hg.timestamp("great/1"), 123456789)
        self.assertEqual(self.hg.timestamp(("is", "graphbrain/1", "great/1")), -1)
        self.assertEqual(self.hg.timestamp((const.source, ("is", "graphbrain/1", "great/1"), "john/1")), -1)


if __name__ == '__main__':
    unittest.main()
