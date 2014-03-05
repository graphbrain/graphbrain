var State;

State = (function() {

  function State() {
    this.values = {};
    if (localStorage.getItem('newedges') !== null) {
      this.values['newedges'] = JSON.parse(localStorage.getItem('newedges'));
    }
  }

  State.prototype.setNewEdges = function(newedges) {
    localStorage.setItem('newedges', JSON.stringify(newedges));
    return this.values['newedges'] = newedges;
  };

  State.prototype.getNewEdges = function() {
    return this.values['newedges'];
  };

  State.prototype.clean = function() {
    return localStorage.removeItem('newedges');
  };

  return State;

})();
