#   Copyright (c) 2016 CNRS - Centre national de la recherche scientifique.
#   All rights reserved.
#
#   Written by Telmo Menezes <telmo@telmomenezes.com>
#
#   This file is part of GraphBrain.
#
#   GraphBrain is free software: you can redistribute it and/or modify
#   it under the terms of the GNU Affero General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   GraphBrain is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU Affero General Public License for more details.
#
#   You should have received a copy of the GNU Affero General Public License
#   along with GraphBrain.  If not, see <http://www.gnu.org/licenses/>.


import gb.hypergraph.symbol as sym
import gb.hypergraph.edge as ed


LEAF = 0
NODE = 1


class Position(object):
    LEFT, RIGHT = range(2)


class Tree(object):
    def __init__(self, elem=None):
        self.root_id = None
        self.cur_id = 0
        self.table = {}
        if elem:
            self.root_id = elem.id
            self.import_element(elem)

    def import_element(self, elem):
        if elem.is_leaf():
            new_elem = Leaf(elem.token)
        else:
            new_elem = Node(elem.children_ids)
        new_elem.copy(elem)
        new_elem.tree = self
        self.set(elem.id, new_elem)
        if elem.is_node():
            for child in elem.children():
                self.import_element(child)
        if elem.id >= self.cur_id:
            self.cur_id = elem.id + 1
        return new_elem

    def get(self, elem_id):
        if elem_id is None:
            return None
        return self.table[elem_id]

    def set(self, elem_id, elem):
        elem.id = elem_id
        self.table[elem_id] = elem

    def add(self, elem):
        self.set(self.cur_id, elem)
        self.cur_id += 1

    def root(self):
        assert(self.root_id is not None)
        return self.get(self.root_id)

    def create_leaf(self, token):
        leaf = Leaf(token)
        leaf.tree = self
        self.add(leaf)
        return leaf

    def create_node(self, children=None):
        node = Node(children)
        node.tree = self
        self.add(node)
        return node

    def enclose(self, entity):
        node_id = entity.id
        self.add(entity)
        node = self.create_node([entity.id])
        self.set(node_id, node)
        return node

    def disenclose(self, node):
        assert(node.is_node())
        assert(len(node.children_ids) > 0)
        entity_id = node.id
        inner_entity = self.get(node.children_ids[0])
        self.set(entity_id, inner_entity)

    def remove_redundant_nesting(self):
        self.root().remove_redundant_nesting()

    def clone_id(self, elem_id):
        elem = self.get(elem_id)
        return elem.clone().id

    def to_hyperedge(self, with_namespaces=True):
        return self.root().to_hyperedge(with_namespaces=with_namespaces)

    def to_hyperedge_str(self, with_namespaces=True):
        return ed.edge2str(self.to_hyperedge(with_namespaces=with_namespaces))

    def __str__(self):
        return str(self.get(self.root_id))


class Element(object):
    def __init__(self):
        self.type = None
        self.id = None
        self.tree = None
        self.namespace = None
        self.position = None
        self.connector = False

    def as_label_list(self, lemmas=False):
        # throw exception
        pass

    def as_text(self, lemmas=False):
        # throw exception
        pass

    def is_leaf(self):
        return self.type == LEAF

    def is_node(self):
        return self.type == NODE

    def is_compound(self):
        return True

    def is_connector(self):
        return self.connector

    def is_terminal(self):
        return self.is_leaf() or self.is_compound()

    def is_not_terminal(self):
        return not self.is_terminal()

    def get_namespace(self):
        if not self.namespace:
            self.generate_namespace()
        return self.namespace

    def to_synonym(self):
        return None

    def apply_layers(self):
        # if not implemented, do nothing.
        pass

    def remove_redundant_nesting(self):
        # if not implemented, do nothing.
        pass

    def flatten(self):
        # if not implemented, do nothing.
        pass

    def add_child(self, elem_id):
        # throw exception
        pass

    def add_to_first_child(self, elem_id, pos):
        # throw exception
        pass

    def nest(self, elem_id):
        # throw exception
        pass

    def has_pos(self, pos, shallow=False):
        # throw exception
        pass

    def has_dep(self, dep, shallow=False):
        # throw exception
        pass

    def has_dep_in(self, deps, shallow=False):
        # throw exception
        pass

    def arity(self):
        # throw exception
        pass

    def flat_leafs(self):
        # throw exception
        pass

    def to_leafs(self):
        # throw exception
        pass

    def to_hyperedge(self, with_namespaces=True):
        # throw exception
        pass

    def get_inner_nested_node(self):
        # throw exception
        pass

    def generate_namespace(self):
        # throw exception
        pass

    def all_tokens(self):
        # throw exception
        pass

    def apply_(self, child_id, pos):
        # throw exception
        pass

    def nest_(self, child_id, pos):
        # throw exception
        pass

    def nest_deep(self, child_id, pos):
        # throw exception
        pass

    def __eq__(self, other):
        return NotImplemented

    def __ne__(self, other):
        result = self.__eq__(other)
        if result is NotImplemented:
            return result
        return not result


class Leaf(Element):
    def __init__(self, token):
        super(Leaf, self).__init__()
        self.type = LEAF
        self.token = token

    def copy(self, source):
        self.token = source.token
        self.namespace = source.namespace
        self.position = source.position
        self.connector = source.connector

    # Leaf is immutable
    def clone(self):
        return self

    # override
    def as_label_list(self, lemmas=False):
        if lemmas:
            return [self.token.lemma.lower()]
        else:
            return [self.token.word.lower()]

    # override
    def as_text(self, lemmas=False):
        if lemmas:
            return self.token.lemma.lower()
        else:
            return self.token.word.lower()

    # override
    def add_child(self, elem_id):
        node = self.tree.enclose(self)
        node.add_child(elem_id)

    # override
    def add_to_first_child(self, elem_id, pos):
        node = self.tree.enclose(self)
        return node.add_to_first_child(elem_id, pos)

    # override
    def nest(self, elem_id):
        node = self.tree.enclose(self)
        return node.nest(elem_id)

    # override
    def has_pos(self, pos, shallow=False):
        return self.token.pos == pos

    # override
    def has_dep(self, dep, shallow=False):
        return self.token.dep == dep

    def has_dep_in(self, deps, shallow=False):
        return self.token.dep in deps

    # override
    def arity(self):
        return 1

    # override
    def flat_leafs(self):
        return [self]

    # override
    def to_leafs(self):
        return [self]

    # override
    def to_hyperedge(self, with_namespaces=True):
        if not with_namespaces:
            s = sym.str2symbol(self.token.word)
        else:
            s = sym.build(self.token.word, self.namespace)
        if self.connector:
            s = '+%s' % s
        return s

    # override
    def generate_namespace(self):
        self.namespace = 'nlp.%s.%s' % (self.token.lemma.lower(), self.token.pos.lower())

    # override
    def get_inner_nested_node(self):
        return self

    def all_tokens(self):
        return [self.token]

    def apply_(self, child_id, pos):
        node = self.tree.enclose(self)
        node.apply_(child_id, pos)

    def nest_(self, child_id, pos):
        node = self.tree.enclose(self)
        node.nest_(child_id, pos)

    def nest_deep(self, child_id, pos):
        node = self.tree.enclose(self)
        node.nest_deep(child_id, pos)

    def __eq__(self, other):
        if isinstance(other, Leaf):
            return self.token == other.token
        elif isinstance(other, Node):
            return False
        return NotImplemented

    def __str__(self):
        s = self.token.word
        if self.connector:
            s = '+%s' % s
        return s


class Node(Element):
    def __init__(self, children_ids=None):
        super(Node, self).__init__()
        self.type = NODE
        if children_ids is None:
            self.children_ids = []
        else:
            self.children_ids = children_ids[:]
        self.layer_ids = []
        self.layer_id = -1
        self.__compound = False
        self.inner_nested_node_id = -1

    def copy(self, source):
        self.__compound = source.__compound
        self.namespace = source.namespace
        self.position = source.position
        self.connector = source.connector
        self.inner_nested_node_id = source.inner_nested_node_id

    def clone(self):
        new_node = self.tree.create_node([self.tree.clone_id(child_id) for child_id in self.children_ids])
        new_node.copy(self)
        return new_node

    # override
    def as_label_list(self, lemmas=False):
        label_list = []
        children = self.children()
        rel = children[0]
        if rel.is_leaf():
            rel = (rel,)
        else:
            rel = rel.children()
        entities = children[1:]
        if len(rel) == 1 and len(entities) == 1:
            entity = entities[0]
            # TODO: make this better...
            if rel[0].is_leaf() and rel[0].as_text() == 'and' and entity.is_node() and entity.arity() == 2:
                ent_children = entity.children()
                label_list += ent_children[0].as_label_list(lemmas)
                label_list += rel[0].as_label_list(lemmas)
                label_list += ent_children[1].as_label_list(lemmas)
            else:
                label_list += rel[0].as_label_list(lemmas)
                label_list += entity.as_label_list(lemmas)
        else:
            for i in range(len(entities)):
                label_list += entities[i].as_label_list(lemmas)
                if len(rel) > i:
                    label_list += rel[i].as_label_list(lemmas)
        return label_list

    # override
    def as_text(self, lemmas=False):
        if lemmas:
            words = [leaf.token.lemma for leaf in self.natural_leaf_sequence()]
        else:
            words = [leaf.token.word for leaf in self.natural_leaf_sequence()]
        words = [word for word in words if len(word) > 0]
        return ' '.join(words)

    def get_child(self, i):
        return self.tree.get(self.children_ids[i])

    def children(self):
        return [self.tree.get(child_id) for child_id in self.children_ids]

    def set_child(self, i, elem_id):
        self.children_ids[i] = elem_id

    def new_layer(self):
        if self.layer_id >= 0:
            self.layer_ids.append(self.layer_id)
            self.layer_id = -1

    def apply_layer(self, entity, layer):
        if layer.is_node():
            for i in range(len(layer.children_ids)):
                if layer.children_ids[i] < 0:
                    child = self.tree.create_node(entity.children_ids)
                    child_id = child.id
                    if child.is_singleton():
                        child_id = child.children_ids[0]
                    layer.set_child(i, child_id)
                    return True
                if self.apply_layer(entity, layer.get_child(i)):
                    return True
        return False

    def apply_layers(self):
        self.layer_ids.reverse()
        prev_layer = self
        for layer_id in self.layer_ids:
            layer = self.tree.get(layer_id)
            self.apply_layer(prev_layer, layer)
            prev_layer = layer
        self.children_ids = prev_layer.children_ids
        self.layer_ids = []

    def is_singleton(self):
        return len(self.children_ids) == 1

    def is_node_singleton(self):
        if not self.is_singleton():
            return False
        child = self.tree.get(self.children_ids[0])
        return child.is_node()

    @property
    def compound(self):
        return self.__compound

    @compound.setter
    def compound(self, compound):
        self.__compound = compound
        for child in self.children():
            if child.is_node():
                child.compound = compound

    # override
    def is_compound(self):
        return self.__compound

    # override
    def add_child(self, elem_id):
        self.children_ids.append(elem_id)

    # override
    def add_to_first_child(self, elem_id, pos):
        if len(self.children_ids) > 0:
            if self.get_child(0).is_terminal():
                self.set_child(0, self.tree.enclose(self.get_child(0)).id)
            if pos == Position.RIGHT:
                self.get_child(0).children_ids.append(elem_id)
            else:
                self.get_child(0).children_ids.insert(0, elem_id)
        else:
            raise IndexError('Requesting root on an empty Node')
        return self

    # override
    def nest(self, elem_id):
        elem = self.tree.get(elem_id)
        if elem.is_leaf():
            rel = elem_id
            rest = []
        else:
            rel = elem.children_ids[0]
            rest = elem.children_ids[1:]
        self.layer_id = self.tree.create_node([rel, -1] + rest).id
        return self

    # override
    def remove_redundant_nesting(self):
        for child_id in self.children_ids:
            child = self.tree.get(child_id)
            child.remove_redundant_nesting()
        if self.is_node_singleton():
            self.tree.disenclose(self)

    # override
    def has_pos(self, pos, shallow=False):
        for child in self.children():
            if (not shallow) or child.is_leaf():
                if child.has_pos(pos):
                    return True
        return False

    # override
    def has_dep(self, dep, shallow=False):
        for child in self.children():
            if (not shallow) or child.is_leaf():
                if child.has_dep(dep):
                    return True
        return False

    # override
    def has_dep_in(self, deps, shallow=False):
        for child in self.children():
            if (not shallow) or child.is_leaf():
                if child.has_dep_in(deps):
                    return True
        return False

    # override
    def arity(self):
        if self.compound:
            return 1
        else:
            return len(self.children_ids)

    # override
    def flatten(self):
        new_children_ids = []
        for child_id in self.children_ids:
            child = self.tree.get(child_id)
            child.flatten()
            if child.is_terminal():
                new_children_ids.append(child.id)
            else:
                new_children_ids += child.children_ids
        self.children_ids = new_children_ids

    # override
    def flat_leafs(self):
        leafs = []
        for child in self.children():
            leafs += child.flat_leafs()
        return leafs

    # override
    def to_leafs(self):
        leafs = []
        for child in self.children():
            leafs += child.to_leafs()
        return leafs

    # replicate sequence in original sentence
    def natural_leaf_sequence(self):
        leafs = self.to_leafs()
        leafs.sort(key=lambda x: x.token.position_in_sentence)
        return leafs

    # override
    def to_hyperedge(self, with_namespaces=True):
        if self.compound:
            words = [leaf.token.word for leaf in self.natural_leaf_sequence()]
            if not with_namespaces:
                s = sym.str2symbol('_'.join(words))
            else:
                if not self.namespace:
                    self.generate_namespace()
                s = sym.build('_'.join(words), self.namespace)
            if self.connector:
                s = '+%s' % s
            return s
        else:
            return tuple([child.to_hyperedge(with_namespaces=with_namespaces) for child in self.children()])

    # override
    def to_synonym(self):
        if self.compound and ((not self.namespace) or '+' in self.namespace):
            edge = ['+/gb']
            for child in self.children():
                edge.append(child.to_hyperedge())
            return edge
        return None

    # override
    def generate_namespace(self):
        self.namespace = '+'.join([child.get_namespace() for child in self.children()])

    # override
    def get_inner_nested_node(self):
        if self.inner_nested_node_id >= 0:
            return self.tree.get(self.inner_nested_node_id)
        else:
            return self

    def set_inner_nested_node(self, elem_id, first=True):
        if self.inner_nested_node_id >= 0 or first:
            self.inner_nested_node_id = elem_id
            if self.get_child(1).is_node():
                self.get_child(1).set_inner_nested_node(elem_id, False)

    def all_tokens(self):
        tokens = []
        for child in self.children():
            tokens = tokens + child.all_tokens()
        return tokens

    def apply_(self, child_id, pos):
        self.children_ids.append(child_id)

    def nest_(self, child_id, pos):
        enclose = True
        if pos == Position.LEFT:
            node = self
        else:
            node = self.get_inner_nested_node()
            if node.is_node():
                inner_rel = node.get_child(0)
                if inner_rel.is_leaf() and not inner_rel.connector and len(node.children_ids) > 1:
                    inner_node = self.tree.create_node(node.children_ids[1:])
                    self.set_inner_nested_node(inner_node.id)
                    node.children_ids = [node.children_ids[0], inner_node.id]
                    node = inner_node
                    # placeholder
                    node.children_ids.insert(0, -1)
                    enclose = False

        if enclose:
            if node.is_leaf():
                node = self.tree.enclose(node)
            if len(node.children_ids) > 1:
                node = self.tree.enclose(node)
                # placeholder
                node.children_ids.insert(0, -1)
                node.set_inner_nested_node(node.get_child(1).get_inner_nested_node().id)
            else:
                # placeholder
                node.children_ids.insert(0, -1)
                node.set_inner_nested_node(node.children_ids[1])

        child = self.tree.get(child_id)
        if child.is_leaf():
            node.children_ids[0] = child_id
        else:
            node.children_ids[0] = child.children_ids[0]
            for cid in child.children_ids[1:]:
                node.children_ids.append(cid)

    def nest_deep(self, child_id, pos):
        child = self.tree.get(child_id)
        if child.is_leaf():
            self.nest_(child_id, pos)
            return

        parent_id = self.id
        if pos == Position.LEFT:
            parent = self
            for i in range(len(child.children_ids)):
                index = -i - 1
                parent.nest_(child.children_ids[index], pos)
                parent = self.tree.get(parent_id)
        else:
            parent = self.get_inner_nested_node()
            for i in range(len(child.children_ids)):
                parent.nest_(child.children_ids[i], pos)
                parent = self.tree.get(parent_id)
                parent = parent.get_inner_nested_node()

    def __eq__(self, other):
        if isinstance(other, Leaf):
            return False
        elif isinstance(other, Node):
            c1 = self.children()
            c2 = other.children()
            if len(c1) != len(c2):
                return False
            for i in range(len(c1)):
                if c1[i] != c2[i]:
                    return False
            return True
        return NotImplemented

    def __str__(self):
        strs = [str(self.tree.get(child_id)) for child_id in self.children_ids]
        if self.compound:
            s = '_'.join(strs)
            if self.connector:
                s = '+%s' % s
            return s
        else:
            return '(%s)' % ' '.join(strs)
