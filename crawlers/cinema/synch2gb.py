#!/usr/bin/env python
# -*- coding: utf-8 -*-


import sys
from pymongo import Connection
from gb.node import Node
from gb.graph import Graph


def process_film(db, graph, film):
    Node().create(film['title'], graph)


def process_person(db, graph, person):
    pass


def synch(graph_owner, graph_name):
    # get graph
    graph = Graph().get_by_owner_and_name(graph_owner, graph_name)

    db = Connection().cinema

    # synch films
    mfilms = db.films
    total = mfilms.count()
    count = 1
    q = mfilms.find()
    for film in q:
        print 'Synching film: %s [%d/%d] (%f%%)' % (film['title'], count, total, (float(count) / float(total)) * 100)
        process_film(db, graph, film)
        count += 1

    # synch people
    mpeople = db.films
    total = mpeople.count()
    count = 1
    q = mpeople.find()
    for person in q:
        print 'Synching person: %s [%d/%d] (%f%%)' % (person['name'], count, total, (float(count) / float(total)) * 100)
        process_person(db, graph, person)
        count += 1


if __name__=='__main__':
    graph_owner = 'gb@graphbrain.com'
    graph_name = 'Movies'
    synch(graph_owner, graph_name)