`String.prototype.replaceAll = function(str1, str2, ignore) {
  return this.replace(new RegExp(str1.replace(/([\/\,\!\\\^\$\{\}\[\]\(\)\.\*\+\?\|\<\>\-\&])/g,"\\$&"),(ignore?"gi":"g")),(typeof(str2)=="string")?str2.replace(/\$/g,"$$$$"):str2);
}`


autoUpdateUsername = true
usernameStatus = 'unknown'
emailStatus = 'unknown'
submitting = false


initSignUpDialog = () ->
    dialogHtml = $("""

<div class="modal hide" id="signUpModal" style="width:650px; height:500px; margin: -295px 0 0 -325px;">
  <div class="modal-header">
    <a class="close" data-dismiss="modal">×</a>
    <h3>Register or Login</h3>
  </div>

  <div class="modal-body" id="registerLoginBody" style="height:500px; overflow:hidden;">
    <div style="float:left">
      <h5>REGISTER NEW ACCOUNT</h5>
      <span id="signupErrMsg" class="error" />
      <form class="signupForm">
        <fieldset id="nameFieldSet">
          <label>Name</label>
          <input id="suName" type="text" class="span3" placeholder="Or an alias if you prefer">
        </fieldset>
        <fieldset id="usernameFieldSet">
          <label>Username</label>
          <input id="suUsername" type="text" class="span3" placeholder="Unique identifier">
        </fieldset>
        <fieldset id="emailFieldSet">
          <label>Email</label>
          <input id="suEmail" type="text" class="span3" placeholder="Will not be seen by other members">
        </fieldset>
        <fieldset id="passFieldSet">
          <label>Password</label>
          <input id="suPassword" type="password" class="span3" placeholder="A good password">
          <br />
          <input id="suPassword2" type="password" class="span3" placeholder="Confirm password">
        </fieldset>
    
        <br />
        <a id="signupButton" class="btn btn-primary">Sign Up</a>
      </form>
    </div>

    <div style="float:right">
      <h5>LOGIN</h5>
      <span id="loginErrMsg" class="error" />
      <form class="loginForm">
        <fieldset id="logEmailFieldSet">
          <label>Email or Username</label>
          <input id="logEmail" type="text" class="span3">
        </fieldset>
        <fieldset id="logPassFieldSet">
          <label>Password</label>
          <input id="logPassword" type="password" class="span3">
        </fieldset>
      
        <br />
        <a id="loginButton" class="btn btn-primary" data-dismiss="modal">Login</a>
      </form>
    </div>

  </div>
</div>
    """)
    dialogHtml.appendTo('body')
    $('#signupButton').click(signup)
    $('#loginButton').click(login)
    $('#suName').keyup(updateUsername)
    $('#suName').blur(checkUsername)
    $('#suUsername').keyup(usernameChanged)
    $('#suUsername').blur(checkUsername)
    $('#suEmail').keyup(emailChanged)
    $('#suEmail').blur(checkEmail)

showSignUpDialog = () ->
  $('#signUpModal').modal('show')

clearSignupErrors = ->
  $('#nameFieldSet').removeClass('control-group error')
  $('#usernameFieldSet').removeClass('control-group error')
  $('#emailFieldSet').removeClass('control-group error')
  $('#passFieldSet').removeClass('control-group error')
  $('#signupErrMsg').html('')

  $('#logEmailFieldSet').removeClass('control-group error')
  $('#logPassFieldSet').removeClass('control-group error')
  $('#loginErrMsg').html('')

signup = ->
  clearSignupErrors()

  name = $("#suName").val()
  username = $("#suUsername").val()
  email = $("#suEmail").val()
  password = $("#suPassword").val()
  password2 = $("#suPassword2").val()

  if (name == '')
    $('#nameFieldSet').addClass('control-group error')
    $('#signupErrMsg').html('Name cannot be empty.')
    return false

  if (username == '')
    $('#usernameFieldSet').addClass('control-group error')
    $('#signupErrMsg').html('Username cannot be empty.')
    return false

  if (email == '')
    $('#emailFieldSet').addClass('control-group error')
    $('#signupErrMsg').html('Email cannot be empty.')
    return false

  filter = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/
  if !filter.test(email)
    $('#emailFieldSet').addClass('control-group error')
    $('#signupErrMsg').html('Not a valid email address.')
    return false    

  if (password == '')
    $('#passFieldSet').addClass('control-group error')
    $('#signupErrMsg').html('You must specify a password.')
    return false

  if (password != password2)
    $('#passFieldSet').addClass('control-group error')
    $('#signupErrMsg').html('Passwords do not match.')
    return false

  if usernameStatus == 'exists'
    return false
  else if usernameStatus == 'unknown'
    submitting = true
    checkUsername()
    return false

  if emailStatus == 'exists'
    return false
  else if emailStatus == 'unknown'
    submitting = true
    checkEmail()
    return false

  $.ajax({
    type: "POST",
    url: "/signup",
    data: "name=" + name + "&username=" + username + "&email=" + email + "&password=" + password,
    dataType: "text",
    success: signupReply
  })
  false

login = ->
  logEmail = $("#logEmail").val()
  password = $("#logPassword").val()
  loginRequest(logEmail, password)

loginRequest = (logEmail, password) ->
  clearSignupErrors()

  if logEmail == ''
    $('#logEmailFieldSet').addClass('control-group error')
    $('#loginErrMsg').html('Email / Username cannot be empty.')
    return false

  if password == ''
    $('#logPassFieldSet').addClass('control-group error')
    $('#loginErrMsg').html('Password cannot be empty.')
    return false

  $.ajax({
    type: "POST",
    url: "/login",
    data: "login=" + logEmail + "&password=" + password,
    dataType: "text",
    success: loginReply
  })
  false

signupReply = (msg) ->
  loginRequest($("#suEmail").val(), $("#suPassword").val())

loginReply = (msg) ->
  if msg == "failed"
    $('#loginErrMsg').html('Wrong username / email or password.')
  else
    response = msg.split(' ')
    $.cookie('username', response[0], { path: '/' })
    $.cookie('session', response[1], { path: '/' })  
    
    if data?
      location.reload()
    else
      window.location.href = '/node/user/' + response[0]

logout = ->
  $.cookie('username', '', { path: '/' })
  $.cookie('session', '', { path: '/' })
  location.reload()

usernameChanged = (msg) ->
  autoUpdateUsername = false
  usernameStatus = 'unknown'

updateUsername = (msg) ->
  if autoUpdateUsername
    username = $("#suName").val().toLowerCase().replaceAll(" ", "_")
    $("#suUsername").val(username)
    usernameStatus = 'unknown'

emailChanged = (msg) ->
  emailStatus = 'unknown'

checkUsername = ->
  if $("#suUsername").val() != ''
    $.ajax({
      type: "POST",
      url: "/checkusername",
      data: "username=" + $("#suUsername").val(),
      dataType: "text",
      success: checkUsernameReply
    })

checkUsernameReply = (msg) ->
  response = msg.split(' ')
  status = response[0]
  username = response[1]
  if username == $("#suUsername").val()
    if status == 'ok'
      usernameStatus = 'ok'
      $('#usernameFieldSet').removeClass('control-group error')
      $('#usernameFieldSet').addClass('control-group success')
      if submitting
        signup()
    else
      usernameStatus = 'exists'
      $('#usernameFieldSet').removeClass('control-group success')
      $('#usernameFieldSet').addClass('control-group error')
      $('#signupErrMsg').html('Sorry, this username is already in use.')
      submitting = false

checkEmail = ->
  if $("#suEmail").val() != ''
    $.ajax({
      type: "POST",
      url: "/checkemail",
      data: "email=" + $("#suEmail").val(),
      dataType: "text",
      success: checkEmailReply
    })

checkEmailReply = (msg) ->
  response = msg.split(' ')
  status = response[0]
  email = response[1]
  if email == $("#suEmail").val()
    if status == 'ok'
      emailStatus = 'ok'
      $('#emailFieldSet').removeClass('control-group error')
      $('#emailFieldSet').addClass('control-group success')
      $('#emailErrMsg').html('')
      if submitting
        signup()
    else
      emailStatus = 'exists'
      $('#emailFieldSet').removeClass('control-group success')
      $('#emailFieldSet').addClass('control-group error')
      $('#signupErrMsg').html('Sorry, this email is already in use.')
      submitting = false