import MySQLdb as mdb

import db
from dbobj import DbObj
from link import Link


class Node(DbObj):
    def __init__(self):
        DbObj.__init__(self)

    def create(self, data, graph):
        self.data = data
        self.graph = graph

        self.cur.execute("INSERT INTO node (data, graph) VALUES (%s, %s)", (data, graph.id))
        self.id = self.insert_id()
        self.commit()

        return self

    def get_by_id(self, node_id):
        self.id = node_id
        self.cur.execute("SELECT data, graph FROM node WHERE id=%s", (node_id,))
        row = self.cur.fetchone()
        self.data = row[0]
        self.graph = row[1]

        return self

    def get_by_data(self, data, graph):
        self.cur.execute("SELECT id FROM node WHERE graph=%s AND data=%s", (graph.id, data))
        row = self.cur.fetchone()
        if row is None:
            self.id = -1
            self.data = ''
        else:
            self.id = row[0]
            self.data = data

        return self

    def _neighbors(self, nodes, depth=0):
        if self.id not in nodes.keys():
            nodes[self.id] = self

        if (depth < 2):
            self.cur.execute("SELECT targ FROM link WHERE orig=%s", (self.id,))
            rows = self.cur.fetchall()
            for row in rows:
                nnode = Node().get_by_id(row[0])
                nnode.parent = self.id
                nnode._neighbors(nodes, depth + 1)

            self.cur.execute("SELECT orig FROM link WHERE targ=%s", (self.id,))
            rows = self.cur.fetchall()
            for row in rows:
                nnode = Node().get_by_id(row[0])
                nnode.parent = self.id
                nnode._neighbors(nodes, depth + 1)

    def _internal_links(self, nodes):
        ilinks = []

        for node_id in nodes.keys():
            self.cur.execute("SELECT id FROM link WHERE orig=%s", (node_id,))
            rows = self.cur.fetchall()
            for row in rows:
                link = Link().get_by_id(row[0])
                if link.targ in nodes.keys():
                    ilinks.append(link)

        return ilinks

    def neighbours_json(self):
        self.parent = ''
        nodes = {}
        self._neighbors(nodes)
        links = self._internal_links(nodes)

        nodes_json = '['
        for node_id in nodes.keys():
            node = nodes[node_id]
            nodes_json = '%s{"id": "%d", "parent":"%s", "text": "%s"},' % (nodes_json, node.id, node.parent, node.data)
        nodes_json = '%s]' % nodes_json

        links_json = '['
        for link in links:
            links_json = '%s{"id":"%s", "orig": "%s", "targ":"%s", "type":"%s", "dir":"%s"},' % (
                         links_json, link.id, link.orig, link.targ, link.relation, link.directed)
        links_json = '%s]' % links_json

        return nodes_json, links_json
