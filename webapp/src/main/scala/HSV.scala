package com.graphbrain.webapp

import scala.math


class PoorLong (val l:Long) {
    /** a "mathematical" modulo that always maps to 0...n-1 (for positive n) */
    def mod(n:Long) = {
        val m = l % n
        if (m<0) m+n else m
    }
}

class PoorDouble (val d:Double) {
    /**the "decimals" of this double */
    def fraction:Double = d - d.floor

    /** a "pseudo-mod", 7.5 mod 5 would map to 2.5, -0.5 mod 5 would map to 4.5 */
    def mod(n:Long) = new PoorLong(d.floor.longValue).mod(n).toDouble + fraction
}

object Conversions {
    implicit def longToPoorLong(l:Long) = new PoorLong(l)
    implicit def doubleToPoorDouble(d:Double) = new PoorDouble(d)
} 

import Conversions._ //put this import before your hsv converter code


object HSV {
  /** Converts an hsv triplet in the range ([0,360],[0,1],[0,1])
   *  to an rgb triplet in the range ([0,1],[0,1],[0,1])
   */
  def hsvToRgb(h:Double, s:Double, v:Double): (Double, Double, Double) = {
    val c = s * v
    val h1 = h / 60.0
    val x  = c * (1.0 - ((h1 mod 2) - 1.0).abs) 
    val (r,g,b) = if (h1 < 1.0) (c, x, 0.0)
                else if (h1 < 2.0) (x, c, 0.0)
                else if (h1 < 3.0) (0.0, c, x)
                else if (h1 < 4.0) (0.0, x, c)
                else if (h1 < 5.0) (x, 0.0, c)
                else (c, 0.0, x) 
    val m = v - c
    (r + m, g + m, b + m)
  }
}