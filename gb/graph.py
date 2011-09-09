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

    def add_rel(self, r):
        orig = Node().get_by_data(r['orig'], self)
        targ = Node().get_by_data(r['targ'], self)

        # one of the nodes has to exist in the graph
        if (orig.id < 0) and (targ.id < 0):
            return False

        # create one of the nodes if it does not exist
        if orig.id < 0:
            orig.create(r['orig'], self)
        elif targ.id < 0:
            targ.create(r['targ'], self)

        # create new link between nodes
        Link().create(orig, targ, r['rel'], r['rel_raw'], r['sentence'])

        return True

    def graph_list_for_user(self, u):
        graphs = []

        # TODO: only graphs that user has access to
        self.cur.execute("SELECT root, name FROM graph")
        for row in self.cur:
            graphs.append({'root':row[0], 'name':row[1]})
        return graphs

    def set_permission(self, u, perm):
        self.cur.execute("SELECT id, perm FROM graph_user WHERE user=%s AND graph=%s", (u.id, self.id))
        row = self.cur.fetchone()
        if row is None:
            self.cur.execute("INSERT INTO graph_user (graph, user, perm) VALUES (%s, %s, %s)", (self.id, u.id, perm))
        else:
            if perm != row[1]:
                self.cur.execute("UPDATE graph_user SET perm=%s WHERE id=%s", (perm, row[0]))
