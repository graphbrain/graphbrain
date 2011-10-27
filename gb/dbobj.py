# -*- coding: utf-8 -*-


from bson.objectid import ObjectId
import mongodb


class DbObj:
    def __init__(self):
        self.db = mongodb.getdb()
        self.d = {}
        self.collection = ''

    def get_by_id(self, obj_id):
        self.d = self.db[self.collection].find_one({'_id': ObjectId(obj_id)})
        return self
    
    def _insert(self):
        self.d['_id'] = self.db[self.collection].insert(self.d)

    def _update_field(self, field):
        if self.d[field]:
            self.db[self.collection].update({'_id': self.d['_id']}, {'$set': {field: self.d[field]}})
        else:
            self.db[self.collection].update({'_id': self.d['_id']}, {'$unset': {field: 1}})

    def _set_field(self, field, val):
        self.d[field] = val
        self._update_field(field)
