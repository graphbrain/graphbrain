#!/usr/bin/env python


import os

from flask import Flask
from flask import render_template
from flask import request
from flask import redirect
from flask import send_from_directory
from jinja2 import FileSystemLoader

from gb.user import User
from gb.graph import Graph
from gb.node import Node
from gb.link import Link
from gb.parser import parse, ParseError
from gb.config import *


application = Flask(__name__)


# setting templates directory
if TEMPLATE_DIR != '':
    template_path = TEMPLATE_DIR
    application.jinja_loader = FileSystemLoader(template_path)


def redirect2login():
    redirect_to_index = redirect('/login')
    response = application.make_response(redirect_to_index)   
    return response


def curuser():
    user_id = request.cookies.get('user_id')
    session = request.cookies.get('session')
    if user_id is None:
        return None
    if session is None:
        return None
    u = User().get_by_id(user_id)
    if u.check_session(session):
        return u
    else:
        return None


@application.route("/")
def main():
    u = curuser()
    if u is None:
        return redirect2login()

    # TODO: these values are just temporary
    telmo = User().get_by_email('telmo@telmomenezes.com')
    g = Graph().get_by_owner_and_name(telmo, 'Cinema')

    redirect_to_index = redirect('/node/%d' % g.root.id)
    response = application.make_response(redirect_to_index)   
    return response


@application.route('/node/<node_id>')
def node(node_id):
    u = curuser()
    if u is None:
        return redirect2login()
   
    n = Node().get_by_id(int(node_id))
    nodes_json, links_json = n.neighbours_json()
    graphs = Graph().graph_list_for_user(u)
    r = application.make_response(render_template('node.html',
                                                  nodes_json=nodes_json,
                                                  links_json=links_json,
                                                  graph_id=n.graph,
                                                  graphs=graphs))
    return r


@application.route("/login", methods=['GET', 'POST'])
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
            response = application.make_response(redirect_to_index)  
            response.set_cookie('user_id', value=u.id)
            response.set_cookie('session', value=session)
            return response
        else:
            return render_template('login.html', message='Sorry, wrong username and/or password.')
        return email


@application.route("/logout")
def logout():
    u = curuser()
    if u is None:
        return redirect2login()
    
    redirect_to_index = redirect('/')
    response = application.make_response(redirect_to_index)  
    response.delete_cookie('user_id')
    response.delete_cookie('session')
    return response


@application.route("/input", methods=['POST',])
def input():
    u = curuser()
    if u is None:
        return redirect2login()
    
    input_str = request.form['input']
    graph_id = request.form['graph_id']

    try:
        result = parse(input_str)
    except ParseError, p:
        # TODO: temporary way of dealing with parse exceptions
        redirect_to_index = redirect('/')
        response = application.make_response(redirect_to_index)  
        return response

    g = Graph()
    g.id = graph_id
    orig_id = g.add_rel(result)

    redirect_to_index = redirect('/node/%d' % orig_id)
    response = application.make_response(redirect_to_index)  
    return response


@application.route("/selbrain", methods=['POST',])
def selbrain():
    node_id = request.form['brainDropDown']

    redirect_to_index = redirect('/node/%s' % node_id)
    response = application.make_response(redirect_to_index)  
    return response


@application.route("/createbrain", methods=['GET', 'POST'])
def createbrain():
    u = curuser()
    if u is None:
        return redirect2login()
    
    if request.method == 'GET':
        return render_template('createbrain.html')
    else:
        graph_name = request.form['name']
        root_data = request.form['item']

        g = Graph().create(graph_name, u)
        root = Node().create(root_data, g)
        g.set_root(root)

        # set admin permission for owner
        g.set_permission(u, 0)

        redirect_to_index = redirect('/node/%d' % root.id)
        response = application.make_response(redirect_to_index)  
        return response


@application.route("/delink", methods=['POST'])
def delink():
    u = curuser()
    if u is None:
        return redirect2login()
    
    link_id = request.form['link_id']
    link = Link().get_by_id(link_id)
    node_id = link.orig
    link.delete()

    redirect_to_index = redirect('/node/%d' % node_id)
    response = application.make_response(redirect_to_index)  
    return response


@application.route("/help", methods=['GET',])
def help():
    return render_template('help.html')


@application.route('/favicon.ico')
def favicon():
    #return send_from_directory(ICO_DIR, 'GB_ico.ico', mimetype='image/vnd.microsoft.icon')
    redirect_to_index = redirect('/static/GB_ico.ico')
    response = application.make_response(redirect_to_index)  
    return response


# temporary Y-Combinator backdoor
@application.route("/ycombinator-vip-room")
def ycombinator():
    u = User().get_by_email('info@ycombinator.com')
    session = u.create_session()
    redirect_to_index = redirect('/')
    response = application.make_response(redirect_to_index)  
    response.set_cookie('user_id', value=u.id)
    response.set_cookie('session', value=session)
    return response


if __name__ == "__main__":
    #TODO: make this configurable
    application.debug = True
    
    application.run()
