import graphbrain.constants as const
from graphbrain.cognition.agent import Agent
from graphbrain.op import create_op


class Taxonomy(Agent):
    def process_edge(self, edge, depth):
        if not edge.is_atom():
            et = edge.type()
            if et[0] == 'C':
                ct = edge[0].connector_type()
                parent = None
                if ct[0] == 'B':
                    mcs = edge.main_concepts()
                    if len(mcs) == 1:
                        parent = mcs[0]
                elif ct[0] == 'M' and len(edge) == 2:
                    parent = edge[1]
                if parent:
                    ont_edge = (const.type_of_pred, edge, parent)
                    yield create_op(ont_edge, primary=False)
