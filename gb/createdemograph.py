#!/usr/bin/env python


import sys

from user import User
from graph import Graph
from node import Node
from link import Link


if __name__ == '__main__':
    email = sys.argv[1] 
    u = User().get_by_email(email)
    g = Graph().create('Demo', u)
    root = Node().create('Graphbrain', g)
    g.set_root(root)
    n = Node().create('Computer Program', g)
    l = Link().create(root, n, 'is')
