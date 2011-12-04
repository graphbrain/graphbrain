#!/usr/bin/env python
# -*- coding: utf-8 -*-


import re
from re import findall, DOTALL
import urllib
import urllib2
import sys
from pymongo import Connection
import process_nodelinknode as pnln


def getDBPediaTriple(rawtriple):
    #Only get valid tuples:
    tagPattern="(<http:.+?>)\s(<http:.+?>)\s(<http:.+?>)"
    elements=re.match(tagPattern, rawtriple)
    triple=[]

    if(elements==None or len(elements.groups())!=3):
        return None;

    else:
        print(len(elements.groups()))

        from_node=elements.group(1)
        from_node=re.split('<http://(.+)/(.+?)>', from_node)
        from_node=from_node[2]

        rel=elements.group(2)
        rel=re.split('<http://(.+)/(.+?)>', rel)
        two_part=re.search('([a-z]+?)([A-Z]{1}[a-z]+)+?', rel[2])
        if(two_part is None or len(two_part.groups())!=2):
            rel=rel[2]
        else:
            rel = two_part.group(1) + '' + two_part.group(2).lower();

        to_node=elements.group(3)
        to_node=re.split('<http://(.+)/(.+?)>', to_node);
        to_node=to_node[2]
       
        
        return (from_node, rel, to_node)

def process_line(line, previouslines, nodes):
    
    print 'Processing: ', line
    triple=getDBPediaTriple(line); 
    if(triple==None):
        return;
    else:
        
        pnln.add_link(nodes, triple[0], triple[2], triple[1])
    


def list_test(file_loc):

    type_file = open(file_loc);
    p_list=[]
    nodes={}
    counter=0
    while counter<60:
        line=type_file.readline();
        process_line(line, p_list, nodes)
        counter+=1;
    print(nodes)
    print(len(nodes))


if __name__=='__main__':
    type_floc='infobox_properties_en.nt'
    #type_floc=sys.stdin;
    #build_category_tree(type_floc)
    list_test(type_floc);