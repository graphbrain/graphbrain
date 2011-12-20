# -*- coding: utf-8 -*-

import urllib


def url_id(url):
    sanitized_url = url.lower()
    sanitized_url = sanitized_url.replace('/', '_')
    return 'web/%s' % sanitized_url


def wikipedia_id(wptitle):
    title = wptitle.lower()
    title = title.replace(' ', '_')
    return 'wikipedia/%s' % title


def gb_id(name, crawler):
    sanitized_name = name.lower()
    sanitized_name = sanitized_name.replace(' ', '_')
    return 'gb/%s/%s' % (crawler, sanitized_name)


def encode_id(in_id):
    return urllib.quote(in_id.encode('utf8'), '')
    #out_id = in_id.replace('$', '$$')
    #out_id = out_id.replace('/', '$!')
    #return out_id


def decode_id(in_id):
    return urllib.unquote(in_id).decode('utf8')
    #out_id = u''

    #escaped = False

    #for c in in_id:
    #    if escaped:
    #        if c == '$':
    #            out_id += '$'
    #        else:
    #            out_id += '/'
    #        escaped = False
    #    else:
    #        if c == '$':
    #            escaped = True
    #        else:
    #            out_id += c

    #return out_id