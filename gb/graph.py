import MySQLdb as mdb

import db
from dbobj import DbObj


class Graph(DbObj):
    def __init__(self):
        DbObj.__init__(self)

    def create(self, name, owner):
        self.name = name
        self.owner = owner

        self.cur.execute("INSERT INTO graph (name, owner) VALUES (%s, %s)", (name, owner.id))
        self.id = self.insert_id()
        self.commit()

        return self

    def set_root(self, root):
        pass
