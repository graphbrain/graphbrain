#!/usr/bin/env python


import gb.db as db
from gb.user import User


if __name__ == '__main__':
    cur = db.cursor()

    cur.execute("SELECT id, name, owner FROM graph")
    for row in cur:
        owner = User().get_by_id(row[2]).email
        print row[0], row[1], owner

    cur.close()
