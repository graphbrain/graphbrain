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
    tagPattern="(<http:.+?>)\s(<http:.+?>)\s(<http:.+?>)"
    elements=re.match(tagPattern, rawtriple)
    
    thing=elements.group(1)
    thing=re.split('<http://(.+)/(.+?)>', thing)
    
    thing=thing[2]
    namespace=elements.group(2)
    namespace=re.split('<http://(.+)/(.+?)>', namespace)
    
    namespace=namespace[2]
    category=elements.group(3)
    category=re.split('<http://(.+)/(.+?)>', category);
    
    category=category[2]
    return (thing, namespace, category)

def getDBPediaTuple(rawqtuple):
    tagPattern="(<http:.+?>)\s(<http:.+?>)\s(<http:.+?>)\s(<http:.+?>)"
    elements=re.match(tagPattern, rawqtuple)
    
    thing=elements.group(1)
    thing=re.split('<http://(.+)/(.+?)>', thing)
    
    thing=thing[2]
    namespace=elements.group(2)
    namespace=re.split('<http://(.+)/(.+?)>', namespace)
    
    namespace=namespace[2]
    category=elements.group(3)
    category=re.split('<http://(.+)/(.+?)>', category);
    
    category=category[2]

    cat_source=elements.group(4)
    return (thing, namespace, category, cat_source)



def add_subtype(nodes, parent, subtype, cat_source):
    pnln.add_link(nodes, subtype, parent, 'is a', cat_source)

def process_line_maxdepth(line, previouslines, nodes):
    
    print 'Processing: ', line
    qtuple=getDBPediaTuple(line); 

    
    #Check whether it is a top level category.
    if(qtuple[2]=='owl#Thing'):
        pnln.create(nodes, qtuple[0], 'text', qtuple[3])
        #Process entire preceding hierarchy in reverse order since this gives the hierarchy.
        for pl in previouslines:
            #The thing:
            pnln.create(nodes, pl[0], 'text', pl[3])

            #The category:
            pnln.create(nodes, pl[2])
            add_subtype(nodes, pl[2], pl[0], pl[3]);
            add_subtype(nodes, pl[2], qtuple[0], qtuple[3])


        previouslines=[]
        return previouslines


    else:
        previouslines.append(qtuple)
        return previouslines

def process_line(line, previouslines, nodes):
    
    print 'Processing: ', line
    qtuple=getDBPediaTuple(line); 

    
    pnln.create(nodes, qtuple[0], 'text', qtuple[3])
    add_subtype(nodes, qtuple[2], qtuple[0], qtuple[3])
    previouslines=[]
    return previouslines

def process_categories(infile=None):
    if(infile==None):
        inputdoc=sys.stdin
    else:
        inputdoc=open(infile)

    db = Connection().categories

    
    p_list=[]
    nodes={}
    for line in inputdoc:
        process_line_maxdepth(line, p_list, nodes)
        
    pnln.buildgraph(nodes, db)
         


def list_test(file_loc):

    type_file = open(file_loc);
    p_list=[]
    nodes={}
    counter=0
    while counter<100:
        line=type_file.readline();
        p_list=process_line_maxdepth(line, p_list, nodes)
        #p_list=process_line(line, p_list, nodes)
        counter+=1;
    
    print(nodes)
    print(len(nodes))


if __name__=='__main__':
    type_floc='/Users/chihchun_chen/Dropbox/DBPedia/data/instance_types_en.nq'
    #list_test(type_floc);
    process_categories()