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