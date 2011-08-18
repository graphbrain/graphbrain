#!/usr/bin/env python


from flask import Flask
from flask import render_template
from flask import request

from gb.user import User
from gb.graph import Graph
from gb.node import Node


app = Flask(__name__)


@app.route("/")
def main():
    # TODO: these values are just temporary
    u = User().get_by_email('telmo@telmomenezes.com')
    g = Graph().get_by_owner_and_name(u, 'Demo')
    root = g.root

    nodes_json, links_json = root.neighbours_json()

    r = app.make_response(render_template('node.html', nodes_json=nodes_json, links_json=links_json))
    return r


@app.route("/login", methods=['GET', 'POST'])
def login():
    if request.method == 'GET':
        return render_template('login.html')
    else:
        email = request.form['email']
        password = request.form['password']

        u = User().get_by_email(email)
        if u.check_password(password):
            pass
        else:
            pass
        return email


if __name__ == "__main__":
    #TODO: make this configurable
    app.debug = True
    
    app.run()
