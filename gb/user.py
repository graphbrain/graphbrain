# -*- coding: utf-8 -*-


import bcrypt
from datetime import datetime

from dbobj import DbObj
from utils import random_string, timestamp
from graph import Graph


class User(DbObj):
    def __init__(self):
        DbObj.__init__(self)
        self.collection = 'nodes'

    def create(self, email, name, password, role):
        self.d['label'] = name
        self.d['type'] = 'user'
        self.d['graph'] = 'users'
        self.d['creation_ts'] = datetime.now()
        self.d['email'] = email
        self.d['role'] = role
        self.d['pwdhash'] = bcrypt.hashpw(password, bcrypt.gensalt(12))
        self.d['sesion'] = 'none'
        self.d['sesion_ts'] = None
        self.d['graph_perms'] = []

        self._insert()

        return self

    def get_by_email(self, email):
        self.d = self.db.nodes.find_one({'type': 'user', 'email': email})
        return self
        
    def check_password(self, password):
        if self.d is None:
            return False
        if self.d['pwdhash'] == bcrypt.hashpw(password, self.d['pwdhash']):
            return True
        else:
            return False

    def create_session(self):
        self._set_field('session', random_string(60))
        return self.d['session']

    def check_session(self, session_str):
        if self.d is None:
            return False
        if session_str == self.d['session']:
            return True
        else:
            return False

    def logout(self):
        self._set_field('session', 'none')

    def graph_list_for_user(self, u):
        graphs = []
        for g in self.d['graph_perms']:
            graphs.append(Graph().get_by_id(g['graph']).d)

        return graphs

    def set_permission(self, graph, perm):
        graph_id = graph.d['_id']
        update = False
        for gp in self.d['graph_perms']:
            if gp['graph'] == graph_id:
                if gp['perm'] != perm:
                    gp['perm'] = perm
                    update = True
                break

        if update:
            self._update_field('graph_perms')
        else:
            self.db.nodes.update({'_id': self.d['_id']}, {'$addToSet': {'graph_perms': {'graph': graph_id, 'perm': perm, 'creation_ts': datetime.now()}}})
