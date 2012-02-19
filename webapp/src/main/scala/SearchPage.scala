package com.graphbrain.webapp

case class SearchPage() extends Page {
	override def html = {

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>GraphBrain</title>
<link href="/css/main.css?311011" type="text/css" rel="Stylesheet" />  
<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
</head>
<body>
<div id="topDiv">
    <div id="topRight">
        <a href="/logout">Logout</a>&nbsp;
    </div>
</div>

<div style="text-align:center">
    <br /><br /><br /><br /><br /><br />
    <img src="/images/GB_logo_L.png" alt="graphbrain" />
    <br /><br /><br /><br /><br /><br />
    <form action="/search" method="post">
        <input type="text" name="input" id="inputField" size="50" />
        <input type="hidden" name="node_id" value="" />
        <button type="submit" id="inputFieldButton">Search</button>
    </form>
</div>
</body>
</html>

  }
}