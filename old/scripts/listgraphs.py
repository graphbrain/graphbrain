#!/usr/bin/env python


import gb.mongodb
from gb.user import User


if __name__ == '__main__':
    db = gb.mongodb.getdb()

    cur = db.nodes.find({'type': 'graph'})
    for g in cur:
        owner = User().get_by_id(g['owner']).d['email']
        print g['_id'], g['label'], owner
