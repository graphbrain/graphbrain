#!/usr/bin/env python
# -*- coding: utf-8 -*-


import re
from re import findall, DOTALL
import urllib
import urllib2
import sys
from pymongo import Connection
from gb import wikipedia 


def process_person(db, wptitle):
    wpage = wikipedia.getpage(wptitle)
    sections = wikipedia.page2sections(wpage)

    image = ''

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
                    if key == 'image':
                        image = wikipedia.get_image_url(value.strip())

    # update db
    mpeople = db.people
    person = mpeople.find_one({'wptitle': wptitle})
    d = {}
    if len(image) > 0:
        d['image'] = image
    mpeople.update({'_id': person['_id']}, {'$set': d})

def main():
    db = Connection().cinema

    mpeople = db.people
    total = mpeople.count()
    count = 1
    q = mpeople.find(timeout=False)
    for person in q:
        print 'Processing: %s [%d/%d] (%f%%)' % (person['name'], count, total, (float(count) / float(total)) * 100)
        if 'wptitle' in person.keys():
            wptitle = person['wptitle']
            try:
                process_person(db, wptitle)
            except:
                pass
        count += 1


if __name__=='__main__':
    main()