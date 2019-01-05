from graphbrain.parsers.semantic_tree import Position
from graphbrain.parsers.predicates import Predicates


def is_part_of_rel(rel, part):
    rel_elems = rel.flat_leafs()
    part_elems = part.flat_leafs()

    for relem in rel_elems:
        for pelem in part_elems:
            if relem == pelem:
                return True

    return False


class Shallow(object):
    def __init__(self, output, lang):
        self.output = output
        self.visited = set()
        self.pred = Predicates(lang=lang)

    def is_directly_under(self, node, parent, child):
        if node.arity() < 2:
            return False

        rel = self.output.tree.get(node.children_ids[0])
        if is_part_of_rel(rel, parent):
            for i in range(1, len(node.children_ids)):
                elem = self.output.tree.get(node.children_ids[i])
                if elem == child:
                    return True

        for i in range(1, len(node.children_ids)):
            elem = self.output.tree.get(node.children_ids[i])
            if self.is_directly_under(elem, parent, child):
                return True

        return False

    def node_fit(self, node, original):
        fit = 0
        if node.is_node() and self.pred.is_predicate(node.children()[0], node):
            rel = self.output.tree.get(node.children_ids[0])
            if rel.arity() == 1 and (len(node.children_ids) == 2):
                fit += 100000
            elif rel.arity() == (len(node.children_ids) - 2):
                fit += 100000

            if rel.is_not_terminal():
                count = min(rel.arity(), node.arity() - 2)
                for i in range(count):
                    rel_part = rel.get_child(i)
                    param1 = node.get_child(i + 1)
                    if i == 0:
                        if self.is_directly_under(original, rel_part, param1):
                            fit += 100
                    param2 = node.get_child(i + 2)
                    if self.is_directly_under(original, rel_part, param2):
                        fit += 100

        # print('node_fit => %s [%s]' % (str(node), fit))
        return fit

    def find_candidates(self, original_node):
        if original_node.is_leaf():
            return [original_node]

        # initialize node, to_process and candidates
        to_process = original_node.children_ids[1:]
        new_node = self.output.tree.create_node(original_node.children_ids[:1])
        candidates = []
        return self.find_candidates_r(original_node, new_node, to_process, candidates)

    def build_candidate(self, original_node, working_node, rel_id, child, to_process, candidates):
        position = '%s %s' % (str(original_node), str(self.output.tree.get(rel_id)))
        if position in self.visited:
            return
        self.visited.add(position)
        new_node = working_node.clone()
        new_node.children_ids[0] = rel_id
        new_node.children_ids += child.children_ids[1:]
        self.find_candidates_r(original_node, new_node, to_process[1:], candidates)

    def find_candidates_r(self, original_node, working_node, to_process, candidates):
        if len(to_process) > 0:
            child_id = to_process[0]
            child = self.output.tree.get(child_id)
            child_candidates = self.find_candidates(child)

            for child in child_candidates:
                if child.is_node() and self.pred.is_predicate(child.children()[0], child):
                    # generate several possibilities for relationships

                    rel_left = self.output.tree.get(working_node.children_ids[0])
                    rel_right = self.output.tree.get(child.children_ids[0])

                    # insert before
                    # (r1 a1 ... (r2 b1 ...)) -> ((r2 r1) a1 ... b1 ...)
                    # ((r1 ...) a1 ... (r2 b1 ...)) -> ((r2 r1 ...) a1 ... b1 ...)
                    if len(working_node.children_ids) == 1:
                        rel = self.output.tree.create_node([working_node.children_ids[0]])
                        rel.children_ids.insert(0, child.children_ids[0])
                        self.build_candidate(original_node, working_node, rel.id, child, to_process, candidates)

                    # merge at the center
                    # (r1 a1 ... (r2 b1 ...)) -> (r1_r2 a1 ... b1 ...)
                    # ((r1 r2) a1 ... ((r3 r4) b1 ...)) -> ((r1 r2_r3 r4) a1 ... b1 ...)
                    # (r1 a1 ... ((r2 r3) b1 ...)) -> ((r1_r2 r3 r4) a1 ... b1 ...)
                    # ((r1 r2) a1 ... (r3 b1 ...)) -> ((r1 r2_r3) a1 ... b1 ...)
                    if rel_left.is_terminal():
                        rel_left_a = []
                        rel_left_b = rel_left.id
                    else:
                        rel_left_a = rel_left.children_ids[:-1]
                        rel_left_b = rel_left.children_ids[-1]
                    if rel_right.is_terminal():
                        rel_right_a = rel_right.id
                        rel_right_b = []
                    else:
                        rel_right_a = rel_right.children_ids[0]
                        rel_right_b = rel_right.children_ids[1:]

                    center_rel_node = self.output.tree.create_node([rel_left_b, rel_right_a])
                    center_rel_node.compound = True

                    if rel_left.is_terminal() and rel_right.is_terminal():
                        rel = center_rel_node
                    else:
                        rel_ids = rel_left_a + [center_rel_node.id] + rel_right_b
                        rel = self.output.tree.create_node(rel_ids)

                    self.build_candidate(original_node, working_node, rel.id, child, to_process, candidates)

                    # insert after
                    # (r1 a1 ... (r2 b1 ...)) -> ((r1 r2) a1 ... b1 ...)
                    # ((r1 ...) a1 ... (r2 b1 ...)) -> ((r1 ... r2) a1 ... b1 ...)
                    if rel_right.is_terminal():
                        new_node = working_node.clone()
                        rel_id = working_node.children_ids[0]
                        rel = self.output.tree.get(rel_id)
                        if not rel.is_terminal():
                            rel = rel.clone()
                        else:
                            rel = self.output.tree.create_node([rel.id])
                        new_node.children_ids[0] = rel.id
                        new_node.add_to_first_child(child.children_ids[0], Position.RIGHT)
                        rel_id = new_node.children_ids[0]
                        self.build_candidate(original_node, working_node, rel_id, child, to_process, candidates)
                # just append new child
                # (r a1 b) -> (r a1 b)
                new_node = working_node.clone()
                new_node.children_ids.append(child_id)
                self.find_candidates_r(original_node, new_node, to_process[1:], candidates)
        else:
            # nothing left to process, append working node
            # working_node.get_child(0).flatten()
            candidates.append(working_node)

        return candidates

    def process_entity(self, entity_id):
        entity = self.output.tree.get(entity_id)

        if not entity.is_terminal() and self.pred.is_predicate(entity.children()[0], entity):
            best_node = None
            best_fit = -1
            candidates = self.find_candidates(entity)
            for candidate in candidates:
                fit = self.node_fit(candidate, entity)
                # print('$ %s {%s}' % (candidate, fit))
                if fit > best_fit:
                    best_fit = fit
                    best_node = candidate
            return best_node.id

        return entity_id

    def process(self):
        self.visited = set()
        self.output.tree.root_id = self.process_entity(self.output.tree.root_id)
        self.output.tree.remove_redundant_nesting()
        return self.output
