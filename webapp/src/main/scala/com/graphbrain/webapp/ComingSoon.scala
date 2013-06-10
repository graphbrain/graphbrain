package com.graphbrain.webapp

case class ComingSoon(prod: Boolean) extends Page {
	def cssAndJs = {
      if (prod) {
        """<link href="/css/soon.css" type="text/css" rel="Stylesheet" />""" +
        analyticsJs
      }
      else {
        """<link href="/css/soon.css" type="text/css" rel="Stylesheet" />"""
      }
    }

	override def html = {

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>GraphBrain</title>  
<link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
{scala.xml.Unparsed(cssAndJs)}
</head>
<body>

<div id="logo">
	<img src="/images/GB_logo_L.png" alt="graphbrain"/>
</div>

<div id="slogan">
A beautiful new way to organize and explore information.
</div>

<div id="slogan2">
Coming soon.
</div>

<div id="slogan2">
You can contact us at <a href="mailto:contact@graphbrain.com">contact@graphbrain.com</a>.
</div>

</body>
</html>

  }
}