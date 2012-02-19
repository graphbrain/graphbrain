# -*- coding: utf-8 -*-


from node import Node
from ids import encode_id
from utils import force_utf8

import requests


def index_node(node):
    eid = encode_id(node.d['_id'])
    payload = force_utf8('{"label": "%s"}' % node.d['label'])
    r = requests.post(u"http://localhost:9200/gb/node/%s" % eid, data=payload)
    
    code = str(r.status_code)
    
    # Successful if status code == 2xx
    return (len(code) == 3) and (code[0] == '2')


def index():
    node = Node()

    count = 0
    scount = 0

    for n in node.getall():
        if 'label' in n.d:
            if (isinstance(n.d['_id'], unicode) or isinstance(n.d['_id'], str)) and (n.d['type'] == 'text'):
                count += 1
                r = index_node(n)
                status = 'FAILED'
                if r:
                    status = 'success'
                    scount += 1

                print '[%d] indexing %s -> %s' % (count, n.d['_id'], status)

    print '%d index operations attempted, %d succesfull' % (count, scount)
    print 'Done.'