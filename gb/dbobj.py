import MySQLdb as mdb

import db


class DbObj:
    def __init__(self):
        self.cur = db.cursor()

    def __del__(self):
        self.cur.close()

    def commit(self):
        db.connection.commit()

    def insert_id(self):
        return db.connection().insert_id
