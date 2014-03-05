String.prototype.replaceAll = function(str1, str2, ignore) {
  return this.replace(new RegExp(str1.replace(/([\/\,\!\\\^\$\{\}\[\]\(\)\.\*\+\?\|\<\>\-\&])/g,"\\$&"),(ignore?"gi":"g")),(typeof(str2)=="string")?str2.replace(/\$/g,"$$$$"):str2);
};

var autoUpdateUsername, checkEmail, checkEmailReply, checkUsername, checkUsernameReply, clearSignupErrors, emailChanged, emailStatus, initSignUpDialog, login, loginReply, loginRequest, logout, showSignUpDialog, signup, signupReply, submitting, updateUsername, usernameChanged, usernameStatus;

autoUpdateUsername = true;

usernameStatus = 'unknown';

emailStatus = 'unknown';

submitting = false;

initSignUpDialog = function() {
  var dialogHtml;
  dialogHtml = $("\n<div class=\"modal hide\" id=\"signUpModal\" style=\"width:650px; height:500px; margin: -295px 0 0 -325px;\">\n  <div class=\"modal-header\">\n    <a class=\"close\" data-dismiss=\"modal\">×</a>\n    <h3>Register or Login</h3>\n  </div>\n\n  <div class=\"modal-body\" id=\"registerLoginBody\" style=\"height:500px; overflow:hidden;\">\n    <div style=\"float:left\">\n      <h5>REGISTER NEW ACCOUNT</h5>\n      <span id=\"signupErrMsg\" class=\"error\" />\n      <form class=\"signupForm\">\n        <fieldset id=\"nameFieldSet\">\n          <label>Name</label>\n          <input id=\"suName\" type=\"text\" class=\"span3\" placeholder=\"Or an alias if you prefer\">\n        </fieldset>\n        <fieldset id=\"usernameFieldSet\">\n          <label>Username</label>\n          <input id=\"suUsername\" type=\"text\" class=\"span3\" placeholder=\"Unique identifier\">\n        </fieldset>\n        <fieldset id=\"emailFieldSet\">\n          <label>Email</label>\n          <input id=\"suEmail\" type=\"text\" class=\"span3\" placeholder=\"Will not be seen by other members\">\n        </fieldset>\n        <fieldset id=\"passFieldSet\">\n          <label>Password</label>\n          <input id=\"suPassword\" type=\"password\" class=\"span3\" placeholder=\"A good password\">\n          <br />\n          <input id=\"suPassword2\" type=\"password\" class=\"span3\" placeholder=\"Confirm password\">\n        </fieldset>\n    \n        <br />\n        <a id=\"signupButton\" class=\"btn btn-primary\">Sign Up</a>\n      </form>\n    </div>\n\n    <div style=\"float:right\">\n      <h5>LOGIN</h5>\n      <span id=\"loginErrMsg\" class=\"error\" />\n      <form class=\"loginForm\">\n        <fieldset id=\"logEmailFieldSet\">\n          <label>Email or Username</label>\n          <input id=\"logEmail\" type=\"text\" class=\"span3\">\n        </fieldset>\n        <fieldset id=\"logPassFieldSet\">\n          <label>Password</label>\n          <input id=\"logPassword\" type=\"password\" class=\"span3\">\n        </fieldset>\n      \n        <br />\n        <a id=\"loginButton\" class=\"btn btn-primary\" data-dismiss=\"modal\">Login</a>\n      </form>\n    </div>\n\n  </div>\n</div>");
  dialogHtml.appendTo('body');
  $('#signupButton').click(signup);
  $('#loginButton').click(login);
  $('#suName').keyup(updateUsername);
  $('#suName').blur(checkUsername);
  $('#suUsername').keyup(usernameChanged);
  $('#suUsername').blur(checkUsername);
  $('#suEmail').keyup(emailChanged);
  return $('#suEmail').blur(checkEmail);
};

showSignUpDialog = function() {
  return $('#signUpModal').modal('show');
};

clearSignupErrors = function() {
  $('#nameFieldSet').removeClass('control-group error');
  $('#usernameFieldSet').removeClass('control-group error');
  $('#emailFieldSet').removeClass('control-group error');
  $('#passFieldSet').removeClass('control-group error');
  $('#signupErrMsg').html('');
  $('#logEmailFieldSet').removeClass('control-group error');
  $('#logPassFieldSet').removeClass('control-group error');
  return $('#loginErrMsg').html('');
};

signup = function() {
  var email, filter, name, password, password2, username;
  clearSignupErrors();
  name = $("#suName").val();
  username = $("#suUsername").val();
  email = $("#suEmail").val();
  password = $("#suPassword").val();
  password2 = $("#suPassword2").val();
  if (name === '') {
    $('#nameFieldSet').addClass('control-group error');
    $('#signupErrMsg').html('Name cannot be empty.');
    return false;
  }
  if (username === '') {
    $('#usernameFieldSet').addClass('control-group error');
    $('#signupErrMsg').html('Username cannot be empty.');
    return false;
  }
  if (email === '') {
    $('#emailFieldSet').addClass('control-group error');
    $('#signupErrMsg').html('Email cannot be empty.');
    return false;
  }
  filter = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
  if (!filter.test(email)) {
    $('#emailFieldSet').addClass('control-group error');
    $('#signupErrMsg').html('Not a valid email address.');
    return false;
  }
  if (password === '') {
    $('#passFieldSet').addClass('control-group error');
    $('#signupErrMsg').html('You must specify a password.');
    return false;
  }
  if (password !== password2) {
    $('#passFieldSet').addClass('control-group error');
    $('#signupErrMsg').html('Passwords do not match.');
    return false;
  }
  if (usernameStatus === 'exists') {
    return false;
  } else if (usernameStatus === 'unknown') {
    submitting = true;
    checkUsername();
    return false;
  }
  if (emailStatus === 'exists') {
    return false;
  } else if (emailStatus === 'unknown') {
    submitting = true;
    checkEmail();
    return false;
  }
  $.ajax({
    type: "POST",
    url: "/signup",
    data: "name=" + name + "&username=" + username + "&email=" + email + "&password=" + password,
    dataType: "text",
    success: signupReply
  });
  return false;
};

login = function() {
  var logEmail, password;
  logEmail = $("#logEmail").val();
  password = $("#logPassword").val();
  return loginRequest(logEmail, password);
};

loginRequest = function(logEmail, password) {
  clearSignupErrors();
  if (logEmail === '') {
    $('#logEmailFieldSet').addClass('control-group error');
    $('#loginErrMsg').html('Email / Username cannot be empty.');
    return false;
  }
  if (password === '') {
    $('#logPassFieldSet').addClass('control-group error');
    $('#loginErrMsg').html('Password cannot be empty.');
    return false;
  }
  $.ajax({
    type: "POST",
    url: "/login",
    data: "login=" + logEmail + "&password=" + password,
    dataType: "text",
    success: loginReply
  });
  return false;
};

signupReply = function(msg) {
  return loginRequest($("#suEmail").val(), $("#suPassword").val());
};

loginReply = function(msg) {
  var response;
  if (msg === "failed") {
    return $('#loginErrMsg').html('Wrong username / email or password.');
  } else {
    response = msg.split(' ');
    $.cookie('username', response[0], {
      path: '/'
    });
    $.cookie('session', response[1], {
      path: '/'
    });
    if (typeof data !== "undefined" && data !== null) {
      return location.reload();
    } else {
      return window.location.href = '/node/user/' + response[0];
    }
  }
};

logout = function() {
  $.cookie('username', '', {
    path: '/'
  });
  $.cookie('session', '', {
    path: '/'
  });
  return location.reload();
};

usernameChanged = function(msg) {
  autoUpdateUsername = false;
  return usernameStatus = 'unknown';
};

updateUsername = function(msg) {
  var username;
  if (autoUpdateUsername) {
    username = $("#suName").val().toLowerCase().replaceAll(" ", "_");
    $("#suUsername").val(username);
    return usernameStatus = 'unknown';
  }
};

emailChanged = function(msg) {
  return emailStatus = 'unknown';
};

checkUsername = function() {
  if ($("#suUsername").val() !== '') {
    return $.ajax({
      type: "POST",
      url: "/checkusername",
      data: "username=" + $("#suUsername").val(),
      dataType: "text",
      success: checkUsernameReply
    });
  }
};

checkUsernameReply = function(msg) {
  var response, status, username;
  response = msg.split(' ');
  status = response[0];
  username = response[1];
  if (username === $("#suUsername").val()) {
    if (status === 'ok') {
      usernameStatus = 'ok';
      $('#usernameFieldSet').removeClass('control-group error');
      $('#usernameFieldSet').addClass('control-group success');
      if (submitting) {
        return signup();
      }
    } else {
      usernameStatus = 'exists';
      $('#usernameFieldSet').removeClass('control-group success');
      $('#usernameFieldSet').addClass('control-group error');
      $('#signupErrMsg').html('Sorry, this username is already in use.');
      return submitting = false;
    }
  }
};

checkEmail = function() {
  if ($("#suEmail").val() !== '') {
    return $.ajax({
      type: "POST",
      url: "/checkemail",
      data: "email=" + $("#suEmail").val(),
      dataType: "text",
      success: checkEmailReply
    });
  }
};

checkEmailReply = function(msg) {
  var email, response, status;
  response = msg.split(' ');
  status = response[0];
  email = response[1];
  if (email === $("#suEmail").val()) {
    if (status === 'ok') {
      emailStatus = 'ok';
      $('#emailFieldSet').removeClass('control-group error');
      $('#emailFieldSet').addClass('control-group success');
      $('#emailErrMsg').html('');
      if (submitting) {
        return signup();
      }
    } else {
      emailStatus = 'exists';
      $('#emailFieldSet').removeClass('control-group success');
      $('#emailFieldSet').addClass('control-group error');
      $('#signupErrMsg').html('Sorry, this email is already in use.');
      return submitting = false;
    }
  }
};
