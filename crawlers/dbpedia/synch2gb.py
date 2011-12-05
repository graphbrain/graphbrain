#!/usr/bin/env python
# -*- coding: utf-8 -*-


import sys
import nltk
from pymongo import Connection
from gb.node import Node
from gb.graph import Graph
from gb.user import User



def create_node_outlinks (db, node, graph, crawler='dbpedia'):
	current_node=Node().create_or_get_by_eid(label=node['name'], graph=graph, eid=node['content'], crawler=crawler, node_type=node['type']);
	relations=node['relations'];
	for relation_type in relations:
		#Check if relationship is a verb:
		if(check_verb(relation_type)):
			for relation in relationtype:
				r_node=db.items.find_one({'name':relation});
				related_node=Node().create_get_by_eid(label=r_node['name'], graph=graph, eid=r_node['content'], crawler=crawler, node_type=r_node['type']);
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

def synch(graph_owner, graph_name):
    # get graph
    u = User().get_by_email(graph_owner)
    graph = Graph().get_by_owner_and_name(u, graph_name)

    db = Connection().things

    # synch
    mthings = db.items
    total = mthings.count()
    count = 1
    q = mthings.find(timeout=False)
    for thing in q:
        print 'Synching item: %s [%d/%d] (%f%%)' % (thing['name'], count, total, (float(count) / float(total)) * 100)
        create_node_outlinks(db, graph, thing)
        count += 1


if __name__ == '__main__':
    graph_owner = 'gb@graphbrain.com'
    graph_name = 'Main'
    synch(graph_owner, graph_name)