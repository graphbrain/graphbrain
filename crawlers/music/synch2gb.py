#!/usr/bin/env python
# -*- coding: utf-8 -*-



import sys
from pymongo import Connection
from gb.node import Node
from gb.graph import Graph


def process_album(db, graph, album):
    Node().create(album['title'], graph)


def process_person(db, graph, person):
    pass


def synch(graph_owner, graph_name):
    # get graph
    graph = Graph().get_by_owner_and_name(graph_owner, graph_name)

    db = Connection().albums

    # synch albums
    malbums = db.albums
    total = malbums.count()
    count = 1
    q = malbums.find()
    for album in q:
        print 'Synching album: %s [%d/%d] (%f%%)' % (album['title'], count, total, (float(count) / float(total)) * 100)
        process_album(db, graph, album)
        count += 1

    # synch people
    mpeople = db.people
    total = mpeople.count()
    count = 1
    q = mpeople.find()
    for person in q:
        print 'Synching person: %s [%d/%d] (%f%%)' % (person['name'], count, total, (float(count) / float(total)) * 100)
        process_person(db, graph, person)
        count += 1


if __name__=='__main__':
    graph_owner = 'gb@graphbrain.com'
    graph_name = 'Albums'
    synch(graph_owner, graph_name)