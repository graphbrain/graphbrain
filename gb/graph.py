import MySQLdb as mdb
from dbobj import DbObj
import db
from node import Node
from link import Link


class Graph(DbObj):
    def __init__(self):
        DbObj.__init__(self)

    def create(self, name, owner):
        self.name = name
        self.owner = owner

        self.execute("INSERT INTO graph (name, owner) VALUES (%s, %s)", (name, owner.id))
        self.id = self.insert_id()
        self.commit()

        return self

    def set_root(self, root):
        self.execute("UPDATE graph SET root=%s WHERE id=%s", (root.id, self.id))
        self.commit()

    def get_by_owner_and_name(self, owner, name):
        self.execute("SELECT id, root FROM graph WHERE owner=%s AND name=%s", (owner.id, name))
        row = self.cur.fetchone()
        self.id = row[0]
        self.root = Node().get_by_id(row[1])

        return self

    def get_by_id(self, graph_id):
        self.execute("SELECT name, root FROM graph WHERE id=%s", (graph_id,))
        row = self.cur.fetchone()
        self.id = graph_id
        self.name = row[0]
        self.root = Node().get_by_id(row[1])

        return self

    def add_rel(self, r):
        orig = Node().get_by_data(r['orig'], self)
        targ = Node().get_by_data(r['targ'], self)

        # one of the nodes has to exist in the graph
        if (orig.id < 0) and (targ.id < 0):
            return -1

        # create one of the nodes if it does not exist
        if orig.id < 0:
            orig.create(r['orig'], self, r['orig_type'])
        elif targ.id < 0:
            targ.create(r['targ'], self, r['targ_type'])

        # create new link between nodes
        Link().create(orig, targ, r['rel'], r['rel_raw'], r['sentence'])

        return orig.id

    def graph_list_for_user(self, u):
        graphs = []

        self.execute("SELECT graph FROM graph_user WHERE user=%s", (u.id,))
        for row in self.cur:
            cur2 = db.execute("SELECT id, root, name FROM graph WHERE id=%s", (row[0],))
            for row2 in cur2:
                graphs.append({'id':row2[0], 'root':row2[1], 'name':row2[2]})
            cur2.close()
        return graphs

    def set_permission(self, u, perm):
        self.execute("SELECT id, perm FROM graph_user WHERE user=%s AND graph=%s", (u.id, self.id))
        row = self.cur.fetchone()
        if row is None:
            self.execute("INSERT INTO graph_user (graph, user, perm) VALUES (%s, %s, %s)", (self.id, u.id, perm))
        else:
            if perm != row[1]:
                self.execute("UPDATE graph_user SET perm=%s WHERE id=%s", (perm, row[0]))
        self.commit()
