#!/usr/bin/env python
# -*- coding: utf-8 -*-


import re
from re import findall, DOTALL
import urllib
import urllib2
import sys
from pymongo import Connection
from gb import wikipedia 


def page2sections(page):
    sections = {}
    cur_section = ''
    cur_text = ''

    lines = page.split('\n')
    for l in lines:
        if (len(l) > 4) and (l[:2] == '=='):
            if len(cur_text) > 0:
                sections[cur_section] = cur_text
            section = l.strip(' =')
            section = section.lower()
            cur_section = section
            cur_text = ''
        else:
            cur_text += l + '\n'

    if len(cur_text) > 0:
                sections[cur_section] = cur_text

    return sections


def process_page(wptitle):
    print 'Processing: ', wptitle
    wpage = wikipedia.getpage(wptitle)
    sections = page2sections(wpage)

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
                    prop[0] = prop[0].strip()
                    prop[1] = prop[1].strip()
                    properties[prop[0]] = prop[1]
    print properties

    # process cast
    if 'cast' in sections:
        print '->Cast'
        lines = sections['cast'].split('\n')
        for l in lines:
            l = l.strip()
            if (len(l) > 1) and (l[0] == '*'):
                l = l.strip('* ')
                role = l.split(' as ')
                text, link = wikipedia.text_and_or_link(role[0])
                print '%s [%s]' % (text, link)


def main():
    db = Connection().cinema

    mfilms = db.films
    q = mfilms.find()
    for film in q:
        wptitle = film['wptitle']
        process_page(wptitle)


if __name__=='__main__':
    main()