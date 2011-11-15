#!/usr/bin/env python
# -*- coding: utf-8 -*-


import re
from re import findall, DOTALL
import urllib
import urllib2
import sys
from pymongo import Connection
from gb import wikipedia 


def person_id(db, name, wptitle):
    pid = None
    mpeople = db.people
    d = {}
    d['name'] = name
    if len(wptitle) > 0:
        d['wptitle'] = wptitle
    person = mpeople.find_one(d)
    if person is None:
        pid = mpeople.insert(d)
    else:
        pid = person['_id']

    return pid


def people_list(db, s):
    result = []
    items = wikipedia.br_list(s)
    for i in items:
        name,  wptitle = wikipedia.text_and_or_link(i)
        result.append(person_id(db, name, wptitle))
    return result


def process_page(db, wptitle, film):
    wpage = wikipedia.getpage(wptitle)
    sections = wikipedia.page2sections(wpage)

    producers = []
    writers = []
    directors = []
    musicians = []
    starring = []
    poster = ''

    # process infobox
    properties = {}
    if '' in sections:
        lines = sections[''].split('\n')
        for l in lines:
            l = l.strip()
            if (len(l) > 1) and (l[0] == '|'):
                l = l.strip('|')
                prop = l.split('=')
                if len(prop) > 1:
                    key = prop[0].strip()
                    value = prop[1].strip()
                    if key == 'producer':
                        producers = people_list(db, value)
                    elif key == 'writer':
                        writers = people_list(db, value)
                    elif key == 'director':
                        directors = people_list(db, value)
                    elif key == 'music':
                        musicians = people_list(db, value)
                    elif key == 'starring':
                        starring = people_list(db, value)
                    elif key == 'image':
                        poster = wikipedia.get_image_url(value.strip())
                    elif key == 'poster':
                        poster = wikipedia.get_image_url(value.strip())
                    #properties[key] = value
    #print properties

    # process cast
    cast = []
    if 'cast' in sections:
        #print '->Cast'
        lines = sections['cast'].split('\n')
        for l in lines:
            l = l.strip()
            if (len(l) > 1) and (l[0] == '*'):
                l = l.strip('* ')
                role = l.split(' as ')
                text, link = wikipedia.text_and_or_link(role[0])
                #print '%s [%s]' % (text, link)
                cast.append(person_id(db, text, link))

    actors = cast
    for s in starring:
        if not s in actors:
            actors.append(s)

    # update db
    mfilms = db.films
    film = mfilms.find_one({'wptitle': wptitle})
    d = {}
    if len(actors) > 0:
        d['actors'] = actors
    if len(producers) > 0:
        d['producers'] = producers
    if len(writers) > 0:
        d['writers'] = writers
    if len(directors) > 0:
        d['directors'] = directors
    if len(musicians) > 0:
        d['musicians'] = musicians
    if len(poster) > 0:
        d['poster'] = poster
    mfilms.update({'_id': film['_id']}, {'$set': d})

def main():
    db = Connection().cinema

    mfilms = db.films
    total = mfilms.count()
    count = 1
    q = mfilms.find()
    for film in q:
        wptitle = film['wptitle']
        print 'Processing: %s [%d/%d] (%f%%)' % (wptitle, count, total, (float(count) / float(total)) * 100)
        process_page(db, wptitle, film)
        count += 1


if __name__=='__main__':
    main()