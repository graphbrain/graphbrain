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
        name, wptitle = wikipedia.text_and_or_link(i)
        result.append(person_id(db, name, wptitle))
    return result


def process_album_page(db, wptitle, album):
    print 'Processing: ', wptitle
    wpage = wikipedia.getpage(wptitle)
    sections = wikipedia.page2sections(wpage)

    artists = []
    producer=[]
    cover = ''

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
                    if key == 'Artist':
                        artist = people_list(db, value)
                    elif key == 'Producer':
                        producer = people_list(db, value)
                    elif key == 'Cover':
                        cover = wikipedia.get_image_url(value.strip())
                    properties[key] = value
    print properties

    # process band members
    if 'Personnel' in sections:
        print '->Personnel'
        lines = sections['Personnel'].split('\n')

        for l in lines:
            l = l.strip()
            if (len(l) > 1) and (l[0] == '*'):
                l = l.strip('* ')
                member=l.split('&nbsp;-')
                text, link = wikipedia.text_and_or_link(member[0])
                print '%s [%s]' % (text, link)
                print person_id(db, text, link)


def main():
    db = Connection().albums

    mcomposers = db.albums
    q = malbums.find()
    for album in q:
        wptitle = album['wptitle']
        process_page(db, wptitle, composer)


if __name__=='__main__':
    main()