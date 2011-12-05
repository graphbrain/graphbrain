#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
from re import findall, DOTALL
import urllib
import urllib2
import sys
from pymongo import Connection
import process_nodelinknode as pnln


TAG_EXTERNAL_LINK='<http://dbpedia.org/ontology/wikiPageExternalLink>';
TAG_HOMEPAGE_LINK='<http://xmlns.com/foaf/0.1/homepage>'
TAG_WIKIPEDIA_PAGE='<http://xmlns.com/foaf/0.1/page>'
TAG_IMAGE_LINK='<http://xmlns.com/foaf/0.1/depiction>'

def getDBPediaTriple(rawtriple):
    #Only get valid tuples:
    tagPattern="(<http:.+?>)\s(<http:.+?>)\s(<http:.+?>)"
    elements=re.match(tagPattern, rawtriple)
    triple=[]
    rel=''

    if(elements==None or len(elements.groups())!=3):
        return None;

    else:
        
        from_node=elements.group(3)
        
        if(elements.group(2)==TAG_EXTERNAL_LINK):
            rel='tells you more about';
        elif(elements.group(2)==TAG_HOMEPAGE_LINK):
            rel='is the homepage of'
        elif(elements.group(2)==TAG_WIKIPEDIA_PAGE):
            rel='is Wikipedia entry for';
        elif(elements.group(2)==TAG_IMAGE_LINK):
            rel='is image of';
        else:
            return None;

        to_node=elements.group(1)
        to_node=re.split('<http://(.+)/(.+?)>', to_node);
        to_node=to_node[2]
               
        return (from_node, rel, to_node)

def process_line(line, previouslines, nodes):
    node_type='url'
    print 'Processing: ', line
    triple=getDBPediaTriple(line); 
    if(triple==None):
        return;
    else:
        if(triple[1]=='is image of'):
            node_type='image';

        pnln.create(nodes, triple[0], node_type);
        pnln.create(nodes, triple[2], 'text');
        pnln.add_link(nodes, triple[0], triple[2], triple[1]);
    
def process_externallinks():
    inputdoc=sys.stdin
    db = Connection().things

    
    p_list=[]
    nodes={}
    for line in inputdoc:
        process_line(line, p_list, nodes)
    
    pnln.buildgraph(nodes, db)

def list_test(file_loc):

    type_file = open(file_loc);
    p_list=[]
    nodes={}
    counter=0
    while counter<50:
        line=type_file.readline();
        process_line(line, p_list, nodes)
        counter+=1;
    print(nodes)
    print(len(nodes))



if __name__=='__main__':
    floc='external_links_en.nt'
    list_test(floc);
    #process_externallinks();