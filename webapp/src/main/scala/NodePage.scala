package com.graphbrain.webapp

import scala.util.Random
import com.graphbrain.hgdb.VertexStore


case class NodePage(store: VertexStore, nodeId: String, prod: Boolean) extends Page {
    
    //val version = "040312"
    val version = NodePage.randomVersion

    val gi = new GraphInterface(nodeId, store)
    val js = "var nodes = " + gi.nodesJSON + ";\n" +
        "var snodes = " + gi.snodesJSON + ";\n" +
        "var links = " + gi.linksJSON + ";\n" +
        "var error = '';\n"

    def cssAndJs = {
      if (prod) {
        """<link href="/css/bootstrap.min.css" type="text/css" rel="Stylesheet" />""" +
        """<link href="/css/main.css?01052012" type="text/css" rel="Stylesheet" />""" +
        """<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>""" +
        """<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.js" type="text/javascript"></script>""" +
        """<script src="/js/bootstrap.min.js" type="text/javascript" ></script>""" +
        """<script src="/js/gb.js?01052012" type="text/javascript" ></script>""" +
        analyticsJs
      }
      else {
        """<link href="/css/bootstrap.min.css?13042012" type="text/css" rel="Stylesheet" />""" +
        """<link href="/css/main.css?""" + version + """" type="text/css" rel="Stylesheet" />""" +
        """<script src="/js/jquery-1.7.2.min.js" type="text/javascript" ></script>""" +
        """<script src="/js/jquery-ui-1.8.18.custom.min.js" type="text/javascript" ></script>""" +
        """<script src="/js/bootstrap.min.js" type="text/javascript" ></script>""" +
        """<script src="/js/gb.js?""" + version + """" type="text/javascript" ></script>"""
      }
    }

	override def html = {

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Graphbrain</title>
<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
{scala.xml.Unparsed(cssAndJs)}
</head>

<body>

<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container-fluid">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
          <a class="brand" href="/"><img src="/images/GB_logo_S.png" alt="graphbrain"/></a>
          <div class="nav-collapse">
            <ul class="nav">
              <li><a href="/node/welcome/graphbrain">About</a></li>
              <li><form class="navbar-search" id="search-field">
                <input type="text" id="search-input-field" class="search-query" placeholder="Search" />
              </form></li>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
</div>

<div id="graphDiv">
  <div id="nodesDiv"></div>
</div>

<script language="javascript">

{scala.xml.Unparsed(js)}

</script>
</body>
</html>

  }
}

object NodePage {
    val rand = new Random

    def randomVersion: String = rand.nextInt(999999999).toString
}