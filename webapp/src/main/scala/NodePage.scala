package com.graphbrain.webapp

import scala.util.Random

import unfiltered.scalate._
import unfiltered.request._
import unfiltered.response._

import com.graphbrain.hgdb.VertexStore
import com.graphbrain.hgdb.Vertex
import com.graphbrain.hgdb.UserNode


case class NodePage(store: VertexStore, node: Vertex, user: UserNode, prod: Boolean, req: HttpRequest[Any]) {
    
    //val version = "040312"
    val version = NodePage.randomVersion

    val gi = new GraphInterface(node.id, store, user)
    
    val userId = if (user == null) "" else user.id

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

    val js = "var nodes = " + gi.nodesJSON + ";\n" +
        "var snodes = " + gi.snodesJSON + ";\n" +
        "var links = " + gi.linksJSON + ";\n" +
        "var rootNodeId = '" + node.id + "';\n" +
        (if (user == null)
          "var brains = [];"
        else
          "var brains = " + gi.brainsJSON + ";\n") +
        "var curBrainId = '" + Server.store.brainId(node) + "';\n" +
        "var userId = '" + userId + "';\n"

    def cssAndJs = {
      if (prod) {
        """<link href="/css/main.css?27052012" type="text/css" rel="Stylesheet" />""" +
        """<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>""" +
        """<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.js" type="text/javascript"></script>""" +
        """<script src="/js/bootstrap.min.js" type="text/javascript" ></script>""" +
        """<script src="/js/gb.js?27052012" type="text/javascript" ></script>""" +
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

    def userStuff = {
      if (user == null) {
        """
        <div class="pull-right">
          <ul class="nav">
            <li><a id="signupLink" href="#">Sign Up</a></li>
            <li><a id="loginLink" href="#">Login</a></li>
          </ul>
        </div>
        """
      }
      else {
        """
        <ul class="nav pull-right">
            <li class="divider-vertical"></li>
            <li><a href="#" id="addLink"><i class="icon-plus icon-white"></i> Add</a></li>
            <li><a href="#" id="addBrainLink"><i class="icon-plus icon-white"></i> Brain</a></li>
            <li><a href="#" id="addFriendLink"><i class="icon-plus icon-white"></i> Friend</a></li>
            <li class="divider-vertical"></li>
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown" id="curBrain"></b></a>
              <ul class="dropdown-menu" id="brainDropdown"></ul>
            </li>
            <li class="divider-vertical"></li>
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-white"></i> """ + user.name + """ <b class="caret"></b></a>
              <ul class="dropdown-menu">
                <li><a href="/node/user/""" + user.username +  """">Home</a></li>
                <li><a href="#" id="logoutLink">Logout</a></li>
              </ul>
            </li>
          </ul>
        """
      }
    }

	def response = Ok ~> Scalate(req, "node.ssp", ("cssAndJs", cssAndJs), ("navBar", NavBar(user, "node").html), ("js", js))
}

object NodePage {
    val rand = new Random

    def randomVersion: String = rand.nextInt(999999999).toString
}