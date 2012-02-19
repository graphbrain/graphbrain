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

def getDBPediaTuple(rawqtuple):
    #Only get valid tuples:
    tagPattern="(<http:.+?>)\s(<http:.+?>)\s(<http:.+?>)\s(<http:.+?>)"
    elements=re.match(tagPattern, rawqtuple)
    qtuple=[]

    if(elements==None or len(elements.groups())!=4):
        return None;

    else:
        print(len(elements.groups()))

        from_node=elements.group(1)
        from_node=re.split('<http://(.+)/(.+?)>', from_node)
        if(len(from_node)<3):
            return None;
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
        if(len(to_node)<3):
            return None;
        to_node=to_node[2]
       
        from_source=elements.group(4)

        return (from_node, rel, to_node, from_source)

def process_line(line, previouslines, nodes):
    
    print 'Processing: ', line
    qtuple=getDBPediaTuple(line); 
    if(qtuple==None):
        return;
    else:
        
        pnln.add_link(nodes, qtuple[0], qtuple[2], qtuple[1], qtuple[3])
    

def process_infobox_properties():
    inputdoc=sys.stdin
    db = Connection().things

    p_list=[]
    nodes={}
    for line in inputdoc:
        process_line(line, p_list, nodes)
    
    pnln.build_graph(nodes, db)


def list_test():
    #file_loc=sys.stdin
    #type_file = open(file_loc);
    type_file=sys.stdin
    p_list=[]
    nodes={}
    counter=0
    while counter<3240:
        line=type_file.readline();
        process_line(line, p_list, nodes)
        counter+=1;
    print(nodes)
    print(len(nodes))


if __name__=='__main__':
    #type_floc='mappingbased_properties_en.nq'
    #list_test();
    process_infobox_properties()