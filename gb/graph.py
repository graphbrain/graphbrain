import MySQLdb as mdb

import db
from dbobj import DbObj
from node import Node
from link import Link


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
        self.cur.execute("UPDATE graph SET root=%s WHERE id=%s", (root.id, self.id))
        self.commit()

    def get_by_owner_and_name(self, owner, name):
        self.cur.execute("SELECT id, root FROM graph WHERE owner=%s AND name=%s", (owner.id, name))
        row = self.cur.fetchone()
        self.id = row[0]
        self.root = Node().get_by_id(row[1])

        return self

    def add_rel(self, orig_str, rel, targ_str):
        orig = Node().get_by_data(orig_str, self)
        targ = Node().get_by_data(targ_str, self)

        # one of the nodes has to exist in the graph
        if (orig.id < 0) and (targ.id < 0):
            return False

        # create one of the nodes if it does not exist
        if orig.id < 0:
            orig.create(orig_str, self)
        elif targ.id < 0:
            targ.create(targ_str, self)

        # create new link between nodes
        Link().create(orig, targ, rel)

        return True
