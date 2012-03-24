package com.graphbrain.webapp

import scala.util.Random
import com.graphbrain.hgdb.VertexStore


case class NodePage(store: VertexStore, nodeId: String) extends Page {
    
    //val version = "040312"
    val version = NodePage.randomVersion

    val gi = new GraphInterface(nodeId, store)
    val js = "var nodes = " + gi.nodesJSON + ";\n" +
        "var snodes = " + gi.snodesJSON + ";\n" +
        "var links = " + gi.linksJSON + ";\n" +
        "var error = '';\n"

    def cssAndJs = {
        """<link href="/css/main.css?""" + version + """" type="text/css" rel="Stylesheet" />""" +
        '\n' +
        """<script src="/js/gb.js?""" + version + """" type="text/javascript" ></script>"""
    }

	override def html = {

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Graphbrain</title>
<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
<script src="/js/jquery-1.6.4.min.js" type="text/javascript" ></script>
{scala.xml.Unparsed(cssAndJs)}
</head>

<body>
<div id="topDiv">
    <div id="logo">
        <a href="/"><img src="/images/GB_logo_M.png" alt="graphbrain"/></a>
    </div>
    <div id="inputArea">
        <form action="/search" method="post">
            <input type="text" name="input" id="inputField" size="50" />
            <button type="submit" id="inputFieldButton">Search</button>
        </form>
    </div>
</div>

<div id="nodesView">
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