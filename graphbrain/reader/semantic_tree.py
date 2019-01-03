from graphbrain.funs import *


LEAF = 0
NODE = 1


class Position(object):
    LEFT, RIGHT = range(2)


class Tree(object):
    def __init__(self, elem=None):
        self.root_id = None
        self.cur_id = 0
        self.table = {}
        self.token2leaf_id = {}
        if elem:
            self.root_id = elem.id
            self.import_element(elem)

    def clone(self):
        tree = Tree()
        tree.root_id = self.root_id
        tree.cur_id = self.cur_id
        for elem_id in self.table:
            elem = self.table[elem_id]
            if elem.is_leaf():
                new_elem = Leaf(elem.token)
                tree.token2leaf_id[elem.token] = elem_id
            else:
                new_elem = Node(elem.children_ids)
            new_elem.id = elem_id
            new_elem.tree = tree
            tree.table[elem_id] = new_elem
        return tree

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
        else:
            self.token2leaf_id[elem.token] = elem.id
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
        if elem.is_leaf():
            self.token2leaf_id[elem.token] = elem_id

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
        if node.is_node() and len(node.children_ids) == 1:
            node_id = node.id
            inner_entity = self.get(node.children_ids[0])
            self.set(node_id, inner_entity)
            return inner_entity
        return self

    def remove_redundant_nesting(self):
        self.root().remove_redundant_nesting()

    def clone_id(self, elem_id):
        elem = self.get(elem_id)
        return elem.clone().id

    def to_hyperedge(self, with_namespaces=True):
        return self.root().to_hyperedge(with_namespaces=with_namespaces)

    def to_hyperedge_str(self, with_namespaces=True):
        return edge2str(self.to_hyperedge(with_namespaces=with_namespaces))

    def token2leaf(self, token):
        return self.get(self.token2leaf_id[token])

    def __str__(self):
        return str(self.get(self.root_id))


class Element(object):
    def __init__(self):
        self.type = None
        self.id = None
        self.tree = None
        self.namespace = None
        self.position = None

    def as_label_list(self, lemmas=False):
        raise NotImplementedError()

    def as_text(self, lemmas=False):
        raise NotImplementedError()

    def is_leaf(self):
        return self.type == LEAF

    def is_node(self):
        return self.type == NODE

    def is_compound(self):
        return True

    def is_connector(self):
        return False

    def is_terminal(self):
        return self.is_leaf() or self.is_compound()

    def is_not_terminal(self):
        return not self.is_terminal()

    def get_namespace(self):
        if not self.namespace:
            self.generate_namespace()
        return self.namespace

    def is_compound_concept(self):
        return False

    def first_child(self):
        raise NotImplementedError()

    def add_to_first_child(self, elem_id, pos):
        raise NotImplementedError()

    def has_pos(self, pos, shallow=False):
        raise NotImplementedError()

    def has_dep(self, dep, shallow=False):
        raise NotImplementedError()

    def has_dep_in(self, deps, shallow=False):
        raise NotImplementedError()

    def arity(self):
        raise NotImplementedError()

    def flat_leafs(self):
        raise NotImplementedError()

    def to_leafs(self):
        raise NotImplementedError()

    def to_hyperedge(self, with_namespaces=True):
        raise NotImplementedError()

    def generate_namespace(self):
        raise NotImplementedError()

    def remove_redundant_nesting(self):
        raise NotImplementedError()

    def all_tokens(self):
        raise NotImplementedError()

    def insert(self, child_id, pos):
        raise NotImplementedError()

    def apply_head(self, child_id):
        raise NotImplementedError()

    def apply_tail(self, child_id):
        raise NotImplementedError()

    def reverse_apply(self, child_id):
        raise NotImplementedError()

    def nest(self, child_id):
        raise NotImplementedError()

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
        self.connector = False

    def copy(self, source):
        self.token = source.token
        self.namespace = source.namespace
        self.position = source.position
        self.connector = source.connector

    # Leaf is immutable
    def clone(self):
        return self

    # override
    def is_connector(self):
        return self.connector

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
    def first_child(self):
        return self

    # override
    def add_to_first_child(self, elem_id, pos):
        node = self.tree.enclose(self)
        return node.add_to_first_child(elem_id, pos)

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
            s = str2symbol(self.token.word)
        else:
            s = build_symbol(self.token.word, self.namespace)
        if self.connector:
            s = '+%s' % s
        return s

    # override
    def generate_namespace(self):
        self.namespace = 'nlp.%s.%s' % (self.token.lemma.lower(), self.token.pos.lower())

    # override
    def remove_redundant_nesting(self):
        return self

    # override
    def all_tokens(self):
        return [self.token]

    # override
    def insert(self, child_id, pos):
        node = self.tree.enclose(self)
        node.insert(child_id, pos)

    # override
    def apply_head(self, child_id):
        node = self.tree.enclose(self)
        node.apply_head(child_id)

    # override
    def apply_tail(self, child_id):
        node = self.tree.enclose(self)
        node.apply_tail(child_id)

    # override
    def reverse_apply(self, child_id):
        node = self.tree.enclose(self)
        node.reverse_apply(child_id)

    # override
    def nest(self, child_id):
        node = self.tree.enclose(self)
        node.nest(child_id)

    # override
    def __eq__(self, other):
        if isinstance(other, Leaf):
            return self.token == other.token
        elif isinstance(other, Node):
            return False
        return NotImplemented

    # override
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
        self.__compound = False

    def copy(self, source):
        self.__compound = source.__compound
        self.namespace = source.namespace
        self.position = source.position

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
    def first_child(self):
        return self.get_child(0)

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
        return tuple([child.to_hyperedge(with_namespaces=with_namespaces) for child in self.children()])

    # override
    def is_compound_concept(self):
        first = self.get_child(0)
        return first.is_connector()

    # override
    def generate_namespace(self):
        self.namespace = '+'.join([child.get_namespace() for child in self.children()])

    # override
    def remove_redundant_nesting(self):
        for child in self.children():
            child.remove_redundant_nesting()
        if len(self.children_ids) == 1:
            return self.tree.disenclose(self)
        return self

    # override
    def all_tokens(self):
        tokens = []
        for child in self.children():
            tokens = tokens + child.all_tokens()
        return tokens

    # override
    def insert(self, child_id, pos):
        if pos == Position.LEFT:
            self.children_ids.insert(0, child_id)
        elif pos == Position.RIGHT:
            self.children_ids.append(child_id)

    # hyperedge generator operations

    # override
    def apply_head(self, child_id):
        self.children_ids.insert(1, child_id)

    # override
    def apply_tail(self, child_id):
        self.children_ids.append(child_id)

    # override
    # (a b) <- (c d) => (c d (a b))
    def reverse_apply(self, child_id):
        child = self.tree.get(child_id)
        if child.is_leaf():
            new_children_ids = [child_id]
        else:
            new_children_ids = child.children_ids[:]

        if len(self.children_ids) == 1:
            new_parent_id = self.children_ids[0]
        else:
            new_parent_id = self.tree.create_node(self.children_ids).id

        new_children_ids.append(new_parent_id)
        self.children_ids = new_children_ids

    # override
    def nest(self, child_id):
        node = self

        if node.is_leaf():
            node = self.tree.enclose(node)
        if len(node.children_ids) > 1:
            node = self.tree.enclose(node)
        # placeholder
        node.children_ids.insert(0, -1)

        child = self.tree.get(child_id)
        if child.is_leaf():
            node.children_ids[0] = child_id
        else:
            node.children_ids[0] = child.children_ids[0]
            for cid in child.children_ids[1:]:
                node.children_ids.append(cid)

    # override
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

    # override
    def __str__(self):
        strs = [str(self.tree.get(child_id)) for child_id in self.children_ids]
        if self.compound:
            s = '_'.join(strs)
            return s
        else:
            return '(%s)' % ' '.join(strs)
