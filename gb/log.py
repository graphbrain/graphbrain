# -*- coding: utf-8 -*-


from datetime import datetime
import mongodb


def log(msg, color, email, ip_addr):
    db = mongodb.getdb()
    mlogs = db.logs
    safe_msg = msg[:1000]
    mlogs.insert({'msg': safe_msg, 'user': email, 'ts': datetime.now(), 'ip_addr': ip_addr, 'color': color})


def get_logs():
    db = mongodb.getdb()
    mlogs = db.logs
    cur = mlogs.find(sort=[('ts', -1),])
    return cur
