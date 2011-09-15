import MySQLdb as mdb
from config import *


_conn = None


def _connect(reconnect=False):
    global _conn

    if reconnect:
        _conn = None

    try:
        if _conn is None:
            _conn = mdb.connect('localhost', DB_USER, DB_PASSWORD, DB_NAME);
    except mdb.Error, e:
        print "Error %d: %s" % (e.args[0],e.args[1])


def cursor(reconnect=False):
    global _conn
    _connect(reconnect)
    return _conn.cursor()
    
    
def connection():
    global _conn
    _connect()
    return _conn


def commit():
    connection().commit()


def execute(query, params=(), cur=None):
    c = cur

    try:
        if c is None:
            c = cursor()
        c.execute(query, params)
    # deal with "MySQL server is gone" errors
    except (AttributeError, mdb.OperationalError): 
        c = cursor(reconnect=True)
        c.execute(query, params)
    except mdb.Error, e:
        print "Error %d: %s" % (e.args[0],e.args[1])

    return c


# Used only for createdb type stuff - NOT for production system
def safe_execute(query, params=()):
    c = None

    try:
        c = cursor()
        c.execute(query, params)
    except mdb.Error, e:
        print "Error %d: %s" % (e.args[0],e.args[1])

    commit()
    c.close()


def insert_id():
    return connection().insert_id()
