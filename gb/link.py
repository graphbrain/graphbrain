import MySQLdb as mdb

import db
from dbobj import DbObj


class Link(DbObj):
    def __init__(self):
        DbObj.__init__(self)

    def create(self, orig, targ, rel, rel_raw, sentence):
        self.orig = orig
        self.targ = targ
        self.relation = rel
        self.relation_raw = rel_raw
        self.sentence = sentence

        self.cur.execute("INSERT INTO link (orig, targ, relation, relation_raw, sentence) VALUES (%s, %s, %s, %s, %s)",
                         (orig.id, targ.id, self.relation, self.relation_raw, self.sentence))
        self.id = self.insert_id()
        self.commit()

        return self

    def get_by_id(self, link_id):
        self.id = link_id
        self.cur.execute("SELECT orig, targ, relation FROM link WHERE id=%s", (link_id,))
        row = self.cur.fetchone()
        self.orig = row[0]
        self.targ = row[1]
        self.relation = row[2]

        return self
