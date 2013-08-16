package com.graphbrain.utils.tests

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import com.graphbrain.utils.Permutations

@RunWith(classOf[JUnitRunner])
class PermutationsSuite extends FunSuite {

  test("permutations") {
    assert(Permutations.permutations(0) === 1)
    assert(Permutations.permutations(1) === 1)
    assert(Permutations.permutations(2) === 2)
    assert(Permutations.permutations(3) === 6)
    assert(Permutations.permutations(4) === 24)
  }

  test("permutation postitions") {
    var pos = Permutations.permutationPositions(3, 0)
    assert(pos === Array(0, 0, 0))
    pos = Permutations.permutationPositions(3, 1)
    assert(pos === Array(0, 1, 0))
    pos = Permutations.permutationPositions(3, 2)
    assert(pos === Array(0, 0, 1))
    pos = Permutations.permutationPositions(3, 3)
    assert(pos === Array(0, 1, 1))
    pos = Permutations.permutationPositions(3, 4)
    assert(pos === Array(0, 0, 2))
    pos = Permutations.permutationPositions(3, 5)
    assert(pos === Array(0, 1, 2))
  }
}