#!/usr/bin/env python
# -*- coding: utf-8 -*-


import urllib
import urllib2
import re
 

def encodetitle(title):
    title = title.replace(' ', '_')
    title = urllib.quote(title.encode('utf8'))
    return title


def getpage(title, encode=True):
    opener = urllib2.build_opener()
    opener.addheaders = [('User-agent', 'Mozilla/5.0')]
    if encode:
        title = encodetitle(title)
    url = 'http://en.wikipedia.org/w/index.php?title=' + title + '&action=raw';
    try:
        infile = opener.open(url)
    except:
        print "ERROR - getpage(): couldn't open url: ", url
        return ''
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
        items2 = i.split('<br/>')
        for i2 in items2:
            items3 = i2.split('<br>')
            for i3 in items3:
                items4 = i3.split('\n')
                for i4 in items4:
                    result.append(i4.strip())
    return result


def get_image_html(title):
    opener = urllib2.build_opener()
    opener.addheaders = [('User-agent', 'Mozilla/5.0')]
    try:
        title = encodetitle(title)
    except:
        print "ERROR - get_image_html(): couldn't encode title"
    url = 'http://en.wikipedia.org/wiki/File:' + title;
    try:
        infile = opener.open(url)
    except:
        print "ERROR - get_image_html(): couldn't open url:", url
        return ''
    s = infile.read()

    return ''.join(s)


def get_image_url(name):
    url = ''
    html = get_image_html(name)
    m = re.findall('\<div class="fullMedia"\>\<a href="([^"]*)', html)
    if len(m) > 0:
        try:
            url = u'http:%s' % m[0]
        except:
            print "ERROR - get_image_url(): couldn't generate url"
    return url