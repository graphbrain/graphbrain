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


def decode_id(in_id):
    return urllib.unquote(in_id).decode('utf8')