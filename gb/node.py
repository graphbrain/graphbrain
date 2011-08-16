import MySQLdb as mdb

import db
from dbobj import DbObj


class Node(DbObj):
    def __init__(self):
        DbObj.__init__(self)

    def create(self, data, graph):
        self.name = name
        self.owner = owner

        self.cur.execute("INSERT INTO node (data, graph) VALUES (%s, %s)", (data, graph.id))
        self.id = self.insert_id()
        self.commit()

        return self
