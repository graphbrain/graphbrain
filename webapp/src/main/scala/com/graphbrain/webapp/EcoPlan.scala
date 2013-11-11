package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import com.graphbrain.eco.{Context, Word, Words, Prog, Text}
import com.graphbrain.db.ProgNode

object EcoPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {

  private def getCode = {
    val prog = WebServer.graph.getProgNode("prog/prog")
    if (prog == null) "" else prog.prog
  }

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

  private def renderParser(req: HttpRequest[Any], text: String = "") = {

    var parse = ""

    if (text != "") {
      val t = new Text(text)

      val p = Prog.fromString(getCode)

      parse +=
        """<div class="panel-group" id="accordion">"""

      var coll = 0

      for (s <- t.sentences) {
        val ctxtList = p.wv(s, 0)

        for (ctxts <- ctxtList) {
          for (c <- ctxts.ctxts) {
            parse +=
              ("""
                |  <div class="panel panel-default">
                |    <div class="panel-heading">
                |      <h4 class="panel-title">
                |        <a data-toggle="collapse" data-parent="#accordion" href="#collapse""" + coll + """">""").stripMargin

            parse += c.getTopRetVertex

            parse +=
              ("""
                |        </a>
                |      </h4>
                |    </div>
                |    <div id="collapse""" + coll + """" class="panel-collapse collapse">
                |      <div class="panel-body">""").stripMargin

            parse += renderWords(ctxts.sentence)

            parse += "<br /><br />"

            parse += renderContext(c)

            parse +=
              """
                |      </div>
                |    </div>
                |  </div>
              """.stripMargin

            coll += 1
          }
        }
      }

      parse += "</div>"
    }

    Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
      Scalate(req, "ecoparse.ssp", ("title", "Parse"), ("text", text), ("parse", parse))(WebServer.engine)
  }

  private def renderCode(req: HttpRequest[Any]) = {
    val code = getCode

    Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
      Scalate(req, "ecocode.ssp", ("title", "Code"), ("code", code))(WebServer.engine)
  }

  def intent = {
    case req@GET(Path("/eco") & Cookies(cookies)) =>
      renderParser(req)
    case req@POST(Path("/eco") & Params(params) & Cookies(cookies)) =>
      renderParser(req, params("text")(0))
    case req@GET(Path(Seg2("eco" :: "code" :: Nil)) & Cookies(cookies)) =>
      renderCode(req)
    case req@POST(Path(Seg2("eco" :: "code" :: Nil)) & Params(params) & Cookies(cookies)) => {
      WebServer.graph.put(ProgNode("prog/prog", params("code")(0)))
      renderCode(req)
    }
  }
}