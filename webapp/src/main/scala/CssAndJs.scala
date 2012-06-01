package com.graphbrain.webapp


import scala.util.Random
import com.graphbrain.hgdb.UserNode


case class CssAndJs() {
    
  //val version = "040312"
  val version = CssAndJs.randomVersion

  val analyticsJs = """
    <script type="text/javascript">

      var _gaq = _gaq || [];
      _gaq.push(['_setAccount', 'UA-30917836-1']);
      _gaq.push(['_trackPageview']);

      (function() {
        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
      })();

    </script>
    """

  def cssAndJs = {
    if (Server.prod) {
      """<link href="/css/main.css?01062012" type="text/css" rel="Stylesheet" />""" +
      """<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>""" +
      """<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.js" type="text/javascript"></script>""" +
      """<script src="/js/bootstrap.min.js" type="text/javascript" ></script>""" +
      """<script src="/js/gb.js?01062012" type="text/javascript" ></script>""" +
      analyticsJs
    }
    else {
      """<link href="/css/main.css?""" + version + """" type="text/css" rel="Stylesheet" />""" +
      """<script src="/js/jquery-1.7.2.min.js" type="text/javascript" ></script>""" +
      """<script src="/js/jquery-ui-1.8.18.custom.min.js" type="text/javascript" ></script>""" +
      """<script src="/js/bootstrap.min.js" type="text/javascript" ></script>""" +
      """<script src="/js/gb.js?""" + version + """" type="text/javascript" ></script>"""
    }
  } 
}

object CssAndJs {
  val rand = new Random
  def randomVersion: String = rand.nextInt(999999999).toString
}