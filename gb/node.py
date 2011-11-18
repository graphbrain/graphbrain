# -*- coding: utf-8 -*-


from datetime import datetime

from dbobj import DbObj


class Node(DbObj):
    def __init__(self):
        DbObj.__init__(self)
        self.collection = 'nodes'

    def create(self, label, graph, node_type='text', eid='', crawler=''):
        self.d = {}
        self.d['label'] = label
        self.d['type'] = node_type
        self.d['graph'] = graph.d['_id']
        self.d['creation_ts'] = datetime.now()
        self.d['origs'] = []
        self.d['targs'] = {}
        self.d['eid'] = eid
        self.d['crawler'] = crawler

        self._insert()

        return self

    def create_or_get_by_eid(self, label, graph, node_type='text', eid='', crawler=''):
        self.d = self.db.nodes.find_one({'eid': eid, 'graph': graph.d['_id']})
        if self.d is None:
            self.create(label, graph, node_type=node_type, eid=eid, crawler=crawler)
        return self

    def get_by_label(self, label, graph_id):
        self.d = self.db.nodes.find_one({'label': label, 'graph': graph_id})
        return self

    def _neighbors(self, nodes, nodeids, depth=0):
        if self.d is None:
            return

        next_nodes = []

        if (depth < 2):
            if 'targs' in self.d:
                for n in self.d['targs'].keys():
                    n = str(n)
                    if n not in nodeids:
                        nnode = Node().get_by_id(n)
                        if not nnode.d is None:
                            nnode.parent = self.d['_id']
                            nodes.append(nnode)
                            nodeids.append(n)
                            next_nodes.append(nnode)

            if 'origs' in self.d:
                for n in self.d['origs']:
                    n = str(n)
                    if n not in nodeids:
                        nnode = Node().get_by_id(n)
                        if not nnode.d is None:
                            nnode.parent = self.d['_id']
                            nodes.append(nnode)
                            nodeids.append(n)
                            next_nodes.append(nnode)

            for n in next_nodes:
                n._neighbors(nodes, nodeids, depth + 1) 


    def _internal_links(self, nodes, nodeids):
        ilinks = {}
        for node in nodes:
            ilinks[node.d['_id']] = []
    
        for orig in nodes:
            if 'targs' in orig.d:
                targs = orig.d['targs']
                for targ_id, targ_list in targs.items():
                    if targ_id in nodeids:
                        for targ in targ_list:
                            ilinks[orig.d['_id']].append({'_id': targ_id, 'relation': targ['relation'], 'directed': targ['directed']})

        return ilinks

    def neighbours_json(self):
        self.parent = ''
        nodes = [self,]
        nodeids = [str(self.d['_id']),]
        self._neighbors(nodes, nodeids)
        links = self._internal_links(nodes, nodeids)

        nodes_json = '['
        for node in nodes:
            nodes_json = '%s{"id":"%s", "parent":"%s", "text":"%s", "type":"%s"},' % (
                nodes_json, str(node.d['_id']), str(node.parent), node.d['label'], node.d['type'])
        nodes_json = '%s]' % nodes_json

        link_id = 0
        links_json = '['
        for orig_id in links.keys():
            for targ in links[orig_id]:
                links_json = '%s{"id":"%d", "orig": "%s", "targ":"%s", "type":"%s", "dir":"%s"},' % (
                            links_json, link_id, str(orig_id), str(targ['_id']), targ['relation'], targ['directed'])
                link_id += 1
        links_json = '%s]' % links_json

        return nodes_json, links_json