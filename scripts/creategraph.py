#!/usr/bin/env python


import sys

from gb.user import User
from gb.graph import Graph
from gb.node import Node
from gb.link import Link


if __name__ == '__main__':
    email = sys.argv[1] 
    graph_name = sys.argv[2]
    root_node = sys.argv[3]

    u = User().get_by_email(email)
    g = Graph().create(graph_name, u)
    root = Node().create(root_node, g)
    g.set_root(root)

    # set admin permission for owner
    g.set_permission(u, 0)
