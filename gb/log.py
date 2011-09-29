import MySQLdb as mdb

import db


def log(msg, color, user_id, ip_addr):
    cur = db.cursor()
    safe_msg = msg[:1000]
    cur.execute("INSERT INTO log (msg, user, ip_addr, color) VALUES (%s, %s, %s, %s)", (safe_msg, user_id, ip_addr, color))
    db.connection().commit()
    cur.close()
