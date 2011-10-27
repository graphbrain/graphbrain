# -*- coding: utf-8 -*-


from datetime import datetime

from dbobj import DbObj


class Node(DbObj):
    def __init__(self):
        DbObj.__init__(self)
        self.collection = 'nodes'

    def create(self, label, graph, node_type='text'):
        self.d['label'] = label
        self.d['type'] = node_type
        self.d['graph'] = graph.d['_id']
        self.d['creation_ts'] = datetime.now()
        self.d['origs'] = []
        self.d['targs'] = {}

        self._insert()

        return self

    def get_by_label(self, label, graph_id):
        self.d = self.db.nodes.find_one({'label': label, 'graph': graph_id})
        return self

    def _neighbors(self, nodes, depth=0):
        if self.d is None:
            return

        if str(self.d['_id']) not in nodes.keys():
            nodes[str(self.d['_id'])] = self

        if (depth < 2):
            if 'targs' in self.d:
                for n in self.d['targs'].keys():
                    nnode = Node().get_by_id(n)
                    nnode.parent = self.d['_id']
                    nnode._neighbors(nodes, depth + 1)

            if 'origs' in self.d:
                for n in self.d['origs']:
                    nnode = Node().get_by_id(n)
                    nnode.parent = self.d['_id']
                    nnode._neighbors(nodes, depth + 1)

    def _internal_links(self, nodes):
        ilinks = {}
        for key, node in nodes.items():
            ilinks[node.d['_id']] = []
    
        for key, orig in nodes.items():
            if 'targs' in orig.d:
                targs = orig.d['targs']
                for targ_id, targ_list in targs.items():
                    if targ_id in nodes.keys():
                        for targ in targ_list:
                            ilinks[orig.d['_id']].append({'_id': targ_id, 'relation': targ['relation'], 'directed': targ['directed']})

        return ilinks

    def neighbours_json(self):
        self.parent = ''
        nodes = {}
        self._neighbors(nodes)
        links = self._internal_links(nodes)

        nodes_json = '['
        for node_id in nodes.keys():
            node = nodes[node_id]
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
