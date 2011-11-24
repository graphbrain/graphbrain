# -*- coding: utf-8 -*-


from datetime import datetime
import json

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

    def _neighbors(self, nodes, nodeids, depth=0, count=0):
        maxnodes = 25

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
                            count += 1
                            if count > maxnodes:
                                return
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
                            count += 1
                            if count > maxnodes:
                                return
                            nnode.parent = self.d['_id']
                            nodes.append(nnode)
                            nodeids.append(n)
                            next_nodes.append(nnode)

            for n in next_nodes:
                n._neighbors(nodes, nodeids, depth + 1, count) 

    def _internal_links(self, nodes, nodeids):
        ilinks = []
        link_id = 0
        for orig in nodes:
            if 'targs' in orig.d:
                targs = orig.d['targs']
                for targ_id, targ_list in targs.items():
                    if targ_id in nodeids:
                        for targ in targ_list:
                            ilinks.append({'id':link_id, 'orig': str(orig.d['_id']), 'targ': targ_id,
                                'relation': targ['relation'], 'directed': targ['directed']})
                            link_id += 1

        return ilinks

    def _reldict(self, links):
        rd = {}
        for l in links:
            orig_id = str(l['orig'])
            targ_id = str(l['targ'])
            rel = l['relation']
            dkey = (True, orig_id, rel)
            rkey = (False, rel, targ_id)
            if dkey in rd:
                rd[dkey].append(targ_id)
            else:
                rd[dkey] = [targ_id,]
            if rkey in rd:
                rd[rkey].append(orig_id)
            else:
                rd[rkey] = [orig_id,]
        return rd

    def _maxrel(self, rd):
        maxlen = -1
        maxkey = None
        for key in rd.keys():
            l = len(rd[key])
            if (maxlen < 0) or (l > maxlen):
                maxlen = l
                maxkey = key
        return maxkey

    def _delrel(self, rd, key):
        nodes = rd[key]
        del rd[key]

        for k in rd.keys():
            newrelnodes = []
            relnodes = rd[k]
            for n in relnodes:
                if not n in nodes:
                    newrelnodes.append(n)
            if len(newrelnodes) > 0:
                rd[k] = newrelnodes
            else:
                del rd[k]

    def _supernodes(self, links):
        snodes = []
        newlinks = links
        rd = self._reldict(links)
        key = self._maxrel(rd)
        super_id = 0
        while not key is None:
            nodes = rd[key]
            direction = key[0]
            orig_id = -1
            targ_id = -1
            rel = ''
            if direction:
                orig_id = key[1]
                rel = key[2]
            else:
                rel = key[1]
                targ_id = key[2]
            
            superkey = 'sn%d' % super_id
            snodes.append({'id': superkey, 'nodes': nodes})

            # adjust links
            auxlinks = []
            if direction:
                for l in newlinks:
                    if (not 'orig' in l) or (('orig' in l) and (l['orig'] != orig_id)) or (l['relation'] != rel):
                        auxlinks.append(l)
                auxlinks.append({'orig': orig_id, 'starg': superkey, 'relation': rel, 'directed': '1'})
            else:
                for l in newlinks:
                    if (not 'targ' in l) or (('targ' in l) and (l['targ'] != targ_id)) or (l['relation'] != rel):
                        auxlinks.append(l)
                auxlinks.append({'sorig': superkey, 'targ': targ_id, 'relation': rel, 'directed': '1'})
            newlinks = auxlinks

            self._delrel(rd, key)
            key = self._maxrel(rd)

            super_id += 1

        return snodes, newlinks

    def neighbours_json(self):
        self.parent = ''
        nodes = [self,]
        nodeids = [str(self.d['_id']),]
        self._neighbors(nodes, nodeids)
        links = self._internal_links(nodes, nodeids)

        snodes, links = self._supernodes(links)

        node_dict = {}
        for node in nodes:
            node_dict[str(node.d['_id'])] = {'parent':str(node.parent), 'text':node.d['label'], 'type':node.d['type']}
        nodes_json = json.dumps(node_dict, separators=(',',':'))

        snodes_json = json.dumps(snodes, separators=(',',':'))

        links_json = json.dumps(links, separators=(',',':'))

        return nodes_json, snodes_json, links_json