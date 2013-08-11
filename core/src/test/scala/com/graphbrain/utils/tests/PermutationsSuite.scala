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
}
