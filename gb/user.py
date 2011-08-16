import MySQLdb as mdb
import bcrypt

import db


class User:
    def __init__(self):
        pass

    def create(email, name, password, role):
        self.email = email
        self.name = name
        self.role = role
        self.pwdhash = bcrypt.hashpw(password, bcrypt.gensalt(12))

        cur = db.cursor()

        cur.execute("INSERT INTO user (name, email, pwdhash, role) VALUES (%s, %s, %s, %d)",
                        (name, email, self.pwdhash, role))

        self.id = db.connection().insert_id

        db.connection().commit()
        cur.close()

        return self.id
