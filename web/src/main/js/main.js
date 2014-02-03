var g, state;

g = false;

state = false;

$(function() {
  Math.seedrandom("GraphBrain GraphBrain");
  state = new State();
  if (typeof data !== "undefined" && data !== null) {
    g = Graph.initGraph(state.getNewEdges());
  }
  initInterface();
  if (typeof data !== "undefined" && data !== null) {
    initRelations();
  }
  browserSpecificTweaks();
  if (typeof data !== "undefined" && data !== null) {
    if (g.changedSNode === null) {
      addAnim(new AnimInitRotation());
    } else {
      addAnim(new AnimLookAt(g.changedSNode));
    }
  }
  return state.clean();
});
