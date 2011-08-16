import MySQLdb as mdb

import db
from dbobj import DbObj


class Link(DbObj):
    def __init__(self):
        DbObj.__init__(self)

    def create(self, orig, targ, relation):
        self.orig = orig
        self.targ = targ
        self.relation = relation

        self.cur.execute("INSERT INTO link (orig, targ, relation) VALUES (%s, %s, %s)", (orig.id, targ.id, relation))
        self.id = self.insert_id()
        self.commit()

        return self
