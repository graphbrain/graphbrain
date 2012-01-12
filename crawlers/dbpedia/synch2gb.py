#!/usr/bin/env python
# -*- coding: utf-8 -*-


import sys
import nltk
from pymongo import Connection
from gb.node import Node
from gb.graph import Graph
from gb.user import User
from gb.ids import url_id, wikipedia_id, gb_id


DBPeidaCats="DBPediaCats";

def create_node_outlinks_Wikipedia (db, node, graph, crawler):
	_id=wikipedia_id(node['name'])
	current_node=Node().create_or_get_by_id(label=node['name'], graph=graph, _id=_id, crawler=crawler, node_type=node['type']);
	relations=node['relations'];
	for relation_type in relations.keys():
		#Check if relationship is a verb:
		if(check_verb(relation_type)):
			current_rtype=relations[relation_type];
			for relation in current_rtype:
				r_node=db.items.find_one({'name':relation});
				_id=wikipedia_id(r_node['name']);
				related_node=Node().create_or_get_by_id(label=r_node['name'], graph=graph, _id=_id, crawler=crawler, node_type=r_node['type']);
				graph.add_link(current_node, related_node, relation_type, relation_type);
				print 'Relation type added: %s' relation_type 
		else:
			print 'Relation type NOT ADDED: %s' relation_type 
			continue;

def check_verb(relation):
	tokens=nltk.word_tokenize(relation);
	verb_tag=re.compile('V[A-Z]*')
	tagged=nltk.pos_tag(tokens);
	tags=[t[1] for t in tagged];

	if(re.search(verb_tag, str(tags)) is None):
		return False;
	else:
		return True;

def synchDB(db, graph, crawler_name, data):
	total=data.count();
	count=1;
	q=data.find(timeout=False)
	for thing in q:
		print 'Synching item: %s [%d/%d] (%f%%)' % (thing['name'], count, total, (float(count) / float(total)) * 100)
        create_node_outlinks(db, graph, thing, crawler_name)
        count += 1

def synchDBInfo(graph_owner, graph_name):
	# get graph
    u = User().get_by_email(graph_owner)
    graph = Graph().get_by_owner_and_name(u, graph_name, crawler_name)
    crawler_name="DBPediaInfo";
    
    db = Connection().things
	# synch
    mthings = db.items
	synchDB(db, graph_owner, graph_name, crawler_name, mthings)


def synchDBCats(graph_owner, graph_name):
	# get graph
    u = User().get_by_email(graph_owner)
    graph = Graph().get_by_owner_and_name(u, graph_name, crawler_name)
    crawler_name="DBPediaCats";
    db = Connection().categories
	
	# synch
    mthings = db.items
	synchDB(db, graph_owner, graph_name, crawler_name, mthings)

	    
    

if __name__ == '__main__':
    graph_owner = 'gb@graphbrain.com'
    graph_name = 'Main'

    synchDBInfo(graph_owner, graph_name)