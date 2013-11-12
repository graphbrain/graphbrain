package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import com.graphbrain.eco._
import com.graphbrain.db.ProgNode
import unfiltered.response.ResponseHeader

object EcoPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {

  private def getCode = {
    val prog = WebServer.graph.getProgNode("prog/prog")
    if (prog == null) "" else prog.prog
  }

  private def renderParser(req: HttpRequest[Any], text: String = "") = {

    var visualCtxtList = List[VisualContext]()
    if (text != "") {
      val t = new Text(text)

      val p = Prog.fromString(getCode)

      for (s <- t.sentences) {
        val ctxtsList = p.wv(s, 0)
        for (ctxts <- ctxtsList) {
          for (ctxt <- ctxts.ctxts) {
            visualCtxtList ::= new VisualContext(ctxt)
          }
        }
      }
    }

    Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
      Scalate(req, "ecoparse.ssp", ("title", "Parse"), ("text", text),
        ("ctxtList", visualCtxtList.reverse))(WebServer.engine)
  }

  private def renderCode(req: HttpRequest[Any]) = {
    val code = getCode

    Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
      Scalate(req, "ecocode.ssp", ("title", "Code"), ("code", code))(WebServer.engine)
  }

  private def renderTests(req: HttpRequest[Any]) = {
    Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
      Scalate(req, "ecotests.ssp", ("title", "Tests"))(WebServer.engine)
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
    case req@GET(Path(Seg2("eco" :: "tests" :: Nil)) & Cookies(cookies)) =>
      renderTests(req)
  }
}