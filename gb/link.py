import MySQLdb as mdb

import db


class Link:
    def __init__(self):
        pass

    def create(self, orig, targ, relation):
        self.orig = orig
        self.targ = targ
        self.relation = relation

        cur = db.cursor()

        cur.execute("INSERT INTO link (orig, targ, relation) VALUES (%s, %s, %s)", (orig.id, targ.id, relation))

        self.id = db.connection().insert_id

        db.connection().commit()
        cur.close()

        return self
