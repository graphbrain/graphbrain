#!/usr/bin/env python


import gb.db as db


if __name__ == '__main__':
    # user table
    db.safe_execute("CREATE TABLE user(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.safe_execute("ALTER TABLE user ADD COLUMN creation_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.safe_execute("ALTER TABLE user ADD COLUMN name VARCHAR(100)")
    db.safe_execute("ALTER TABLE user ADD COLUMN email VARCHAR(100)")
    db.safe_execute("ALTER TABLE user ADD COLUMN pwdhash VARCHAR(60)")
    db.safe_execute("ALTER TABLE user ADD COLUMN role INT")
    # 0: user; 1: admin
    db.safe_execute("ALTER TABLE user ADD COLUMN session VARCHAR(60) DEFAULT 'none'")
    db.safe_execute("ALTER TABLE user ADD COLUMN session_ts INT DEFAULT -1")

    # graph table
    db.safe_execute("CREATE TABLE graph(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.safe_execute("ALTER TABLE graph ADD COLUMN creation_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.safe_execute("ALTER TABLE graph ADD COLUMN name VARCHAR(100)")
    db.safe_execute("ALTER TABLE graph ADD COLUMN owner INT")
    db.safe_execute("ALTER TABLE graph ADD COLUMN root INT")

    # graph_user table
    db.safe_execute("CREATE TABLE graph_user(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.safe_execute("ALTER TABLE graph_user ADD COLUMN creation_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.safe_execute("ALTER TABLE graph_user ADD COLUMN graph INT")
    db.safe_execute("ALTER TABLE graph_user ADD COLUMN user INT")
    db.safe_execute("ALTER TABLE graph_user ADD COLUMN perm INT")
    # 0: admin; 1: editor; 2: reader
    
    # node table
    db.safe_execute("CREATE TABLE node(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.safe_execute("ALTER TABLE node ADD COLUMN creation_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.safe_execute("ALTER TABLE node ADD COLUMN data VARCHAR(250)")
    db.safe_execute("ALTER TABLE node ADD COLUMN graph INT")
    db.safe_execute("ALTER TABLE node ADD COLUMN node_type INT DEFAULT 0")
    # 0: text; 1: image

    # link table
    db.safe_execute("CREATE TABLE link(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.safe_execute("ALTER TABLE link ADD COLUMN creation_ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.safe_execute("ALTER TABLE link ADD COLUMN orig INT")
    db.safe_execute("ALTER TABLE link ADD COLUMN targ INT")
    db.safe_execute("ALTER TABLE link ADD COLUMN relation VARCHAR(100)")
    db.safe_execute("ALTER TABLE link ADD COLUMN relation_raw VARCHAR(100)")
    db.safe_execute("ALTER TABLE link ADD COLUMN sentence VARCHAR(500)")
    db.safe_execute("ALTER TABLE link ADD COLUMN directed INT DEFAULT 1")

    # log table
    db.safe_execute("CREATE TABLE log(id INT PRIMARY KEY AUTO_INCREMENT)")
    db.safe_execute("ALTER TABLE log ADD COLUMN msg VARCHAR(500)")
    db.safe_execute("ALTER TABLE log ADD COLUMN ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    db.safe_execute("ALTER TABLE log ADD COLUMN user INT")
    db.safe_execute("ALTER TABLE log ADD COLUMN ip_addr VARCHAR(15)")
    db.safe_execute("ALTER TABLE log ADD COLUMN color VARCHAR(7)")

    db.connection().close()
