import MySQLdb as mdb
import bcrypt

import db


class User:
    def __init__(self):
        pass

    def create(self, email, name, password, role):
        self.email = email
        self.name = name
        self.role = role
        self.pwdhash = bcrypt.hashpw(password, bcrypt.gensalt(12))

        cur = db.cursor()

        cur.execute("INSERT INTO user (name, email, pwdhash, role) VALUES (%s, %s, %s, %s)",
                        (name, email, self.pwdhash, role))

        self.id = db.connection().insert_id

        db.connection().commit()
        cur.close()

        return self.id

    def get_by_email(email):
        cur = db.cursor
        cur.excute("SELECT id, name, pwdhash, role, creation_ts, session, session_ts FROM user WHERE email=%s", (email,))
        row = cur.fetchone()
        self.id = row[0]
        self.name = row[1]
        self.pwdhash = row[2]
        self.role = row[3]
        self.creation_ts = row[4]
        self.session = row[5]
        self.session_ts = row[6]
