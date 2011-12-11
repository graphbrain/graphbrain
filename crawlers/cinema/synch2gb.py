#!/usr/bin/env python
# -*- coding: utf-8 -*-


import sys
from pymongo import Connection
from gb.node import Node
from gb.graph import Graph
from gb.user import User
from gb.ids import url_id, wikipedia_id, gb_id


def get_person_node(db, graph, person_id):
    person = db.people.find_one({'_id': person_id})
    _id = ''
    if 'wptitle' in person:
        _id = wikipedia_id(person['wptitle'])
    else:
        _id = gb_id(person['name'], 'cinema')
    person_node = Node().create_or_get_by_id(label=person['name'], graph=graph, _id=_id, crawler='cinema')
    if 'image' in person.keys():
        _id = url_id(person['image'])
        image_node = Node().create_or_get_by_eid(label=person['image'], graph=graph, _id=_id, crawler='cinema', node_type='image')
        graph.add_link(image_node, person_node, 'photo of', 'photo of')
    return person_node


def link_film_to_people(db, graph, film_node, people, rel):
    for person in people:
        person_node = get_person_node(db, graph, person)
        graph.add_link(person_node, film_node, rel, rel)    


def process_film(db, graph, film):
    _id = wikipedia_id(film['wptitle'])
    film_node = Node().create_or_get_by_id(label=film['title'], graph=graph, _id=_id, crawler='cinema')

    actors = []
    producers = []
    writers = []
    directors = []
    musicians = []
    poster = ''

    if 'actors' in film.keys():
        actors = film['actors']
    if 'producers' in film.keys():
        producers = film['producers']
    if 'writers' in film.keys():
        writers = film['writers']
    if 'directors' in film.keys():
        directors = film['directors']
    if 'musicians' in film.keys():
        musicians = film['musicians']

    link_film_to_people(db, graph, film_node, actors, 'actor in')
    link_film_to_people(db, graph, film_node, producers, 'producer of')
    link_film_to_people(db, graph, film_node, writers, 'writer of')
    link_film_to_people(db, graph, film_node, directors, 'director of')
    link_film_to_people(db, graph, film_node, musicians, 'musician for')

    if 'poster' in film.keys():
        _id = url_id(film['poster'])
        poster_node = Node().create_or_get_by_eid(label=film['poster'], graph=graph, _id=_id, crawler='cinema', node_type='image')
        graph.add_link(poster_node, film_node, 'poster of', 'poster of')


def synch(graph_owner, graph_name):
    # get graph
    u = User().get_by_email(graph_owner)
    graph = Graph().get_by_owner_and_name(u, graph_name)

    db = Connection().cinema

    # synch films
    mfilms = db.films
    total = mfilms.count()
    count = 1
    q = mfilms.find(timeout=False)
    for film in q:
        print 'Synching film: %s [%d/%d] (%f%%)' % (film['title'], count, total, (float(count) / float(total)) * 100)
        process_film(db, graph, film)
        count += 1


if __name__ == '__main__':
    graph_owner = 'gb@graphbrain.com'
    graph_name = 'Main'
    synch(graph_owner, gra ph_name)