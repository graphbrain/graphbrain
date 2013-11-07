package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import com.graphbrain.eco.{Context, Word, Words, Prog}

object EcoPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {

  private def renderContext(ctxt: Context, indent: Int = 0): String = {
    var r = ""

    (0 until indent).foreach(x => r += "&nbsp;&nbsp;&nbsp;&nbsp;")
    r += "<span class=\"text-success\">" + ctxt.parent.rule.name + "</span>"
    r += " (" + renderWords(ctxt.parent.sentence) + ") <br />"

    for (sctxt <- ctxt.subContexts)
      r += renderContext(sctxt, indent + 1)

    r
  }

  private def renderWord(word: Word) =
    word.word + " <span class=\"text-primary\">[" + word.pos + "]</span>"

  private def renderWords(words: Words) =
    words.words.map(w => renderWord(w))
      .reduceLeft(_ + " " + _)

  private def render(req: HttpRequest[Any], text: String = "") = {

    var parse = ""

    if (text != "") {
      val p = Prog.load("eco/progs/test.eco")
      val ctxtList = p.wv(text, 0)

      for (ctxts <- ctxtList) {
        parse +=
          """<div class="panel-group" id="accordion">"""

        for (c <- ctxts.ctxts) {
          parse +=
            """
              |  <div class="panel panel-default">
              |    <div class="panel-heading">
              |      <h4 class="panel-title">
              |        <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">""".stripMargin

          parse += c.getTopRetVertex

          parse +=
            """
              |        </a>
              |      </h4>
              |    </div>
              |    <div id="collapseOne" class="panel-collapse collapse">
              |      <div class="panel-body">""".stripMargin

          parse += renderWords(ctxts.sentence)

          parse += "<br /><br />"

          parse += renderContext(c)

          parse +=
            """
              |      </div>
              |    </div>
              |  </div>
            """.stripMargin
        }

        parse += "</div>"
      }
    }

    Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
      Scalate(req, "eco.ssp", ("text", text), ("parse", parse))(WebServer.engine)
  }

  def intent = {
    case req@GET(Path("/eco") & Cookies(cookies)) =>
      render(req)
    case req@POST(Path("/eco") & Params(params) & Cookies(cookies)) =>
      render(req, params("text")(0))
  }
}
