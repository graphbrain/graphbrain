`String.prototype.replaceAll = function(str1, str2, ignore) {
  return this.replace(new RegExp(str1.replace(/([\/\,\!\\\^\$\{\}\[\]\(\)\.\*\+\?\|\<\>\-\&])/g,"\\$&"),(ignore?"gi":"g")),(typeof(str2)=="string")?str2.replace(/\$/g,"$$$$"):str2);
}`


autoUpdateUsername = true


initSignUpDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="signUpModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Sign Up</h3>
  </div>
  <form class="signupForm">
    <div class="modal-body" id="registerLoginBody">
      <fieldset id="nameFieldSet">
        <label>Name</label>
        <input id="suName" type="text" class="span3" placeholder="Or an alias if you prefer">
        <span id="nameErrMsg" class="help-inline" />
      </fieldset>
      <fieldset id="usernameFieldSet">
        <label>Username</label>
        <input id="suUsername" type="text" class="span3" placeholder="Unique identifier">
        <span id="usernameErrMsg" class="help-inline" />
      </fieldset>
      <fieldset id="emailFieldSet">
        <label>Email</label>
        <input id="suEmail" type="text" class="span3" placeholder="Will not be seen by other members">
        <span id="emailErrMsg" class="help-inline" />
        <span class="help-block">We will never give or sell it to third-parties.</span>
      </fieldset>
      <fieldset id="passFieldSet">
        <label>Password</label>
        <input id="suPassword" type="password" class="span3" placeholder="A good password">
        <input id="suPassword2" type="password" class="span3" placeholder="Confirm password">
        <span id="passErrMsg" class="help-inline" />
      </fieldset>
    </div>
    <div class="modal-footer">
      </form>
      <a class="btn" data-dismiss="modal">Close</a>
      <a id="signupButton" class="btn btn-primary">Sign Up</a>
    </div>
  </form>
</div>
    """)
    dialogHtml.appendTo('body')
    $('#signupButton').click(signup)

initLoginDialog = () ->
    dialogHtml = $("""
<div class="modal hide" id="loginModal">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Login</h3>
  </div>
  <form class="loginForm">
    <div class="modal-body" id="registerLoginBody">
      <label>Email</label>
      <input id="logEmail" type="text" class="span3">
      <label>Password</label>
      <input id="logPassword" type="password" class="span3">
    </div>
    <div class="modal-footer">
      <a class="btn" data-dismiss="modal">Close</a>
      <a id="loginButton" class="btn btn-primary" data-dismiss="modal">Login</a>
    </div>
  </form>
</div>
    """)
    dialogHtml.appendTo('body')
    $('#loginButton').click(login)
    $('#suName').keyup(updateUsername)
    $('#suUsername').keyup(stopUpdatingUsername)
    

showSignUpDialog = () ->
  $('#signUpModal').modal('show')

showLoginDialog = () ->
  $('#loginModal').modal('show')

clearSignupErrors = ->
  $('#nameFieldSet').removeClass('control-group error')
  $('#usernameFieldSet').removeClass('control-group error')
  $('#emailFieldSet').removeClass('control-group error')
  $('#passFieldSet').removeClass('control-group error')
  $('#nameErrMsg').html('')
  $('#usernameErrMsg').html('')
  $('#emailErrMsg').html('')
  $('#passErrMsg').html('')

signup = ->
  clearSignupErrors()

  name = $("#suName").val()
  username = $("#suUsername").val()
  email = $("#suEmail").val()
  password = $("#suPassword").val()
  password2 = $("#suPassword2").val()

  if (name == '')
    $('#nameFieldSet').addClass('control-group error')
    $('#nameErrMsg').html('Name cannot be empty.')
    return false

  if (username == '')
    $('#usernameFieldSet').addClass('control-group error')
    $('#usernameErrMsg').html('Username cannot be empty.')
    return false

  if (email == '')
    $('#emailFieldSet').addClass('control-group error')
    $('#emailErrMsg').html('Email cannot be empty.')
    return false

  filter = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/
  if !filter.test(email)
    $('#emailFieldSet').addClass('control-group error')
    $('#emailErrMsg').html('Not a valid email address.')
    return false    

  if (password == '')
    $('#passFieldSet').addClass('control-group error')
    $('#passErrMsg').html('You must specify a password.')
    return false

  if (password != password2)
    $('#passFieldSet').addClass('control-group error')
    $('#passErrMsg').html('Passwords do not match.')
    return false

  $.ajax({
    type: "POST",
    url: "/signup",
    data: "name=" + name + "&email=" + email + "&password=" + password,
    dataType: "text",
    success: signupReply
  })
  false

login = ->
  $.ajax({
    type: "POST",
    url: "/login",
    data: "email=" + $("#logEmail").val() + "&password=" + $("#logPassword").val(),
    dataType: "text",
    success: loginReply
  })
  false

signupReply = (msg) ->
  console.log('signup reply')

loginReply = (msg) ->
  console.log('login reply')

stopUpdatingUsername = (msg) ->
  autoUpdateUsername = false

updateUsername = (msg) ->
  if autoUpdateUsername
    username = $("#suName").val().toLowerCase().replaceAll(" ", "_")
    $("#suUsername").val(username)