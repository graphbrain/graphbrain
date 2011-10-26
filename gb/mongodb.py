# -*- coding: utf-8 -*-


from pymongo import Connection


_gbdb = None


def getdb():
    global _gbdb

    if _gbdb is None:
        mconn = Connection()
        _gbdb = mconn.gb

    return _gbdb
