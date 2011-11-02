#!/usr/bin/env python
# -*- coding: utf-8 -*-


import datetime

from flask import Flask
from flask import render_template
from flask import request
from flask import redirect
from jinja2 import FileSystemLoader

from gb.user import User
from gb.graph import Graph
from gb.node import Node
from gb.parser import parse, ParseError
from gb.config import *
from gb.log import log, get_logs


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
    u = None
    try:
        u = User().get_by_id(user_id)
    except:
        return None
    if u.check_session(session):
        return u
    else:
        return None


def set_session(response, user_id, session):
    expires = datetime.datetime.now() + datetime.timedelta(90)
    response.set_cookie('user_id', value=user_id, expires=expires, domain=COOKIES_DOMAIN)
    response.set_cookie('session', value=session, expires=expires, domain=COOKIES_DOMAIN)


def node_response(node_id, user, error=''):
    n = Node().get_by_id(node_id)
    nodes_json, links_json = n.neighbours_json()
    graphs = user.graph_list_for_user(user)
    r = application.make_response(render_template('node.html',
                                                  nodes_json=nodes_json,
                                                  links_json=links_json,
                                                  graph_id=n.d['graph'],
                                                  node_id=n.d['_id'],
                                                  graphs=graphs,
                                                  error=error))
    return r


@application.route("/")
def main():
    u = curuser()
    if u is None:
        return redirect2login()

    # TODO: these values are just temporary
    telmo = User().get_by_email('telmo@telmomenezes.com')
    g = Graph().get_by_owner_and_name(telmo, 'Cinema')

    redirect_to_index = redirect('/node/%s' % g.d['root'])
    response = application.make_response(redirect_to_index)   
    return response


@application.route('/node/<node_id>')
def node(node_id):
    u = curuser()
    if u is None:
        return redirect2login()
   
    return node_response(node_id, u)


@application.route("/login", methods=['GET', 'POST'])
def login():
    if request.method == 'GET':
        return render_template('login.html')
    else:
        email = request.form['email']
        password = request.form['password']

        u = User().get_by_email(email)
        if u.check_password(password):
            log('login', '#0000FF', u.d['email'], request.remote_addr)
            
            session = u.create_session()
            redirect_to_index = redirect('/')
            response = application.make_response(redirect_to_index)  
            expires = datetime.datetime.now() + datetime.timedelta(90)
            set_session(response, u.d['_id'], session)
            return response
        else:
            log('failed login [email: %s]' % email, '#FF3399', 'none', request.remote_addr)
            return render_template('login.html', message='Sorry, wrong username and/or password.')
        return email


@application.route("/logout")
def logout():
    u = curuser()
    if u is None:
        return redirect2login()
    
    log('logout', '#333333', u.d['email'], request.remote_addr)
    
    redirect_to_index = redirect('/')
    response = application.make_response(redirect_to_index)  
    response.set_cookie('session', value='', domain=COOKIES_DOMAIN)
    return response


@application.route("/input", methods=['POST',])
def input():
    u = curuser()
    if u is None:
        return redirect2login()
    
    error_msg = 'Sorry, could not understand the sentence. Want some <a href="/help">help</a>?'
    
    input_str = request.form['input']
    graph_id = request.form['graph_id']
    node_id = request.form['node_id']

    try:
        result = parse(input_str)
    except:
        log('sentence parse error (1) [%s]' % input_str, '#FF0000', u.d['email'], request.remote_addr)
        return node_response(node_id, u, error_msg)

    g = Graph().get_by_id(graph_id)
    orig_id = g.add_rel_from_parser(result)

    if (orig_id is None) or (orig_id == -1):
        log('sentence parse error (2) [%s]' % input_str, '#FF0000', u.d['email'], request.remote_addr)
        return node_response(node_id, u, error_msg)

    log('correct sentence [%s]' % input_str, '#33CC33', u.d['email'], request.remote_addr)
    
    redirect_to_index = redirect('/node/%s' % orig_id)
    response = application.make_response(redirect_to_index)   
    return response


@application.route("/add", methods=['POST',])
def add():
    u = curuser()
    if u is None:
        return redirect2login()
    
    graph_id = request.form['graph_id']
    node_id = request.form['node_id']
    orig_id = request.form['orig_id']
    orig_text = request.form['orig_text']
    rel = request.form['rel']
    targ_id = request.form['targ_id']
    targ_text = request.form['targ_text']

    g = Graph().get_by_id(graph_id)
    if not g.add_rel(rel, orig_id=orig_id, orig_text=orig_text, targ_id=targ_id, targ_text=targ_text):
        error_msg = 'Error adding relationship. Want some <a href="/help">help</a>?'
        log('error adding relationship %s->[%s]->%s' % (orig_id, rel, targ_text), '#FF0000', u.d['email'], request.remote_addr)
        return node_response(node_id, u, error_msg)

    log('relationship added through UI %s->[%s]->%s' % (orig_id, rel, targ_text), '#33CC33', u.d['email'], request.remote_addr)
    
    redirect_to_index = redirect('/node/%s' % node_id)
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
        u.set_permission(g, 'admin')

        log('brain created [%s]' % graph_name, '#00FF00', u.d['email'], request.remote_addr)

        redirect_to_index = redirect('/node/%s' % root.d['_id'])
        response = application.make_response(redirect_to_index)  
        return response


@application.route("/delink", methods=['POST'])
def delink():
    u = curuser()
    if u is None:
        return redirect2login()
    
    link_orig = request.form['link_orig']
    link_targ = request.form['link_targ']
    link_type = request.form['link_type']
    Graph().del_link(link_orig, link_targ, link_type)

    redirect_to_index = redirect('/node/%s' % link_orig)
    response = application.make_response(redirect_to_index)  
    return response


@application.route("/help", methods=['GET',])
def help():
    return render_template('help.html')


@application.route("/log", methods=['GET',])
def logpage():
    u = curuser()
    if u is None:
        return redirect2login()
    
    logs = get_logs()
    return render_template('log.html', logs=logs)


if __name__ == "__main__":
    #TODO: make this configurable
    application.debug = True
    
    application.run()
