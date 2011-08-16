import MySQLdb as mdb

import db


class Graph:
    def __init__(self):
        pass

    def create(self, name, owner):
        self.name = name
        self.owner = owner

        cur = db.cursor()

        cur.execute("INSERT INTO graph (name, owner) VALUES (%s, %s)", (name, owner))

        self.id = db.connection().insert_id

        db.connection().commit()
        cur.close()

        return self.id
