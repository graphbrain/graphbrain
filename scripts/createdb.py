#!/usr/bin/env python


import gb.db as db


if __name__ == '__main__':
    # user table
    db.execute("CREATE TABLE user(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.execute("ALTER TABLE user ADD COLUMN creation_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.execute("ALTER TABLE user ADD COLUMN name VARCHAR(100)")
    db.execute("ALTER TABLE user ADD COLUMN email VARCHAR(100)")
    db.execute("ALTER TABLE user ADD COLUMN pwdhash VARCHAR(60)")
    db.execute("ALTER TABLE user ADD COLUMN role INT")
    # 0: user; 1: admin
    db.execute("ALTER TABLE user ADD COLUMN session VARCHAR(60) DEFAULT 'none'")
    db.execute("ALTER TABLE user ADD COLUMN session_ts INT DEFAULT -1")

    # graph table
    db.execute("CREATE TABLE graph(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.execute("ALTER TABLE graph ADD COLUMN creation_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.execute("ALTER TABLE graph ADD COLUMN name VARCHAR(100)")
    db.execute("ALTER TABLE graph ADD COLUMN owner INT")
    db.execute("ALTER TABLE graph ADD COLUMN root INT")

    # graph_user table
    db.execute("CREATE TABLE graph_user(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.execute("ALTER TABLE graph_user ADD COLUMN creation_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.execute("ALTER TABLE graph_user ADD COLUMN graph INT")
    db.execute("ALTER TABLE graph_user ADD COLUMN user INT")
    db.execute("ALTER TABLE graph_user ADD COLUMN perm INT")
    # 0: admin; 1: editor; 2: reader
    
    # node table
    db.execute("CREATE TABLE node(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.execute("ALTER TABLE node ADD COLUMN creation_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.execute("ALTER TABLE node ADD COLUMN data VARCHAR(250)")
    db.execute("ALTER TABLE node ADD COLUMN graph INT")

    # link table
    db.execute("CREATE TABLE link(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.execute("ALTER TABLE link ADD COLUMN creation_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.execute("ALTER TABLE link ADD COLUMN orig INT")
    db.execute("ALTER TABLE link ADD COLUMN targ INT")
    db.execute("ALTER TABLE link ADD COLUMN relation VARCHAR(100)")
    db.execute("ALTER TABLE link ADD COLUMN relation_raw VARCHAR(100)")
    db.execute("ALTER TABLE link ADD COLUMN sentence VARCHAR(500)")
    db.execute("ALTER TABLE link ADD COLUMN directed INT DEFAULT 1")

    db.commit()
    db.connection().close()
