package com.graphbrain.webapp


import com.graphbrain.hgdb.UserNode


case class NavBar(user: UserNode, page: String) {
    
  val userId = if (user == null) "" else user.id

  def pages = {
    if (page == "home") {
      """
      <div class="nav-collapse">
        <ul class="nav">
          <li class="active"><a href="#">home</a></li>
          <li><a href="/about">learn more</a></li>
          <li><a href="/contact">contact</a></li>
        </ul>
      </div>
      """
    }
    else if (page == "about") {
      """
      <div class="nav-collapse">
        <ul class="nav">
          <li><a href="/">home</a></li>
          <li class="active"><a href="#">learn more</a></li>
          <li><a href="/contact">contact</a></li>
        </ul>
      </div>
      """
    }
    else if (page == "contact") {
      """
      <div class="nav-collapse">
        <ul class="nav">
          <li><a href="/">home</a></li>
          <li><a href="/about">learn more</a></li>
          <li class="active"><a href="#">contact</a></li>
        </ul>
      </div>
      """
    }
    else {
      ""
    }
  }

  def tools = {
    if (page == "node") {
      """
      <li>
        <div class="btn-group" data-toggle="buttons-radio">
          <button class="btn">Public</button>
          <button class="btn">Personal</button>
          <button class="btn">Private</button>
        </div>
      </li>
      <li>&nbsp;</li>
      <li><button class="btn" id="ai-chat-button" data-toggle="button"><i class="icon-asterisk icon-black"></i> Talk to AI</button></li>
      <li><button class="btn" id="removeButton" data-toggle="button"><i class="icon-remove icon-black"></i> Remove</button></li>
      """
    }
    else {
      ""
    }
  }

  def userStuff = {
    if (user == null) {
      """
      <div class="pull-right">
        <ul class="nav">
          <li><a class="signupLink" href="#">Sign Up</a></li>
          <li><a id="loginLink" href="#">Login</a></li>
        </ul>
      </div>
      """
    }
    else {
      """
      <div class="pull-right">
      <ul class="nav">
        """ + tools + """
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-black"></i> """ + user.name + """ <b class="caret"></b></a>
          <ul class="dropdown-menu">
            <li><a href="/node/user/""" + user.username +  """">Home</a></li>
            <li><a href="#" id="logoutLink">Logout</a></li>
          </ul>
        </li>
      </ul>
      </div>
      """
    }
  }

  // <div class="navbar navbar-fixed-top">
  def html = {
    if (page == "comingsoon") {
      ""
    }
    else {
      """
      <div class="navbar navbar-fixed-top">
        <div class="navbar-inner">
          <div class="container-fluid">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
            </a>
            <a href="/secret"><img src="/images/GB_logo_XS.png" class="brand" alt="graphbrain" /></a>
            <div class="nav-collapse">
              <ul class="nav">
                <li><form class="navbar-search" id="search-field">
                  <input type="text" id="search-input-field" placeholder="Search" />
                </form></li>
              </ul>
            </div>
            """ + pages + """
            """ + userStuff + """
          </div>
        </div>
        <div id="alert" class="alert" style="visibility:hidden; margin:0px">
          <div id="alertMsg"></div>
        </div>
      </div>
      """
    }
  } 
}

object NavBar {
}
