from __future__ import annotations

import ast
import logging
from functools import lru_cache

from spacy.language import Language
from spacy.lang.en import English
from spacy.tokens import Doc
from spacy_transformers import TransformerData
from spacy_transformers.pipeline_component import Transformer
from thinc.types import Ragged
from torch import Tensor
from torch.nn import CosineSimilarity
import torch.nn.functional as F

from graphbrain.hyperedge import Hyperedge, Atom
from graphbrain.hypergraph import Hypergraph
from graphbrain.semsim.matcher.matcher import SemSimMatcher, SemSimConfig

logger: logging.Logger = logging.getLogger(__name__)


class ContextEmbeddingMatcher(SemSimMatcher):
    _SPACY_PIPE_TRF_COMPONENT_NAME: str = 'transformer'
    _SPACY_DOC_CACHE_SIZE: int = 64
    _EMBEDDING_CACHE_SIZE: int = 64

    def __init__(self, config: SemSimConfig):
        super().__init__(config=config)
        self._spacy_pipe: Language = self._create_spacy_pipeline(config.model_name)
        self._cos_sim: CosineSimilarity = CosineSimilarity()

    def _create_spacy_pipeline(self, model_name: str) -> Language:
        logger.info("Creating SpaCy transformer pipeline...")
        nlp: Language = English()
        config = {
            "model": {
                "@architectures": "spacy-transformers.TransformerModel.v3",
                "name": model_name,
                "tokenizer_config": {"use_fast": True},
            }
        }
        # noinspection PyTypeChecker
        trf: Transformer = nlp.add_pipe(self._SPACY_PIPE_TRF_COMPONENT_NAME, config=config)
        trf.model.initialize()
        logger.info("Done creating SpaCy transformer pipeline!")
        return nlp

    # def _similarities(
    #         self,
    #         candidate_edge: Hyperedge = None,
    #         candidate_tok_pos: dict[int, Hyperedge] = None,
    #         ref_edges: list[Hyperedge] = None,
    #         ref_tok_poses: list[dict[int, Hyperedge]] = None,
    #         hg: Hypergraph = None
    # ) -> dict[str, float] | None:
    #     assert root_edge, f"Missing root edge!"
    #
    #     can_tok_idx: int | None = _get_and_validate_tok_idx(tok_pos)
    #     if can_tok_idx is None:
    #         return None
    #
    #     candidate_embedding: Tensor = self._get_candidate_embedding(candidate, can_tok_idx, root_edge, hg)
    #     reference_embeddings: list[Tensor] = self._get_reference_embeddings(references, can_tok_idx, root_edge, hg)
    #
    #     if candidate_embedding is not None and reference_embeddings is not None:
    #         return {ref: float(self._cos_sim(candidate_embedding, reference_trf_embedding))
    #                 for ref, reference_trf_embedding in zip(references, reference_embeddings)}
    #
    #     return None

    # def _get_candidate_embedding(
    #         self,
    #         candidate: str,
    #         can_tok_idx: int,
    #         root_edge: Hyperedge,
    #         hg: Hypergraph,
    # ) -> Tensor | None:
    #     root_edge_tokens: list[str] | None = _get_and_validate_edge_tokens(root_edge, hg)
    #     if not root_edge_tokens or not _validate_candidate_token(candidate, can_tok_idx, root_edge_tokens):
    #         return None
    #
    #     return self._get_embedding(tuple(root_edge_tokens), can_tok_idx)
    #
    # def _get_reference_embeddings(
    #         self, ref_edges: list[Hyperedge], tok_idx: int, root_edge: Hyperedge, hg: Hypergraph
    # ) -> list[Tensor] | None:
    #     root_edge_tok_pos: Hyperedge = _get_and_validate_edge_tok_pos(root_edge, hg)
    #
    #     reference_trf_embeddings: list[Tensor] = []
    #     for ref_edge in ref_edges:
    #         ref_edge_tok_pos: Hyperedge = _get_and_validate_edge_tok_pos(ref_edge, hg)
    #         ref_edge_tokens: list[str] = _get_and_validate_edge_tokens(ref_edge, hg)
    #
    #         tok_idx_trail: list[int] = _recursive_tok_idx_search(tok_idx, root_edge_tok_pos)
    #         if not tok_idx_trail:
    #             logger.error(f"Could not find tok_idx trail for ref_edge: {ref_edge}")
    #             return None
    #
    #         ref_edge_tok_idx: int | None = _follow_tok_idx_trail(ref_edge_tok_pos, tok_idx_trail)
    #         if ref_edge_tok_idx is None:
    #             logger.error(f"Could not find tok_idx for ref_edge: {ref_edge}")
    #             return None
    #
    #         if not _validate_tok_idx_for_tokens(ref_edge_tok_idx, ref_edge_tokens):
    #             logger.error(f"Reference sentence does not match tok_idx")
    #             return None
    #
    #         reference_trf_embeddings.append(self._get_embedding(tuple(ref_edge_tokens), ref_edge_tok_idx))
    #     return reference_trf_embeddings
    #
    # @lru_cache(maxsize=_EMBEDDING_CACHE_SIZE)
    # def _get_embedding(self, tokens: tuple[str], tok_idx: int) -> Tensor | None:
    #     spacy_doc, spacy_tokens = self._get_spacy_doc_and_tokens(tokens)
    #     if not _validate_spacy_tokenization(tokens, spacy_tokens):
    #         return None
    #
    #     return _get_trf_embedding_of_lex_token(spacy_tokens, tok_idx, spacy_doc._.trf_data)
    #
    # def _get_spacy_doc_and_tokens(self, tokens: tuple[str] = None) -> tuple[Doc, tuple[str]]:
    #     spacy_doc: Doc = Doc(self._spacy_pipe.vocab, words=tokens)
    #     self._spacy_pipe.get_pipe(self._SPACY_PIPE_TRF_COMPONENT_NAME)(spacy_doc)
    #     assert spacy_doc._.trf_data is not None, f"Missing trf_data"  # noqa
    #     return spacy_doc, tuple(tok.text for tok in spacy_doc)

    # build the tok_idxes list based on the tok_pos(es)
        # do a depths first search and collect all the tok_idxes
        # filter out -1 (no corresponding token)
        # sort ascending

    # get the tokens for the candidate and ref edges

    # validate that greatest tok_idx is smaller than length of tokens

    # get the candidate embedding for each semsim_ctx_idx

    # for each semsim_ctx_idx take the corresponding tok_idx_list

    def _similarities(
            self,
            # candidate_edge: Hyperedge = None,
            # candidate_tok_pos: dict[int, Hyperedge] = None,
            # ref_edges: list[Hyperedge] = None,
            # refs_tok_poses: list[dict[int, Hyperedge]] = None,
            cand_edge: Hyperedge = None,
            cand_tok_pos: Hyperedge = None,
            ref_edges: list[Hyperedge] = None,
            ref_tok_poses: list[Hyperedge] = None,
            hg: Hypergraph = None,
            **kwargs
    ) -> dict[str, float] | None:
        assert cand_edge and cand_tok_pos and ref_edges and ref_tok_poses and hg, (
            f"Missing argument(s): {cand_edge=} {cand_tok_pos=} {ref_edges=} {ref_tok_poses=} {hg=}"
        )

        # cand_tok_idxes_map: dict[int, tuple[int]] = _get_tok_idxes_map(candidate_tok_pos)
        # ref_tok_idxes_maps: list[dict[int, tuple[int]]] = [
        #     _get_tok_idxes_map(ref_tok_pos) for ref_tok_pos in refs_tok_poses
        # ]
        #
        # cand_tokens: tuple[str] = _get_and_validate_tokens(candidate_edge, cand_tok_idxes_map, hg)
        # refs_tokens: list[tuple[str]] = [
        #     _get_and_validate_tokens(ref_edge, ref_tok_idxes_map, hg)
        #     for ref_edge, ref_tok_idxes_map in zip(ref_edges, ref_tok_idxes_maps)
        # ]
        #
        # if not cand_tokens or not all(refs_tokens):
        #     return None
        #
        # cand_embeddings: dict[int, Tensor] = {
        #     semsim_ctx_idx: self._get_embedding(cand_tokens, cand_tok_idxes)
        #     for semsim_ctx_idx, cand_tok_idxes in cand_tok_idxes_map.items()
        # }
        #
        # refs_embeddings: list[dict[int, Tensor]] = [
        #     {
        #         semsim_ctx_idx: self._get_embedding(ref_tokens, ref_tok_idxes)
        #         for semsim_ctx_idx, ref_tok_idxes in ref_tok_idxes_map.items()
        #     }
        #     for ref_tokens, ref_tok_idxes_map in zip(refs_tokens, ref_tok_idxes_maps)
        # ]

        cand_tok_idxes: tuple[int] = _get_tok_idxes_sorted(cand_tok_pos)
        refs_tok_idxes: list[tuple[int]] = [_get_tok_idxes_sorted(ref_tok_pos) for ref_tok_pos in ref_tok_poses]

        cand_tokens: tuple[str] = _get_and_validate_tokens(cand_edge, cand_tok_idxes, hg)
        refs_tokens: list[tuple[str]] = [
            _get_and_validate_tokens(ref_edge, ref_tok_idxes, hg)
            for ref_edge, ref_tok_idxes in zip(ref_edges, refs_tok_idxes)
        ]

        if not cand_tokens or not all(refs_tokens):
            return None

        cand_embedding: Tensor | None = self._get_embedding(cand_tokens, cand_tok_idxes)
        refs_embeddings: list[Tensor | None] = [
            self._get_embedding(ref_tokens, ref_tok_idxes)
            for ref_tokens, ref_tok_idxes in zip(refs_tokens, refs_tok_idxes)
        ]

        if cand_embedding is None or not any(ref_embedding is not None for ref_embedding in refs_embeddings):
            return None

        return {
            ref_edge: float(self._cos_sim(cand_embedding, ref_embedding))
            for ref_edge, ref_embedding in zip(ref_edges, refs_embeddings)
        }

    @lru_cache(maxsize=_EMBEDDING_CACHE_SIZE)
    def _get_embedding(self, tokens: tuple[str], tok_idxes: tuple[int]) -> Tensor | None:
        spacy_doc, spacy_tokens = self._get_spacy_doc_and_tokens(tokens)
        if not _validate_spacy_tokenization(tokens, spacy_tokens):
            return None

        return _get_trf_embedding_of_lex_tokens(spacy_tokens, tok_idxes, spacy_doc._.trf_data)

    @lru_cache(maxsize=_SPACY_DOC_CACHE_SIZE)
    def _get_spacy_doc_and_tokens(self, tokens: tuple[str] = None) -> tuple[Doc, tuple[str]]:
        spacy_doc: Doc = Doc(self._spacy_pipe.vocab, words=tokens)
        self._spacy_pipe.get_pipe(self._SPACY_PIPE_TRF_COMPONENT_NAME)(spacy_doc)
        assert spacy_doc._.trf_data is not None, f"Missing trf_data"  # noqa
        return spacy_doc, tuple(tok.text for tok in spacy_doc)


def _get_trf_embedding_of_lex_tokens(lexical_tokens: tuple[str], lex_tok_idxes: tuple[int], trf_data: TransformerData):
    trf_tok_idxes: tuple[int] = _get_trf_tok_idxes(len(lexical_tokens), lex_tok_idxes, trf_data.align)

    lex_tokens: list[str] = [lexical_tokens[tok_idx] for tok_idx in lex_tok_idxes]
    trf_tokens: list[str] = [trf_data.wordpieces.strings[0][tok_idx] for tok_idx in trf_tok_idxes]
    logger.debug(f"Lexical tokens: {lex_tokens}, Transformer tokens: {trf_tokens}")

    trf_embeddings: Tensor = Tensor(trf_data.model_output.last_hidden_state[:, trf_tok_idxes, :])
    return _average_pool(trf_embeddings, Tensor(trf_data.wordpieces.attention_mask[:, trf_tok_idxes]))


def _get_trf_tok_idxes(n_lex_tokens: int, lex_tok_idxes: tuple[int], alignment_data: Ragged):
    lex2trf_idx: dict[int, list[int]] = _get_lex2trf_tok_idx(n_lex_tokens, alignment_data)
    tok_trf_idxes: list[int] = []
    for lex_tok_idx in lex_tok_idxes:
        trf_tok_idxes_: list[int] = lex2trf_idx[lex_tok_idx]
        for trf_idx in trf_tok_idxes_:
            if trf_idx not in tok_trf_idxes:
                tok_trf_idxes.append(trf_idx)
    return tuple(tok_trf_idxes)


# Make alignment between lexical tokens and transformer (sentencepiece, wordpiece, ...) tokens
def _get_lex2trf_tok_idx(n_lex_toks: int, alignment_data: Ragged) -> dict[int, list[int]]:
    lex2trf_idx: dict[int, list[int]] = {}
    trf_idx: int = 0
    for lex_idx in range(n_lex_toks):
        trf_token_length: int = alignment_data.lengths[lex_idx]
        lex2trf_idx[lex_idx] = list(alignment_data.dataXd[trf_idx:trf_idx + trf_token_length])
        trf_idx += trf_token_length
    return lex2trf_idx


# Adapted from: https://huggingface.co/intfloat/e5-base
def _average_pool(last_hidden_states: Tensor, attention_mask: Tensor, normalize: bool = False) -> Tensor:
    last_hidden: Tensor = last_hidden_states.masked_fill(~attention_mask[..., None].bool(), 0.0)
    embeddings: Tensor = last_hidden.sum(dim=1) / attention_mask.sum(dim=1)[..., None]
    if normalize:
        embeddings = F.normalize(embeddings, p=2)
    return embeddings


def _validate_spacy_tokenization(tokens: tuple[str], spacy_tokens: tuple[str]) -> bool:
    try:
        assert spacy_tokens == tokens
    except AssertionError:
        logger.warning(f"Spacy lexical tokenization does not equal given tokens: {spacy_tokens=} != {tokens=}")
        return False
    return True


def _get_and_validate_tokens(
        edge: Hyperedge, tok_idxes: tuple[int], hg: Hypergraph
) -> tuple[str] | None:
    tokens: tuple[str] = _get_and_validate_edge_tokens(edge, hg)
    if not tokens:
        return None

    if not _validate_tok_idxes_for_tokens(tok_idxes, tokens):
        return None

    return tokens


# def _get_and_validate_tokens(
#         edge: Hyperedge, tok_idxes_maps: list[dict[int, tuple[int]]] | dict[int, tuple[int]], hg: Hypergraph
# ) -> tuple[str] | None:
#     if isinstance(tok_idxes_maps, dict):
#         tok_idxes_maps = [tok_idxes_maps]
#
#     tokens: tuple[str] = _get_and_validate_edge_tokens(edge, hg)
#     if not tokens:
#         return None
#
#     for tok_idxes_map in tok_idxes_maps:
#         for tok_idxes in tok_idxes_map.values():
#             if not _validate_tok_idxes_for_tokens(tok_idxes, tokens):
#                 return None
#
#     return tokens


def _get_and_validate_edge_tokens(edge: Hyperedge, hg: Hypergraph) -> tuple[str] | None:
    tokens_str: str = hg.get_str_attribute(edge, 'tokens')
    if not tokens_str:
        return None
    try:
        tokens: list[str] = ast.literal_eval(tokens_str)
    except ValueError:
        logger.error(f"Edge has invalid 'tokens' attribute: {tokens_str}")
        return None
    return tuple(tokens)


def _validate_tok_idxes_for_tokens(tok_idxes: tuple[int], tokens: tuple[str]) -> bool:
    out_of_range_tok_idxes: list[int] = [tok_idx for tok_idx in tok_idxes if tok_idx >= len(tokens)]
    try:
        assert not out_of_range_tok_idxes
    except AssertionError:
        logger.warning(
            f"Token idxes are greater then length of tokens: {out_of_range_tok_idxes=}, {tokens=} (len={len(tokens)})"
        )
        return False
    return True


def _get_tok_idxes_map(tok_pos: dict[int, Hyperedge]) -> dict[int, tuple[int]]:
    return {semsim_ctx_idx: tuple(sorted(_get_tok_idxes(tok_pos))) for semsim_ctx_idx, tok_pos in tok_pos.items()}


def _get_tok_idxes_sorted(tok_pos: Hyperedge) -> tuple[int]:
    return tuple(sorted(_get_tok_idxes(tok_pos)))


def _get_tok_idxes(tok_pos: Hyperedge) -> list[int]:
    # list of atoms in tok_pos (possibly with duplicates)
    tok_pos_atoms: list[Atom] = tok_pos.all_atoms()

    tok_idxes: list[int] = []
    for tok_pos_atom in tok_pos_atoms:
        tok_idx: int | None = _get_and_validate_tok_idx(tok_pos_atom)
        if tok_idx is not None:
            tok_idxes.append(tok_idx)
    return tok_idxes


def _get_and_validate_tok_idx(tok_pos: Hyperedge) -> int | None:
    try:
        assert tok_pos.is_atom()
    except AssertionError:
        logger.error(f"Passed tok_pos is not an atom: {tok_pos=}")
        return None

    try:
        tok_idx_str: str = tok_pos[0]
    except IndexError:
        logger.error(f"Cannot access tok_pos string value: {tok_pos=}")
        return None

    try:
        tok_idx: int = int(tok_idx_str)
    except (TypeError, ValueError):
        logger.error(f"Cannot convert tok_pos to int value: {tok_pos=}")
        return None

    if tok_idx < 0:
        logger.debug(f"No corresponding token for tok_pos: {tok_pos=}")
        return None

    return tok_idx


# def _validate_candidate_token(candidate: str, tok_idx: int, root_edge_tokens: list[str]) -> bool:
#     if not _validate_tok_idx_for_tokens(tok_idx, root_edge_tokens):
#         return False
#
#     # check if corresponding token equals the given candidate str
#     candidate_token: str = root_edge_tokens[tok_idx]
#     try:
#         assert candidate.lower() == candidate_token.lower()
#     except AssertionError:
#         logger.warning(
#             f"Token in root edge does not equal candidate: '{candidate_token}' (idx: {tok_idx}) != '{candidate}."
#             f"Root edge tokens: {root_edge_tokens}"
#         )
#         return False
#
#     return True


# def _recursive_tok_idx_search(
#         tok_idx: int,
#         tok_pos: Hyperedge,
#         idx_trail: list[int] = None
# ) -> list[int] | None:
#     if not idx_trail:
#         idx_trail = []
#     if tok_pos.atom:
#         if tok_pos[0] == str(tok_idx):
#             return idx_trail
#         return None
#     for sub_tok_pos_idx, sub_tok_pos in enumerate(tok_pos):
#         if sub_idx_trail := _recursive_tok_idx_search(tok_idx, sub_tok_pos, [sub_tok_pos_idx]):
#             return idx_trail + sub_idx_trail
#     return None
#
#
# def _follow_tok_idx_trail(tok_pos: Hyperedge, tok_idx_trail: list[int]) -> int | None:
#     sub_tok_pos: Hyperedge | Atom = tok_pos
#     for sub_tok_idx in tok_idx_trail:
#         if len(sub_tok_pos) < sub_tok_idx:
#             logger.error(f"tok_idx_trail does not match tok_pos: {tok_idx_trail=}, {tok_pos=}")
#             return None
#
#         if sub_tok_pos.is_atom():
#             logger.debug(f"Reached atom in tok_pos: {tok_pos=}, {tok_idx_trail=}, {sub_tok_pos=}")
#             break
#
#         sub_tok_pos: Hyperedge = sub_tok_pos[sub_tok_idx]
#
#     return _get_and_validate_tok_idx(sub_tok_pos)
