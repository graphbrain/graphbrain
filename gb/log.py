# -*- coding: utf-8 -*-

import MySQLdb as mdb

import db


def log(msg, color, user_id, ip_addr):
    cur = db.cursor()
    safe_msg = msg[:1000]
    cur.execute("INSERT INTO log (msg, user, ip_addr, color) VALUES (%s, %s, %s, %s)", (safe_msg, user_id, ip_addr, color))
    db.connection().commit()
    cur.close()


def get_logs():
    logs = []
    cur = db.cursor()
    cur2 = db.cursor()
    cur.execute("SELECT msg, user, ip_addr, color, ts FROM log ORDER BY id DESC")
    for row in cur:
	email = 'none'
        if row[1] > 0:
            cur2.execute("SELECT email FROM user WHERE id=%s", (row[1],))
            user = cur2.fetchone()[0]
	logs.append({'msg': row[0], 'user': user, 'ip_addr': row[2], 'color': row[3], 'ts': row[4]})
    cur.close()
    cur2.close()
    return logs
