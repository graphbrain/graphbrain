#!/usr/bin/env python
# -*- coding: utf-8 -*-


from node import Node
from ids import encode_id
from utils import force_utf8

import requests

import sys
import json


def query(term):
    payload = force_utf8('{"query":{"text":{"label":"%s"}}}' % term)
    r = requests.post('http://localhost:9200/gb/node/_search', data=payload)
    
    result = json.loads(r.content)
    return result


def first_hit(term):
    query_result = query(term)
    count = int(query_result['hits']['total'])
    if count == 0:
        return ''
    else:
        return query_result['hits']['hits'][0]['_id']


if __name__ == '__main__':
    print first_hit(sys.argv[1])