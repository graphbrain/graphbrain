import MySQLdb as mdb


__conn = None


def __connect():
    global __conn

    try:
        if __conn is None:
            __conn = mdb.connect('localhost', 'root', '', 'graphbrain');
    except mdb.Error, e:
        print "Error %d: %s" % (e.args[0],e.args[1])


def cursor():
    global __conn
    __connect()
    return __conn.cursor()
    
    
def connection():
    global __conn
    __connect()
    return __conn


def execute(query):
    global __conn
    __connect()
    cur = cursor()
   
    try:
        cur.execute(query)
        connection().commit()
    except mdb.Error, e:
        print "Error %d: %s" % (e.args[0],e.args[1])
    cur.close()
