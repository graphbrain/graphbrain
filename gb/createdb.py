#!/usr/bin/env python


import db


if __name__ == '__main__':

    # user table
    db.execute("CREATE TABLE user(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.execute("ALTER TABLE user ADD COLUMN name VARCHAR(100)")
    db.execute("ALTER TABLE user ADD COLUMN email VARCHAR(100)")
    db.execute("ALTER TABLE user ADD COLUMN creation_ts INT")
    db.execute("ALTER TABLE user ADD COLUMN pwdhash VARCHAR(60)")
    db.execute("ALTER TABLE user ADD COLUMN session VARCHAR(60)")
    db.execute("ALTER TABLE user ADD COLUMN session_ts INT")

    # graph table
    db.execute("CREATE TABLE graph(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.execute("ALTER TABLE graph ADD COLUMN owner INT")
    db.execute("ALTER TABLE graph ADD COLUMN root INT")
    
    # node table
    db.execute("CREATE TABLE node(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.execute("ALTER TABLE node ADD COLUMN data VARCHAR(250)")
    db.execute("ALTER TABLE node ADD COLUMN graph INT")

    # link table
    db.execute("CREATE TABLE link(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.execute("ALTER TABLE link ADD COLUMN orig INT")
    db.execute("ALTER TABLE link ADD COLUMN targ INT")
    db.execute("ALTER TABLE link ADD COLUMN relation VARCHAR(100)")

    db.connection().close()
