var hideAlert, initAlert, setErrorAlert, setInfoAlert;

initAlert = function() {
  return $('#alert').css('display', 'none');
};

$('#alert').css('visibility', 'visible');

setInfoAlert = function(msg) {
  $('#alert').css('display', 'block');
  $('#alert').removeClass('alert-error');
  $('#alert').addClass('alert-info');
  return $('#alertMsg').html(msg);
};

setErrorAlert = function(msg) {
  $('#alert').css('display', 'block');
  $('#alert').removeClass('alert-info');
  $('#alert').addClass('alert-error');
  return $('#alertMsg').html(msg);
};

hideAlert = function() {
  return $('#alert').css('display', 'none');
};
