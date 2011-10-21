#!/usr/bin/env python
# -*- coding: utf-8 -*-


import gb.db as db
from pymongo import Connection


if __name__ == '__main__':
    mconn = Connection()
    mdb = mconn.gb

    users = {}
    musers = mdb.users
    cur = db.execute("SELECT id, creation_ts, name, email, pwdhash, role, session, session_ts FROM user")
    for row in cur:
        mysql_id = row[0]
        creation_ts = row[1]
        name = row[2]
        email = row[3]
        pwdhash = row[4]
        role = row[5]
        session = row[6]
        session_ts = row[7]

        mid = musers.insert({'name': name, 'email': email, 'creation_ts': creation_ts, 'role': role, 'pwdhash': pwdhash, 'session': session, 'session_ts': session_ts, 'graph_perms': []})
        users[mysql_id] = mid

    nodes = {}
    mnodes = mdb.nodes
    cur = db.execute("SELECT id, creation_ts, data, graph, node_type FROM node")
    for row in cur:
        mysql_id = row[0]
        creation_ts = row[1]
        data = unicode(row[2])
        graph = row[3]
        node_type = row[4]

        mid = mnodes.insert({'data': data, 'graph': graph, 'creation_ts': creation_ts, 'node_type': node_type, 'origs': [], 'targs': []})
        nodes[mysql_id] = mid
    
    graphs = {}
    mgraphs = mdb.graphs
    cur = db.execute("SELECT id, creation_ts, name, owner, root FROM graph")
    for row in cur:
        mysql_id = row[0]
        creation_ts = row[1]
        name = row[2]
        owner = row[3]
        root = row[4]

        if owner in users:
            owner = users[owner]
        else:
            owner = -1
   
        if root in nodes:
            root = nodes[root]
        else:
            root = -1

        mid = mgraphs.insert({'name': name, 'owner': owner, 'creation_ts': creation_ts, 'root': root})
        graphs[mysql_id] = mid

    cur = db.execute("SELECT id, creation_ts, graph, user, perm FROM graph_user")
    for row in cur:
        mysql_id = row[0]
        creation_ts = row[1]
        graph = row[2]
        user = row[3]
        perm = row[4]

        musers.update({'_id': users[user]}, {'$addToSet': {'graph_perms': {'graph': graphs[graph], 'perm': perm, 'creation_ts': creation_ts}}})

    cur = db.execute("SELECT id, creation_ts, orig, targ, relation, relation_raw, sentence, directed FROM link")
    for row in cur:
        mysql_id = row[0]
        creation_ts = row[1]
        orig = row[2]
        targ = row[3]
        relation = row[4]
        relation_raw = row[5]
        sentence = row[6]
        directed = row[7]

        mnodes.update({'_id': nodes[orig]}, {'$addToSet': {'targs': {'targ': nodes[targ], 'relation': relation, 'relation_raw': relation_raw, 'sentence': sentence, 'directed': directed}}})
        mnodes.update({'_id': nodes[targ]}, {'$addToSet': {'origs': {'orig': nodes[orig]}}})

    mlogs = mdb.logs
    cur = db.execute("SELECT id, msg, ts, user, ip_addr, color FROM log")
    for row in cur:
        mysql_id = row[0]
        msg = row[1]
        ts = row[2]
        user = row[3]
        ip_addr = row[4]
        color = row[5]

        mlogs.insert({'msg': name, 'user': users[user], 'ts': ts, 'ip_addr': ip_addr, 'color': color})

    db.connection().close()
