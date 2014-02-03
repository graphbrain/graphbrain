var initRelations, relationReply, relationSubmit;

initRelations = function() {
  var count, eventData, html, label, r, rels, _i, _j, _len, _len1, _results;
  html = "";
  rels = data['allrelations'];
  count = 0;
  for (_i = 0, _len = rels.length; _i < _len; _i++) {
    r = rels[_i];
    label = g.label(r['label'], r['pos']) + '<br />';
    if (g.snodes[r['snode']] === void 0) {
      html += '<a class="visible_rel_link" href="#" id="rel' + count + '">' + label + '</a>';
    } else {
      html += '<a class="hidden_rel_link" href="#" id="rel' + count + '">' + label + '</a>';
    }
    count += 1;
  }
  $('#rel-list').html(html);
  count = 0;
  _results = [];
  for (_j = 0, _len1 = rels.length; _j < _len1; _j++) {
    r = rels[_j];
    eventData = {
      rel: r['rel'],
      pos: r['pos'],
      snode: r['snode']
    };
    $('#rel' + count).bind('click', eventData, relationSubmit);
    _results.push(count += 1);
  }
  return _results;
};

relationSubmit = function(msg) {
  var eventData;
  eventData = msg.data;
  if (g.snodes[eventData.snode] === void 0) {
    $.ajax({
      type: "POST",
      url: "/rel",
      data: "rel=" + eventData.rel + "&pos=" + eventData.pos + "&rootId=" + rootNodeId,
      dataType: "json",
      success: relationReply
    });
  } else {
    addAnim(new AnimLookAt(g.snodes[eventData.snode]));
  }
  return false;
};

relationReply = function(msg) {
  var k, sid, snode, v, _ref;
  g.addSNodesFromJSON(msg);
  initRelations();
  sid = '';
  _ref = msg['snodes'];
  for (k in _ref) {
    v = _ref[k];
    sid = k;
  }
  if (sid !== '') {
    snode = g.snodes[sid];
    return addAnim(new AnimLookAt(snode));
  }
};
