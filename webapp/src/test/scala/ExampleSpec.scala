package com.example

import org.specs._

import dispatch._

object ExampleSpec extends Specification with unfiltered.spec.netty.Served {
  
  import dispatch._
  
  def setup = { _.handler(Palindrome) }
  
  "The example app" should {
    "serve unfiltered text" in {
      Http(host as_str) must beMatching("What say you")
    }
  }
}
