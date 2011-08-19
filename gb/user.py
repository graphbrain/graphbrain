import MySQLdb as mdb
import bcrypt

import db
from dbobj import DbObj
from utils import random_string, timestamp


class User(DbObj):
    def __init__(self):
        DbObj.__init__(self)

    def create(self, email, name, password, role):
        self.email = email
        self.name = name
        self.role = role
        self.pwdhash = bcrypt.hashpw(password, bcrypt.gensalt(12))

        self.cur.execute("INSERT INTO user (name, email, pwdhash, role) VALUES (%s, %s, %s, %s)",
                        (name, email, self.pwdhash, role))
        self.id = self.insert_id()
        self.commit()

        return self

    def get_by_id(self, user_id):
        self.cur.execute("SELECT email, name, pwdhash, role, creation_ts, session, session_ts FROM user WHERE id=%s", (user_id,))
        row = self.cur.fetchone()
        self.id = user_id
        self.email = row[0]
        self.name = row[1]
        self.pwdhash = row[2]
        self.role = row[3]
        self.creation_ts = row[4]
        self.session = row[5]
        self.session_ts = row[6]

        return self

    def get_by_email(self, email):
        self.cur.execute("SELECT id, name, pwdhash, role, creation_ts, session, session_ts FROM user WHERE email=%s", (email,))
        row = self.cur.fetchone()
        self.email = email
        self.id = row[0]
        self.name = row[1]
        self.pwdhash = row[2]
        self.role = row[3]
        self.creation_ts = row[4]
        self.session = row[5]
        self.session_ts = row[6]

        return self

    def check_password(self, password):
        if self.pwdhash == bcrypt.hashpw(password, self.pwdhash):
            return True
        else:
            return False

    def create_session(self):
        session_str = random_string(60)
        self.cur.execute("UPDATE user SET session=%s, session_ts=%s WHERE id=%s", (session_str, timestamp(), self.id))
        self.commit()
        return session_str

    def check_session(self, session_str):
        if session_str == self.session:
            return True
        else:
            return False

    def logout(self):
        self.cur.execute("UPDATE user SET session='none' WHERE id=%s", (self.id, ))
        self.commit()
