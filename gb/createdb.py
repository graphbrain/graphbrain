#!/usr/bin/env python


import db


if __name__ == '__main__':

    # user table
    db.execute("CREATE TABLE user(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.execute("ALTER TABLE user ADD COLUMN name VARCHAR(50)")
    
    # node table

    # link table

    db.connection().close()
