#!/usr/bin/env python
# -*- coding: utf-8 -*-



import sys
from pymongo import Connection
from gb.node import Node
from gb.graph import Graph
from gb.user import User

def get_person_node(db, graph, person_id):
    person = db.people.find_one({'_id': person_id})
    eid = ''
    if 'wptitle' in person:
        eid = person['wptitle']
    else:
        eid = 'album:person:%s' % person['name']
    person_node = Node().create_or_get_by_eid(label=person['name'], graph=graph, eid=eid, crawler='album')
    if 'image' in person.keys():
        image_node = Node().create_or_get_by_eid(label=person['image'], graph=graph, eid=person['image'], crawler='album', node_type='image')
        graph.add_link(image_node, person_node, 'photo of', 'photo of')
    return person_node

def link_album_to_people(db, graph, album_node, people, rel):
    for person in people:
        person_node = get_person_node(db, graph, person)
        graph.add_link(person_node, album_node, rel, rel)    

def process_album(db, graph, album):
    album_node = Node().create_or_get_by_eid(label=album['title'], graph=graph, eid=album['wptitle'], crawler='album')

    artists = []
    producers = []
    cover = ''

    if 'artists' in album.keys():
        artists = album['artists']
    if 'producers' in album.keys():
        producers = album['producers']

    link_album_to_people(db, graph, album_node, artists, 'created')
    link_album_to_people(db, graph, album_node, producers, 'producer of')

    if 'cover' in album.keys():
        cover_node = Node().create_or_get_by_eid(label=album['cover'], graph=graph, eid=album['cover'], crawler='album', node_type='image')
        graph.add_link(cover_node, album_node, 'cover of', 'cover of')




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