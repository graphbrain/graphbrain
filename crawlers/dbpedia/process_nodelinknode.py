def create(nodes, name, node_type='text'):
	if(name in nodes):
		return '';
	else:
		nodes[name]={'name':name, 'relations':{}, 'type':node_type, 'content': name};
		return name;
	
	 

def add_relationship(nodes, name, rel_name):
    if(name in nodes):
        if(rel_name in nodes[name]):
            return '';
        else:
            nodes[name]['relations'][rel_name]=[]
    else:
        create(nodes, name);

def add_link(nodes, from_node, to_node, rel_type):
	
	if(from_node in nodes):
		if(rel_type in nodes[from_node]['relations']):
			currentcats=nodes[from_node]['relations'][rel_type]
			if(to_node in currentcats):
				return'';
			else:
				nodes[from_node]['relations'][rel_type].append(to_node)
				create(nodes, to_node);
				return to_node;
		else:         
			add_relationship(nodes, from_node, rel_type);
			add_link(nodes, from_node, to_node, rel_type);

	else:
		create(nodes, from_node)
		add_relationship(nodes, from_node, rel_type)
        add_link(nodes, from_node, to_node, rel_type)

def build_graph(entries, db):

	

    count=0;
    inserted=0;
    mthings=db.items

    for thing in entries:
    	if mthings.findone({'name': thing, 'relations': entries[thing]['relations']}) is None:
    		mthings.insert(nodes[thing]);
    		inserted+=1;
    		print '"%s" inserted' % thing
    	else:
    		print 'NOT INSERTED.' "%s" % thing
    	count+=1;
    print '%d items, %d inserted. ' % (count, inserted)