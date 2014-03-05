var dragging, fullBind, initInterface, lastScale, lastX, lastY, mouseDown, mouseMove, mouseUp, mouseWheel, scroll, scrollOff, scrollOn, touchEnd, touchMove, touchStart;

dragging = false;

lastX = 0;

lastY = 0;

lastScale = -1;

scroll = false;

scrollOn = function(e) {
  return scroll = true;
};

scrollOff = function(e) {
  return scroll = false;
};

mouseUp = function(e) {
  dragging = false;
  return false;
};

mouseDown = function(e) {
  dragging = true;
  lastX = e.pageX;
  lastY = e.pageY;
  stopAnims();
  return false;
};

mouseMove = function(e) {
  var deltaX, deltaY;
  if (dragging) {
    deltaX = e.pageX - lastX;
    deltaY = e.pageY - lastY;
    lastX = e.pageX;
    lastY = e.pageY;
    g.rotateX(-deltaX * 0.0015);
    g.rotateY(deltaY * 0.0015);
    g.updateView();
  }
  return false;
};

touchStart = function(e) {
  var touch;
  stopAnims();
  if (e.touches.length === 1) {
    touch = e.touches[0];
    lastX = touch.pageX;
    lastY = touch.pageY;
  }
  return true;
};

touchEnd = function(e) {
  lastScale = -1;
  return true;
};

touchMove = function(e) {
  var deltaScale, deltaX, deltaY, dx, dy, scale, touch, x, y;
  if (e.touches.length === 1) {
    e.preventDefault();
    touch = e.touches[0];
    deltaX = touch.pageX - lastX;
    deltaY = touch.pageY - lastY;
    lastX = touch.pageX;
    lastY = touch.pageY;
    g.rotateX(-deltaX * 0.0015);
    g.rotateY(deltaY * 0.0015);
    g.updateView();
    false;
  } else if (e.touches.length === 2) {
    e.preventDefault();
    dx = e.touches[0].pageX - e.touches[1].pageX;
    dy = e.touches[0].pageY - e.touches[1].pageY;
    scale = Math.sqrt(dx * dx + dy * dy);
    if (lastScale >= 0) {
      x = (e.touches[0].pageX + e.touches[1].pageX) / 2;
      y = (e.touches[0].pageY + e.touches[1].pageY) / 2;
      deltaScale = (scale - lastScale) * 0.025;
      g.zoom(deltaScale, x, y);
    }
    lastScale = scale;
    false;
  }
  return true;
};

mouseWheel = function(e, delta, deltaX, deltaY) {
  if (!scroll) {
    g.zoom(deltaY, e.pageX, e.pageY);
  }
  return true;
};

fullBind = function(eventName, f) {
  $("#graph-view").bind(eventName, f);
  $(".snode1").bind(eventName, f);
  $(".snodeN").bind(eventName, f);
  return $(".link").bind(eventName, f);
};

initInterface = function() {
  $('#search-field').submit(searchQuery);
  initSearchDialog();
  initSignUpDialog();
  $('.signupLink').bind('click', showSignUpDialog);
  $('#loginLink').bind('click', showSignUpDialog);
  $('#logoutLink').bind('click', logout);
  fullBind("mouseup", mouseUp);
  fullBind("mousedown", mouseDown);
  fullBind("mousemove", mouseMove);
  fullBind("mousewheel", mouseWheel);
  document.addEventListener('touchstart', touchStart);
  document.addEventListener('touchend', touchEnd);
  document.addEventListener('touchmove', touchMove);
  initAlert();
  if (typeof data !== "undefined" && data !== null) {
    initAiChat();
    initRemoveDialog();
    initDisambiguateDialog();
    $('#ai-chat-button').bind('click', aiChatButtonPressed);
  }
  if (typeof errorMsg !== "undefined" && errorMsg !== null) {
    if (errorMsg !== '') {
      return setErrorAlert(errorMsg);
    }
  }
};
