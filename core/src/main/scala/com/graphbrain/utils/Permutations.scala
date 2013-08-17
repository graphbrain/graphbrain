package com.graphbrain.utils


object Permutations {
	
	def permutations(n: Int) = {
		var perms = 1
		for (i <- 2 to n) {
			perms *= i
		}
	  perms
	}
	
	def assignToPermPos(value: String, strArray: Array[String], pos: Int): Unit = {
		var j = 0
		for (i <- 0 until strArray.length) {
			if (strArray(i) == null) {
				if (j == pos) {
					strArray(i) = value
					return
				}
				j += 1
			}
		}
	}
	
	def getFromPermPos(strArray: Array[String], pos: Int): String = {
		var j = 0
    for (i <- 0 until strArray.length) {
			if (strArray(i) != null) {
				if (j == pos) {
					val res = strArray(i)
					strArray(i) = null
					return res
				}
				j += 1
			}
		}
		// this should not happen
		null
	}
	
	// http://stackoverflow.com/questions/1506078/fast-permutation-number-permutation-mapping-algorithms
	def permutationPositions(n: Int, per: Int): Array[Int] = {
	  val res = new Array[Int](n)
		var number = per

		// the remaining element always goes to the first free slot
		res(0) = 0
		
		var base = 2
		for (i <- 1 until n) {
			res(i) = number % base
		  number = number / base
		  base += 1
		}
		
		res
	}
	
	def strArrayPermutation(in: Array[String], per: Int) = {
		val n = in.length
		val out = new Array[String](n)
		val config = permutationPositions(n, per)

    for (i <- n - 1 to 0 by -1) {
			assignToPermPos(in(n - i - 1), out, config(i))
		}
		
		out
	}
	
	def strArrayUnpermutate(in: Array[String], per: Int) = {
		val n = in.length
		val out = new Array[String](n)
		val config = permutationPositions(n, per)

    for (i <- n - 1 to 0 by -1) {
			out(n - i - 1) = getFromPermPos(in, config(i))
		}
		
		out
	}
}