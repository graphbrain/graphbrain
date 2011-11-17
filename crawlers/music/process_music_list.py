#!/usr/bin/env python
# -*- coding: utf-8 -*-


import re
from re import findall, DOTALL
import urllib2
import sys
from pymongo import Connection
from gb import wikipedia


def album_list(markup):
    albums = []
    lines = markup.split('\n')

    for l in lines:
        if (len(l) > 0) and (l[0] == '*'):
            aa=l.split(" - ");
            if(len(aa)>1):
                artist_matches=re.findall('\[\[([^\]]*)\]\]', aa[0])
                album_matches=re.findall('\[\[([^\]]*)\]\]', aa[1])
                if(len(album_matches)>0):
                    s = album_matches[0].split('|')
                if(len(artist_matches)>0):
                    artist=artist_matches[0].split('|')
                else:
                    artist=aa[0].strip('* ')
                wptitle = s[0]
                title = wptitle        
            
                a = {'title': title, 'wptitle': wptitle, 'artist': artist}
                albums.append(a)

    return albums


def main():
    # from http://en.wikipedia.org/wiki/List_of_concept_albums
    list_pages = ('0.E2.80.939', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')

    db = Connection().albums

    count = 0
    inserted = 0

    for l in list_pages:
        title = 'List_of_concept_albums#%s' % l
        print title
        page = wikipedia.getpage(title)
        albums = album_list(page)
        for a in albums:
            malbums = db.albums
            if malbums.find_one({'title': a['title']}) is None:
                malbums.insert(a)
                inserted += 1
                print '"%s" inserted' % a['title']
            else:
                print 'NOT INSERTED: "%s"' % a['title']
        count += len(albums)

    print '%d albums found, %d inserted.' % (count, inserted)
    

if __name__=='__main__':
    main()