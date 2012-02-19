#!/usr/bin/env python


import sys

from gb.user import User
from gb.graph import Graph


if __name__ == '__main__':
    email = sys.argv[1] 
    graph_id = sys.argv[2]
    perm = sys.argv[3]

    u = User().get_by_email(email)
    g = Graph().get_by_id(graph_id)
    u.set_permission(g, perm)
