import MySQLdb as mdb

import db


class Node:
    def __init__(self):
        pass

    def create(self, data, graph):
        self.name = name
        self.owner = owner

        cur = db.cursor()

        cur.execute("INSERT INTO node (data, graph) VALUES (%s, %s)", (data, graph.id))

        self.id = db.connection().insert_id

        db.connection().commit()
        cur.close()

        return self
