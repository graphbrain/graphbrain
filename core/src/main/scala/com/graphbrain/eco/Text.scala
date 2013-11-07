package com.graphbrain.eco

class Text(val text: String) {
  val sentences = text.split("\\.").map(_.trim).filter(_ != "")

  def parse(prog: Prog) = {
    for (s <- sentences) {
      val vertex = prog.parse(s)
      println(s)
      println(vertex)
    }
  }
}

object Text {
  def main(args: Array[String]) = {
    val text =
      """
        |The Obama administration is appealing to its allies in Congress to stick with health care law.
        |
        |Egypt's ousted leader Mohammed Morsi has gone on trial in Cairo, telling the judge the case is illegitimate as he remains president.
        |
        |He and 14 other Muslim Brotherhood figures face charges of inciting the killing of protesters outside the presidential palace in 2012.
        |
        |After Mr Morsi's remarks and his refusal to wear a uniform, the judge adjourned the trial until 8 January.
        |
        |Protests took place outside the court and elsewhere in Cairo.
      """.stripMargin

    new Text(text).parse(Prog.load("eco/progs/test.eco"))
  }
}