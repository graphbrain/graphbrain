#!/usr/bin/env python


from flask import Flask
from flask import render_template
from flask import request
from flask import redirect

from gb.user import User
from gb.graph import Graph
from gb.node import Node
from gb.parser import parse


app = Flask(__name__)


def redirect2login():
    redirect_to_index = redirect('/login')
    response = app.make_response(redirect_to_index)   
    return response


def requirelogin(f):
    def fun():
        user_id = request.cookies.get('user_id')
        session = request.cookies.get('session')
        if user_id is None:
            return redirect2login()
        if session is None:
            return redirect2login()
        u = User().get_by_id(user_id)
        if u.check_session(session):
            return f(u)
        else:
            return redirect2login()
    fun.__name__ = f.__name__
    return fun


@app.route("/")
@requirelogin
def main(u):
    # TODO: these values are just temporary
    #u = User().get_by_email('telmo@telmomenezes.com')
    g = Graph().get_by_owner_and_name(u, 'Demo')
    root = g.root

    nodes_json, links_json = root.neighbours_json()

    r = app.make_response(render_template('node.html', nodes_json=nodes_json, links_json=links_json, graph_id=g.id))
    return r


@app.route('/node/<node_id>')
def node(node_id):
    n = Node().get_by_id(int(node_id))

    nodes_json, links_json = n.neighbours_json()

    r = app.make_response(render_template('node.html', nodes_json=nodes_json, links_json=links_json, graph_id=n.graph))
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
            session = u.create_session()
            redirect_to_index = redirect('/')
            response = app.make_response(redirect_to_index)  
            response.set_cookie('user_id', value=u.id)
            response.set_cookie('session', value=session)
            return response
        else:
            return render_template('login.html', message='Sorry, wrong username and/or password.')
        return email


@app.route("/logout")
@requirelogin
def logout(u):
    redirect_to_index = redirect('/')
    response = app.make_response(redirect_to_index)  
    response.delete_cookie('user_id')
    response.delete_cookie('session')
    return response


@app.route("/input", methods=['POST',])
@requirelogin
def input(u):
    input_str = request.form['input']
    graph_id = request.form['graph_id']

    orig, rel, targ = parse(input_str)
    g = Graph()
    g.id = graph_id
    g.add_rel(orig, rel, targ)

    redirect_to_index = redirect('/')
    response = app.make_response(redirect_to_index)  
    return response


if __name__ == "__main__":
    #TODO: make this configurable
    app.debug = True
    
    app.run()
