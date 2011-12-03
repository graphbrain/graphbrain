#!/usr/bin/env python
# -*- coding: utf-8 -*-


import re
from re import findall, DOTALL
import urllib
import urllib2
import sys
from pymongo import Connection


def getDBPediaTriple(rawtriple):
    tagPattern="(<http:.+?>)\s(<http:.+?>)\s(<http:.+?>)"
    elements=re.match(tagPattern, triple)
    thing=elements.group(1)
    thing=re.split('<http://dbpedia.org/resource/(.+?)>', thing)
    thing=thing[1]
    namespace=elements.group(2)
    namespace=re.split('<http://www.w3.org/(.+?)rdf-syntax-ns#(.+?)>', namespace)
    namespace=namespace[2]
    category=elements.group(3)
    category=re.split('<http://schema.org/(.+)>', category);
    category=category[1]
    return (thing, namespace, category)


def create(nodes, name):
    
    if(name in nodes):
        return '';
    else:
        nodes[name]={'name':name, 'subtypes':[]};
        return name;

def add_subtype(nodes, parent, subtype)
    if(parent in nodes):
        currentsubtypes=nodes[parent]['subtype'];
        if(subtype in currentsubtypes):
            return '';
        else:
            nodes[parent]['subtype'].append(subtype);
            create(nodes, subtype);
            return subtype;
    else:
        create(nodes, parent);
        add_subtype(nodes, parent, subtype);


def process_line(db, line, previouslines, nodes):
    
    print 'Processing: ', line
    triple=getDBPediaTriple(line); 

    
    #Check whether it is a top level category.
    if(category=='owl#Thing'):
        subcats=[];    
        #Process entire preceding hierarchy in reverse order since this gives the hierarchy.
        for(pl in previouslines):
            #The thing:
            create(things, pl[0])
            #The category:
            create(things, pl[2])
            add_subtype(things, pl[2], pl[0]);
            for subcat in subcats:
                add_subtype(things, pl[2], subcat);

            subcats.append(pl[2])
        previouslines=[]
    else:
        previouslines.append(triple)
            
   

def main():
    db = Connection().things

    type_file = open('instance_types_en.nt');
    p_list=[]
    nodes=[]
    for line in type_file:
        process_line(db, line, p_list, nodes)
    
    count=0;
    inserted=0;

    for thing in nodes:
        mthings=db.items
        if mthings.find_one({'name': thing, 'subtypes': nodes[thing]['subtypes']}) in None:
            mthings.insert(nodes[thing])
            inserted+=1;
            print '"%s" inserted' % thing
        else:
            print 'NOT INSERTED.' "%s" % thing
        count+=1;
    print '%d items, %d inserted. ' % (count, inserted)
         

if __name__=='__main__':
    main()