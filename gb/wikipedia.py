#!/usr/bin/env python
# -*- coding: utf-8 -*-


import urllib
import urllib2
import re
 

def getpage(title):
    opener = urllib2.build_opener()
    opener.addheaders = [('User-agent', 'Mozilla/5.0')]
    title = title.replace(' ', '_')
    title = urllib.quote(title.encode('utf8'))
    url = 'http://en.wikipedia.org/w/index.php?title=' + title + '&action=raw';
    infile = opener.open(url)
    s = infile.read()

    return ''.join(s)


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


def text_and_or_link(s):
    text = ''
    link = ''
    matches = re.findall('\[\[([^\]]*)\]\]', s)
    if len(matches) > 0:
        parts = matches[0].split('|')
        link = parts[0].strip()
        if len(parts) > 1:
            text = parts[1].strip()
        else:
            text = link
    else:
        text = s.strip()

    return text, link


def br_list(s):
    result = []
    items = s.split('<br />')
    for i in items:
        result.append(i.strip())
    return result