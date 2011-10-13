# -*- coding: utf-8 -*-

import MySQLdb as mdb

import db
from dbobj import DbObj


class Link(DbObj):
    def __init__(self):
        DbObj.__init__(self)

    def create(self, orig, targ, rel, rel_raw, sentence='', directed=1):
        self.orig = orig
        self.targ = targ
        self.relation = rel
        self.relation_raw = rel_raw
        self.sentence = sentence
        self.directed = directed

        self.execute("INSERT INTO link (orig, targ, relation, relation_raw, sentence, directed) VALUES (%s, %s, %s, %s, %s, %s)",
                         (orig.id, targ.id, self.relation, self.relation_raw, self.sentence, self.directed))
        self.id = self.insert_id()
        self.commit()

        return self

    def get_by_id(self, link_id):
        self.id = link_id
        self.execute("SELECT orig, targ, relation, directed FROM link WHERE id=%s", (link_id,))
        row = self.cur.fetchone()
        self.orig = row[0]
        self.targ = row[1]
        self.relation = row[2]
        self.directed = row[3]

        return self
        
    def delete(self):
        self.execute("DELETE FROM link WHERE id=%s", (self.id,))
        self.commit()
