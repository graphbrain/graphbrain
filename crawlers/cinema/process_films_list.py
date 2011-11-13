#!/usr/bin/env python
# -*- coding: utf-8 -*-


import re
from re import findall, DOTALL
import urllib2
import sys
from pymongo import Connection
from gb import wikipedia


def film_list(markup):
    films = []
    lines = markup.split('\n')
    for l in lines:
        if (len(l) > 0) and (l[0] == '*'):
            matches = re.findall('\[\[([^\]]*)\]\]', l)
            if len(matches) != 2:
                continue
            s = matches[0].split('|')
            wptitle = s[0]
            title = wptitle
            if len(s) > 1:
                title = s[1]
            s = matches[1].split('|')
            year = s[0]
            if len(s) > 1:
                year = s[1]
            f = {'title': title, 'wptitle': wptitle, 'year': year}
            films.append(f)

    return films


def main():
    # from http://en.wikipedia.org/wiki/List_of_films#Alphabetical_indexes
    list_pages = ('numbers', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J–K', 'L', 'M', 'N–O', 'P', 'Q–R', 'S', 'T', 'U–W', 'X–Z')

    db = Connection().cinema

    count = 0
    inserted = 0

    for l in list_pages:
        title = 'List_of_films:_%s' % l
        print title
        page = wikipedia.getpage(title)
        films = film_list(page)
        for f in films:
            mfilms = db.films
            if mfilms.find_one({'title': f['title'], 'year': f['year']}) is None:
                mfilms.insert(f)
                inserted += 1
                print '"%s" inserted' % f['title']
            else:
                print 'NOT INSERTED: "%s"' % f['title']
        count += len(films)

    print '%d films found, %d inserted.' % (count, inserted)
    

if __name__=='__main__':
    main()