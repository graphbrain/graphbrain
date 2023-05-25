from __future__ import annotations

import ast
import logging
from functools import lru_cache
from typing import Callable, Any

from spacy.language import Language
from spacy.lang.en import English
from spacy.tokens import Doc
from spacy_transformers import TransformerData
from spacy_transformers.pipeline_component import Transformer
from thinc.types import Ragged
from torch import Tensor
from torch.nn import CosineSimilarity
import torch.nn.functional as F

from graphbrain.hyperedge import Hyperedge, hedge, Atom
from graphbrain.hypergraph import Hypergraph
from graphbrain.semsim.matching.matcher import SemSimMatcher, SemSimType, SemSimConfig

logger: logging.Logger = logging.getLogger(__name__)


class ContextEmbeddingMatcher(SemSimMatcher):
    _TYPE: SemSimType = SemSimType.CONTEXT
    _SPACY_PIPE_TRF_COMPONENT_NAME: str = 'transformer'
    _EMBEDDING_CACHE_SIZE: int = 64

    def __init__(self, config: SemSimConfig):
        super().__init__(config=config)
        self._spacy_pipe: Language = self._create_spacy_pipeline(config.model_name)
        self._cos_sim: CosineSimilarity = CosineSimilarity(dim=1)

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
        trf: Transformer = nlp.add_pipe(self._SPACY_PIPE_TRF_COMPONENT_NAME, config=config)  # noqa
        trf.model.initialize()
        logger.info("Done creating SpaCy transformer pipeline!")
        return nlp

    def _similarities(
            self,
            candidate: str,
            references: list[Hyperedge],
            root_edge: Hyperedge = None,
            tok_pos: Hyperedge = None,
            hg: Hypergraph = None,
            **kwargs
    ) -> dict[str, float] | None:
        assert root_edge, f"Missing root edge!"

        can_tok_idx: int | None = _get_and_validate_tok_idx(tok_pos)
        if can_tok_idx is None:
            return None

        candidate_embedding: Tensor = self._get_candidate_embedding(candidate, can_tok_idx, root_edge, hg)
        reference_embeddings: list[Tensor] = self._get_reference_embeddings(references, can_tok_idx, root_edge, hg)

        if candidate_embedding is not None and reference_embeddings is not None:
            return {ref: float(self._cos_sim(candidate_embedding, reference_trf_embedding))
                    for ref, reference_trf_embedding in zip(references, reference_embeddings)}

        return None

    def _get_candidate_embedding(
            self,
            candidate: str,
            can_tok_idx: int,
            root_edge: Hyperedge,
            hg: Hypergraph,
    ) -> Tensor | None:
        root_edge_tokens: list[str] | None = _get_and_validate_edge_tokens(root_edge, hg)
        if not root_edge_tokens or not _validate_candidate_token(candidate, can_tok_idx, root_edge_tokens):
            return None

        return self._get_embedding(tuple(root_edge_tokens), can_tok_idx)

    """
    Questions:
    * Is it possible to follow the tok_idx_trail for every ref edge?
        * Do we get root edges (candidate edges) that do not match the pattern? probably yes
    * Can we store/cache the tok_idx for a given ref_edge?
        * Is it always the same? should be --> refactor into new get_ref_edge_tok_idx method and add caching
            --> bullshit  
        * we need the tok_idx_trail
            * can cache follow trail method if tok_pos (Hyperedge) is hashable
    
    """

    def _get_reference_embeddings(
            self, ref_edges: list[Hyperedge], tok_idx: int, root_edge: Hyperedge, hg: Hypergraph
    ) -> list[Tensor] | None:
        root_edge_tok_pos: Hyperedge = _get_and_validate_edge_tok_pos(root_edge, hg)

        reference_trf_embeddings: list[Tensor] = []
        for ref_edge in ref_edges:
            ref_edge_tok_pos: Hyperedge = _get_and_validate_edge_tok_pos(ref_edge, hg)
            ref_edge_tokens: list[str] = _get_and_validate_edge_tokens(ref_edge, hg)

            tok_idx_trail: list[int] = _recursive_tok_idx_search(tok_idx, root_edge_tok_pos)
            if not tok_idx_trail:
                logger.error(f"Could not find tok_idx trail for ref_edge: {ref_edge}")
                return None

            ref_edge_tok_idx: int | None = _follow_tok_idx_trail(ref_edge_tok_pos, tok_idx_trail)
            if ref_edge_tok_idx is None:
                logger.error(f"Could not find tok_idx for ref_edge: {ref_edge}")
                return None

            if not _validate_tok_idx_for_tokens(ref_edge_tok_idx, ref_edge_tokens):
                logger.error(f"Reference sentence does not match tok_idx")
                return None

            reference_trf_embeddings.append(self._get_embedding(tuple(ref_edge_tokens), ref_edge_tok_idx))
        return reference_trf_embeddings

    @lru_cache(maxsize=_EMBEDDING_CACHE_SIZE)
    def _get_embedding(self, tokens: tuple[str], tok_idx: int) -> Tensor | None:
        spacy_doc, spacy_tokens = self._get_spacy_doc_and_tokens(tokens)
        if not _validate_spacy_tokenization(tokens, spacy_tokens):
            return None

        return _get_trf_embedding_of_lex_token(spacy_tokens, tok_idx, spacy_doc._.trf_data)

    def _get_spacy_doc_and_tokens(self, tokens: tuple[str] = None) -> tuple[Doc, tuple[str]]:
        spacy_doc: Doc = Doc(self._spacy_pipe.vocab, words=tokens)
        self._spacy_pipe.get_pipe(self._SPACY_PIPE_TRF_COMPONENT_NAME)(spacy_doc)
        assert spacy_doc._.trf_data is not None, f"Missing trf_data"  # noqa
        return spacy_doc, tuple(tok.text for tok in spacy_doc)


def _get_trf_embedding_of_lex_token(lexical_tokens: tuple[str], lex_tok_idx: int, trf_data: TransformerData):
    tok_trf_idxes: list[int] = _get_lex2trf_idx(lexical_tokens, trf_data.align)[lex_tok_idx]
    trf_tokens: list[str] = [trf_data.wordpieces.strings[0][tok_idx] for tok_idx in tok_trf_idxes]

    logger.debug(f"Lexical token: {lexical_tokens[lex_tok_idx]}, Transformer tokens: {trf_tokens}")

    trf_embeddings: Tensor = Tensor(trf_data.model_output.last_hidden_state[:, tok_trf_idxes, :])
    return _average_pool(trf_embeddings, Tensor(trf_data.wordpieces.attention_mask[:, tok_trf_idxes]))


def _validate_spacy_tokenization(tokens: tuple[str], spacy_tokens: tuple[str]) -> bool:
    try:
        assert spacy_tokens == tokens
    except AssertionError:
        logger.warning(f"Spacy lexical tokenization does not equal given tokens: {spacy_tokens=} != {tokens=}")
        return False
    return True


def _get_and_validate_edge_tok_pos(edge: Hyperedge, hg: Hypergraph) -> Hyperedge | None:
    return _get_and_validate_edge_str_attr(edge, hg, 'tok_pos', hedge)


def _get_and_validate_edge_tokens(edge: Hyperedge, hg: Hypergraph) -> list[str] | None:
    return _get_and_validate_edge_str_attr(edge, hg, 'tokens', ast.literal_eval)


def _get_and_validate_edge_str_attr(edge: Hyperedge, hg: Hypergraph, attribute_name: str, constructor: Callable) -> Any:
    attribute_str: str = hg.get_str_attribute(edge, attribute_name)
    if not attribute_str:
        return None
    try:
        attribute_val: Any = constructor(attribute_str)
    except ValueError:
        logger.error(f"Edge has invalid '{attribute_name}' attribute: {attribute_str}")
        return None
    return attribute_val


def _validate_candidate_token(candidate: str, tok_idx: int, root_edge_tokens: list[str]) -> bool:
    if not _validate_tok_idx_for_tokens(tok_idx, root_edge_tokens):
        return False

    # check if corresponding token equals the given candidate str
    candidate_token: str = root_edge_tokens[tok_idx]
    try:
        assert candidate.lower() == candidate_token.lower()
    except AssertionError:
        logger.warning(
            f"Token in root edge does not equal candidate: '{candidate_token}' (idx: {tok_idx}) != '{candidate}."
            f"Root edge tokens: {root_edge_tokens}"
        )
        return False

    return True


def _validate_tok_idx_for_tokens(tok_idx: int, tokens: list[str]) -> bool:
    try:
        assert tok_idx < len(tokens)
    except AssertionError:
        logger.warning(f"Token idx is greater then length of tokens: {tok_idx=}, {tokens=} (len={len(tokens)})")
        return False
    return True


def _get_and_validate_tok_idx(tok_pos: Hyperedge) -> int | None:
    if not tok_pos:
        return None

    try:
        assert tok_pos.is_atom()
    except AssertionError:
        logger.error(f"Passed tok_pos is not an atom: {tok_pos=}")
        return None

    try:
        tok_idx_str: str = tok_pos[0]
    except IndexError:
        logger.warning(f"Cannot access tok_pos string value: {tok_pos=}")
        return None

    try:
        tok_idx: int = int(tok_idx_str)
    except (TypeError, ValueError):
        logger.warning(f"Cannot convert tok_pos to int value: {tok_pos=}")
        return None

    if tok_idx < 0:
        logger.debug(f"No corresponding token for tok_pos: {tok_pos=}")
        return None

    return tok_idx


def _recursive_tok_idx_search(
        tok_idx: int,
        tok_pos: Hyperedge,
        idx_trail: list[int] = None
) -> list[int] | None:
    if not idx_trail:
        idx_trail = []
    if tok_pos.atom:
        if tok_pos[0] == str(tok_idx):
            return idx_trail
        return None
    for sub_tok_pos_idx, sub_tok_pos in enumerate(tok_pos):
        if sub_idx_trail := _recursive_tok_idx_search(tok_idx, sub_tok_pos, [sub_tok_pos_idx]):
            return idx_trail + sub_idx_trail
    return None


def _follow_tok_idx_trail(tok_pos: Hyperedge, tok_idx_trail: list[int]) -> int | None:
    sub_tok_pos: Hyperedge | Atom = tok_pos
    for sub_tok_idx in tok_idx_trail:
        if len(sub_tok_pos) < sub_tok_idx or sub_tok_pos.is_atom():
            logger.error(f"tok_idx_trail does not match tok_pos: {tok_idx_trail=}, {tok_pos=}")
            return None

        sub_tok_pos: Hyperedge = sub_tok_pos[sub_tok_idx]

    return _get_and_validate_tok_idx(sub_tok_pos)


# Make alignment between lexical tokens and transformer (sentencepiece, wordpiece, ...) tokens
def _get_lex2trf_idx(lexical_tokens: tuple[str], alignment_data: Ragged) -> dict[int, list[int]]:
    lex2trf_idx: dict[int, list[int]] = {}
    trf_idx: int = 0
    for lex_idx in range(len(lexical_tokens)):
        trf_token_length: int = alignment_data.lengths[lex_idx]
        lex2trf_idx[lex_idx] = list(alignment_data.dataXd[trf_idx:trf_idx + trf_token_length])
        trf_idx += trf_token_length
    return lex2trf_idx


# Adapted from: https://huggingface.co/intfloat/e5-base
def _average_pool(last_hidden_states: Tensor, attention_mask: Tensor, normalize: bool = False) -> Tensor:
    last_hidden: Tensor = last_hidden_states.masked_fill(~attention_mask[..., None].bool(), 0.0)
    embeddings: Tensor = last_hidden.sum(dim=1) / attention_mask.sum(dim=1)[..., None]
    if normalize:
        embeddings = F.normalize(embeddings, p=2, dim=1)
    return embeddings
