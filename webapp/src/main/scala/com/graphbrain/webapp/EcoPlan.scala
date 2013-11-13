package com.graphbrain.webapp

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import com.graphbrain.eco._
import com.graphbrain.db.{TextNode, ProgNode}
import unfiltered.response.ResponseHeader
import unfiltered.Cookie

object EcoPlan extends cycle.Plan with cycle.SynchronousExecution with ServerErrorResponse {

  private def getCode = {
    val prog = WebServer.graph.getProgNode("prog/prog")
    if (prog == null) "" else prog.prog
  }

  private def getTests = {
    val tests = WebServer.graph.getTextNode("text/tests")
    if (tests == null) "" else tests.text
  }

  private def renderParser(req: HttpRequest[Any], cookies: Map[String, Any], parseText: String = "") = {

    val text = if (parseText == "")
      cookies("parse_test") match {
        case Some(Cookie(_, value, _, _, _, _, _, _)) => value
        case _ => ""
      }
    else
      parseText

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

    Ok ~>
      SetCookies(Cookie("parse_test", parseText)) ~>
      ResponseHeader("Content-Type", Set("text/html")) ~>
      Scalate(req, "ecoparse.ssp", ("title", "Parse"), ("text", text),
        ("ctxtList", visualCtxtList.reverse))(WebServer.engine)
  }

  private def renderCode(req: HttpRequest[Any]) = {
    val code = getCode

    Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
      Scalate(req, "ecocode.ssp", ("title", "Code"), ("code", code))(WebServer.engine)
  }

  private def renderRunTests(req: HttpRequest[Any], run: Boolean) = {
    var visualCtxtList = List[VisualContext]()

    if (run) {
      val testData = getTests
      val tests = new Tests(testData)

      val p = Prog.fromString(getCode)

      for (t <- tests.tests) {
        val ctxtsList = p.wv(t(0), 0)
        for (ctxts <- ctxtsList) {
          for (ctxt <- ctxts.ctxts) {
            visualCtxtList ::= new VisualContext(ctxt, t(1))
          }
        }
      }
    }

    Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
      Scalate(req, "ecoruntests.ssp", ("title", "Run Tests"), ("ctxtList", visualCtxtList.reverse))(WebServer.engine)
  }

  private def renderEditTests(req: HttpRequest[Any]) = {
    val tests = getTests

    Ok ~> ResponseHeader("Content-Type", Set("text/html")) ~>
      Scalate(req, "ecoedittests.ssp", ("title", "Edit Tests"), ("tests", tests))(WebServer.engine)
  }

  def intent = {
    case req@GET(Path("/eco") & Cookies(cookies)) =>
      renderParser(req, cookies)
    case req@POST(Path("/eco") & Params(params) & Cookies(cookies)) =>
      renderParser(req, cookies, params("text")(0))
    case req@GET(Path(Seg2("eco" :: "code" :: Nil)) & Cookies(cookies)) =>
      renderCode(req)
    case req@POST(Path(Seg2("eco" :: "code" :: Nil)) & Params(params) & Cookies(cookies)) => {
      WebServer.graph.put(ProgNode("prog/prog", params("code")(0)))
      renderCode(req)
    }
    case req@GET(Path(Seg2("eco" :: "runtests" :: Nil)) & Cookies(cookies)) =>
      renderRunTests(req, run = false)
    case req@POST(Path(Seg2("eco" :: "runtests" :: Nil)) & Cookies(cookies)) =>
      renderRunTests(req, run = true)
    case req@GET(Path(Seg2("eco" :: "edittests" :: Nil)) & Cookies(cookies)) =>
      renderEditTests(req)
    case req@POST(Path(Seg2("eco" :: "edittests" :: Nil)) & Params(params) & Cookies(cookies)) => {
      WebServer.graph.put(TextNode("text/tests", params("tests")(0)))
      renderEditTests(req)
    }
  }
}