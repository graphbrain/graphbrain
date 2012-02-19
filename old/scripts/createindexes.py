#!/usr/bin/env python
# -*- coding: utf-8 -*-


import pymongo
from gb.mongodb import getdb


if __name__ == '__main__':
    db = getdb()
    db.nodes.ensure_index([('eid', pymongo.ASCENDING), ('graph', pymongo.ASCENDING)])
    db.nodes.ensure_index([('label', pymongo.ASCENDING), ('graph', pymongo.ASCENDING)])