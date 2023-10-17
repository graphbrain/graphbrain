import itertools
from collections import Counter
from typing import List

from graphbrain import hedge
from graphbrain.hyperedge import Hyperedge
from graphbrain.patterns import is_fun_pattern, is_pattern
from graphbrain.patterns.atoms import _matches_atomic_pattern
from graphbrain.patterns.utils import _defun_pattern_argroles, _atoms_and_tok_pos
from graphbrain.patterns.variables import _remove_special_vars, _varname, _assign_edge_to_var, _regular_var_count, \
    _generate_special_var_name
from graphbrain.semsim import semsim
from graphbrain.utils.lemmas import lemma
from graphbrain.utils.semsim import get_edge_word_part, extract_pattern_words, extract_similarity_threshold, \
    replace_edge_word_part, SEMSIM_CTX_TOK_POS_VAR_CODE, SEMSIM_CTX_THRESHOLD_VAR_CODE


class Matcher:
    def __init__(self, edge, pattern, curvars=None, tok_pos=None, hg=None):
        self.hg = hg
        self.semsim_ctx = False
        self.results_with_special_vars = self._match(edge, pattern, curvars=curvars, tok_pos=tok_pos)
        self.results = [_remove_special_vars(result) for result in self.results_with_special_vars]

    def _match(self, edge, pattern, curvars=None, tok_pos=None):
        if curvars is None:
            curvars = {}

        # functional patterns
        if is_fun_pattern(pattern):
            return self._match_fun_pat(
                edge,
                pattern,
                curvars,
                tok_pos=tok_pos
            )

        # function pattern on edge can never match non-functional pattern
        if is_fun_pattern(edge):
            return []

        # atomic patterns
        if pattern.atom:
            if _matches_atomic_pattern(edge, pattern):
                variables = {}
                if is_pattern(pattern):
                    varname = _varname(pattern)
                    if len(varname) > 0:
                        # if varname in curvars and curvars[varname] != edge:
                        #     return []
                        variables[varname] = _assign_edge_to_var({**curvars, **variables}, varname, edge)[varname]
                return [{**curvars, **variables}]
            else:
                return []

        min_len = len(pattern)
        max_len = min_len
        # open-ended?
        if pattern[-1].to_str() == '...':
            pattern = hedge(pattern[:-1])
            min_len -= 1
            max_len = float('inf')

        result = [{}]
        argroles_posopt = _defun_pattern_argroles(pattern)[0].argroles().split('-')[0]
        if len(argroles_posopt) > 0 and argroles_posopt[0] == '{':
            match_by_order = False
            argroles_posopt = argroles_posopt[1:-1]
        else:
            match_by_order = True
        argroles = argroles_posopt.split(',')[0]
        argroles_opt = argroles_posopt.replace(',', '')

        if len(argroles) > 0:
            min_len = 1 + len(argroles)
            max_len = float('inf')
        else:
            match_by_order = True

        if len(edge) < min_len or len(edge) > max_len:
            return []

        # match by order
        if match_by_order:
            for i, pitem in enumerate(pattern):
                eitem = edge[i]
                _result = []

                for variables in result:
                    if pitem.atom:
                        varname = _varname(pitem)
                        if _matches_atomic_pattern(eitem, pitem):  # elif
                            if len(varname) > 0 and varname[0].isupper():
                                variables[varname] = _assign_edge_to_var(
                                    {**curvars, **variables}, varname, eitem)[varname]
                        else:
                            continue
                        _result.append(variables)
                    else:
                        tok_pos_item = None
                        if tok_pos is not None:
                            try:
                                assert len(tok_pos) > i
                            except AssertionError:
                                raise RuntimeError(f"Index '{i}' in tok_pos '{tok_pos}' is out of range")
                            tok_pos_item = tok_pos[i]
                        _result += self._match(
                            eitem,
                            pitem,
                            {**curvars, **variables},
                            tok_pos=tok_pos_item
                        )
                result = _result
        # match by argroles
        else:
            result = []
            # match connector first
            # TODO: avoid matching connector twice!
            ctok_pos = tok_pos[0] if tok_pos else None
            if self._match(edge[0], pattern[0], curvars, tok_pos=ctok_pos):
                role_counts = Counter(argroles_opt).most_common()
                unknown_roles = (len(pattern) - 1) - len(argroles_opt)
                if unknown_roles > 0:
                    role_counts.append(('*', unknown_roles))
                # add connector pseudo-argrole
                role_counts = [('X', 1)] + role_counts
                result = self._match_by_argroles(
                    edge,
                    pattern,
                    role_counts,
                    len(argroles),
                    curvars=curvars,
                    tok_pos=tok_pos
                )

        unique_vars = []
        for variables in result:
            v = {**curvars, **variables}
            if v not in unique_vars:
                unique_vars.append(v)
        return unique_vars

    def _match_by_argroles(
            self,
            edge,
            pattern,
            role_counts,
            min_vars,
            matched=(),
            curvars=None,
            tok_pos=None
    ):
        if curvars is None:
            curvars = {}

        if len(role_counts) == 0:
            return [curvars]

        argrole, n = role_counts[0]

        # match connector
        if argrole == 'X':
            eitems = [edge[0]]
            pitems = [pattern[0]]
        # match any argrole
        elif argrole == '*':
            eitems = [e for e in edge if e not in matched]
            pitems = pattern[-n:]
        # match specific argrole
        else:
            eitems = edge.edges_with_argrole(argrole)
            pitems = _defun_pattern_argroles(pattern).edges_with_argrole(argrole)

        if len(eitems) < n:
            if _regular_var_count(curvars) >= min_vars:
                return [curvars]
            else:
                return []

        result = []

        if tok_pos:
            tok_pos_items = [tok_pos[i] for i, subedge in enumerate(edge) if subedge in eitems]
            tok_pos_perms = tuple(itertools.permutations(tok_pos_items, r=n))

        for perm_n, perm in enumerate(tuple(itertools.permutations(eitems, r=n))):
            if tok_pos:
                tok_pos_perm = tok_pos_perms[perm_n]
            perm_result = [{}]
            for i, eitem in enumerate(perm):
                pitem = pitems[i]
                tok_pos_item = tok_pos_perm[i] if tok_pos else None
                item_result = []
                for variables in perm_result:
                    item_result += self._match(
                        eitem,
                        pitem,
                        {**curvars, **variables},
                        tok_pos=tok_pos_item
                    )
                perm_result = item_result
                if len(item_result) == 0:
                    break

            for variables in perm_result:
                result += self._match_by_argroles(
                    edge,
                    pattern,
                    role_counts[1:],
                    min_vars,
                    matched + perm,
                    {**curvars, **variables},
                    tok_pos=tok_pos
                )

        return result

    def _match_atoms(
            self,
            atom_patterns,
            atoms,
            curvars,
            atoms_tok_pos=None,
            matched_atoms=None
    ) -> list:
        if matched_atoms is None:
            matched_atoms = []

        if len(atom_patterns) == 0:
            return [curvars]

        results = []
        atom_pattern = atom_patterns[0]

        for atom_pos, atom in enumerate(atoms):
            if atom not in matched_atoms:
                tok_pos = atoms_tok_pos[atom_pos] if atoms_tok_pos else None
                svars = self._match(atom, atom_pattern, curvars, tok_pos=tok_pos)
                for variables in svars:
                    results += self._match_atoms(
                        atom_patterns[1:],
                        atoms,
                        {**curvars, **variables},
                        atoms_tok_pos=atoms_tok_pos,
                        matched_atoms=matched_atoms + [atom]
                    )

        return results

    # TODO: deal with argroles
    def _match_lemma(self, lemma_pattern, edge, curvars):
        if self.hg is None:
            raise RuntimeError('Lemma pattern function requires hypergraph.')

        if edge.not_atom:
            return []

        _lemma = lemma(self.hg, edge, same_if_none=True)

        # add argroles to _lemma if needed
        ar = edge.argroles()
        if ar != '':
            parts = _lemma.parts()
            parts[1] = '{}.{}'.format(parts[1], ar)
            _lemma = hedge('/'.join(parts))

        if _matches_atomic_pattern(_lemma, lemma_pattern):
            return [curvars]

        return []

    def _match_semsim(
            self,
            pattern: Hyperedge,
            edge: Hyperedge,
            curvars: dict,
    # ) -> list[dict]:
    ) -> List[dict]:
        edge_word_part: str = get_edge_word_part(edge)
        if not edge_word_part:
            return []

        # can be one word (e.g. "say") or a list of words (e.g. ["say, tell, speak"])
        pattern_words_part: str = pattern[0].parts()[0]
        reference_words: list[str] = extract_pattern_words(pattern_words_part)

        threshold: float | None = extract_similarity_threshold(pattern)
        if not semsim(
                semsim_type="FIX",
                threshold=threshold,
                cand_word=edge_word_part,
                ref_words=reference_words,
        ):
            return []

        edge_with_pattern_word_part = replace_edge_word_part(edge, pattern_words_part)
        if _matches_atomic_pattern(edge_with_pattern_word_part, pattern[0]):
            return [curvars]

        return []

    def _match_fun_pat(self, edge, fun_pattern, curvars, tok_pos=None) -> list:
        fun = fun_pattern[0].root()
        if fun == 'var':
            if len(fun_pattern) != 3:
                raise RuntimeError('var pattern function must have two arguments')
            pattern = fun_pattern[1]
            var_name = fun_pattern[2].root()
            if edge.not_atom and str(edge[0]) == 'var' and len(edge) == 3 and str(edge[2]) == var_name:
                this_var = _assign_edge_to_var(curvars, var_name, edge[1])
                return self._match(
                    edge[1],
                    pattern,
                    curvars={**curvars, **this_var},
                    tok_pos=tok_pos
                )
            else:
                this_var = _assign_edge_to_var(curvars, var_name, edge)
                return self._match(
                    edge,
                    pattern,
                    curvars={**curvars, **this_var},
                    tok_pos=tok_pos
                )
        elif fun == 'atoms':
            if tok_pos:
                atoms, atoms_tok_pos = _atoms_and_tok_pos(edge, tok_pos)
            else:
                atoms = edge.atoms()
                atoms_tok_pos = None
            atom_patterns = fun_pattern[1:]
            return self._match_atoms(
                atom_patterns,
                atoms,
                curvars,
                atoms_tok_pos=atoms_tok_pos
            )
        elif fun == 'lemma':
            return self._match_lemma(fun_pattern[1], edge, curvars)
        elif fun == 'semsim' or fun == 'semsim-fix':
            return self._match_semsim(
                fun_pattern[1:],
                edge,
                curvars,
                hg=self.hg,
            )
        elif fun == 'semsim-ctx':
            self.semsim_ctx = True
            threshold = extract_similarity_threshold(fun_pattern[1:])
            special_vars = {
                _generate_special_var_name(SEMSIM_CTX_TOK_POS_VAR_CODE, curvars): tok_pos,
                _generate_special_var_name(SEMSIM_CTX_THRESHOLD_VAR_CODE, curvars): threshold
            }
            return self._match(
                edge,
                fun_pattern[1],
                curvars={**curvars, **special_vars},
                tok_pos=tok_pos
            )
        elif fun == 'any':
            for pattern in fun_pattern[1:]:
                matches = self._match(
                    edge,
                    pattern,
                    curvars=curvars,
                    tok_pos=tok_pos
                )
                if len(matches) > 0:
                    return matches
            return []
        else:
            raise RuntimeError(f"Unknown pattern function: {fun}")