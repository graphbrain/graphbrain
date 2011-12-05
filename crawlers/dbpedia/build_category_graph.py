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



def add_subtype(nodes, parent, subtype):
    pnln.add_link(nodes, subtype, parent, 'is a')

def process_line(line, previouslines, nodes):
    
    print 'Processing: ', line
    triple=getDBPediaTriple(line); 

    
    #Check whether it is a top level category.
    if(triple[2]=='owl#Thing'):
        subcats=[];    
        #Process entire preceding hierarchy in reverse order since this gives the hierarchy.
        for pl in previouslines:
            #The thing:
            pnln.create(nodes, pl[0])
            #The category:
            pnln.create(nodes, pl[2])
            add_subtype(nodes, pl[2], pl[0]);
            for subcat in subcats:
                add_subtype(nodes, pl[2], subcat);

            subcats.append(pl[2])
        previouslines=[]

    else:
        previouslines.append(triple)
            
   

def process_categories():

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
    while counter<20:
        line=type_file.readline();
        process_line(line, p_list, nodes)
        counter+=1;
    
    print(nodes)
    print(len(nodes))


if __name__=='__main__':
    type_floc='instance_types_en.nt'
    list_test(type_floc);
    #process_categories()