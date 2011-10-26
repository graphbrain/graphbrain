#!/usr/bin/env python
# -*- coding: utf-8 -*-


import gb.db as db
from pymongo import Connection


if __name__ == '__main__':
    mconn = Connection()
    mdb = mconn.gb

    # process users
    users = {}
    user_emails = {}
    mnodes = mdb.nodes
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

        if role == 0:
            role = 'user'
        else:
            role = 'admin'

        mid = mnodes.insert({'label': name, 'type': 'user', 'graph': 'users', 'creation_ts': creation_ts, 'email': email, 'role': role, 'pwdhash': pwdhash, 'session': session, 'session_ts': session_ts, 'graph_perms': []})
        users[mysql_id] = mid
        user_emails[mysql_id] = email

    # process graphs
    graphs = {}
    graph_roots = {}
    cur = db.execute("SELECT id, creation_ts, name, owner, root FROM graph")
    for row in cur:
        mysql_id = row[0]
        creation_ts = row[1]
        name = row[2]
        owner = row[3]
        root = row[4]

        if root is None:
            continue

        if owner in users:
            owner = users[owner]
        else:
            owner = -1

        mid = mnodes.insert({'label': name, 'type': 'graph', 'graph': 'graphs', 'creation_ts': creation_ts, 'owner': owner})
        graphs[mysql_id] = mid
        graph_roots[mid] = root

    # process nodes
    nodes = {}
    cur = db.execute("SELECT id, creation_ts, data, graph, node_type FROM node")
    for row in cur:
        mysql_id = row[0]
        creation_ts = row[1]
        data = unicode(row[2])
        graph = row[3]
        node_type = row[4]

        if node_type == 0:
            node_type = 'text'
        else:
            node_type = 'image'

        if graph in graphs.keys():
            mid = mnodes.insert({'label': data, 'type': node_type, 'graph': graphs[graph], 'creation_ts': creation_ts, 'origs': [], 'targs': {}})
            nodes[mysql_id] = mid
   
    # update roots
    for gid in graph_roots.keys():
        mnodes.update({'_id': gid}, {'$set': {'root': nodes[graph_roots[gid]]}})
   
    # process graph permissions
    cur = db.execute("SELECT id, creation_ts, graph, user, perm FROM graph_user")
    for row in cur:
        mysql_id = row[0]
        creation_ts = row[1]
        graph = row[2]
        user = row[3]
        perm = row[4]

        if perm == 0:
            perm = 'admin'
        elif perm == 1:
            perm = 'editor'
        else:
            perm = 'reader'

        mnodes.update({'_id': users[user]}, {'$addToSet': {'graph_perms': {'graph': graphs[graph], 'perm': perm, 'creation_ts': creation_ts}}})

    # process links
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

        if directed == 1:
            directed = True
        else:
            directed = False

        mnodes.update({'_id': nodes[orig]}, {'$addToSet': {'targs.%s' % nodes[targ]: {'relation': relation, 'relation_raw': relation_raw, 'sentence': sentence, 'directed': directed}}})
        mnodes.update({'_id': nodes[targ]}, {'$addToSet': {'origs': nodes[orig]}})

    # process logs
    mlogs = mdb.logs
    cur = db.execute("SELECT id, msg, ts, user, ip_addr, color FROM log")
    for row in cur:
        mysql_id = row[0]
        msg = row[1]
        ts = row[2]
        user = row[3]
        ip_addr = row[4]
        color = row[5]

        mlogs.insert({'msg': msg, 'user': user_emails[user], 'ts': ts, 'ip_addr': ip_addr, 'color': color})

    db.connection().close()
