from __future__ import annotations

import ast
import logging
from functools import lru_cache
from typing import Union

from spacy import prefer_gpu
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
    _SPACY_DOC_CACHE_SIZE: int = 2048
    _EMBEDDING_CACHE_SIZE: int = 2048

    def __init__(self, config: SemSimConfig):
        super().__init__(config=config)
        self._use_all_tokes: bool = config.use_all_tokens
        self._spacy_pipe: Language = self._create_spacy_pipeline(config.model_name)
        self._embedding_prefix_tokens: list[str] = _get_embedding_prefix_tokens(config.embedding_prefix)
        self._cos_sim: CosineSimilarity = CosineSimilarity()

    def _create_spacy_pipeline(self, model_name: str) -> Language:
        logger.info("Creating SpaCy transformer pipeline...")
        logger.info(f"### GPU ACTIVE: {prefer_gpu()}")

        nlp: Language = English()
        config = {
            "model": {
                "@architectures": "spacy-transformers.TransformerModel.v3",
                "name": model_name,
                "tokenizer_config": {"use_fast": True},
            }
        }
        trf: Transformer = nlp.add_pipe(self._SPACY_PIPE_TRF_COMPONENT_NAME, config=config)
        trf.model.initialize()
        logger.info("Done creating SpaCy transformer pipeline!")
        return nlp

    def _similarities(
            self,
            cand_edge: Hyperedge = None,
            cand_tok_pos: Hyperedge = None,
            ref_edges: list[Hyperedge] = None,
            ref_tok_poses: list[Hyperedge] = None,
            hg: Hypergraph = None,
            **kwargs
    ) -> Union[dict[str, float], None]:
        if not cand_tok_pos:
            logger.debug(f"Missing tok_pos for candidate edge: {cand_edge=}")
            return None

        if not ref_edges:
            logger.warning(f"Missing reference edges for SemSim CTX: {ref_edges=}")
            return None

        # this should not happen and indicates a bug
        assert cand_edge and hg, f"Missing argument(s): {hg=} {cand_edge=} {ref_tok_poses=}"

        cand_tokens: tuple[str, ...] = _get_and_validate_tokens(cand_edge, hg)
        refs_tokens: list[tuple[str, ...]] = [_get_and_validate_tokens(ref_edge, hg) for ref_edge in ref_edges]

        cand_tok_idxes: tuple[int, ...] = _get_and_validate_tok_idxes(cand_tok_pos, cand_tokens, self._use_all_tokes)
        refs_tok_idxes: list[tuple[int, ...]] = [
            _get_and_validate_tok_idxes(ref_tok_pos, ref_tokens, self._use_all_tokes)
            for ref_tok_pos, ref_tokens in zip(ref_tok_poses, refs_tokens)
        ]

        if not cand_tok_idxes or not all(refs_tokens):
            # validation of tok_idxes for tokens failed
            return None

        cand_embedding: Union[Tensor, None] = self._get_embedding(cand_tokens, cand_tok_idxes)
        refs_embeddings: list[Union[Tensor, None]] = [
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
    def _get_embedding(self, tokens: tuple[str, ...], tok_idxes: tuple[int, ...]) -> Union[Tensor, None]:
        prefixed_tokens: tuple[str, ...] = tuple(self._embedding_prefix_tokens + list(tokens))

        spacy_doc, spacy_tokens = self._get_spacy_doc_and_tokens(prefixed_tokens)
        if not _validate_spacy_tokenization(prefixed_tokens, spacy_tokens):
            return None

        prefixed_tok_idxes: tuple[int, ...] = tuple(
            tok_idx + len(self._embedding_prefix_tokens) for tok_idx in tok_idxes
        )
        return _get_trf_embedding_of_lex_tokens(spacy_tokens, prefixed_tok_idxes, spacy_doc._.trf_data)

    @lru_cache(maxsize=_SPACY_DOC_CACHE_SIZE)
    def _get_spacy_doc_and_tokens(self, tokens: tuple[str, ...] = None) -> tuple[Doc, tuple[str, ...]]:
        spacy_doc: Doc = Doc(self._spacy_pipe.vocab, words=tokens)
        self._spacy_pipe.get_pipe(self._SPACY_PIPE_TRF_COMPONENT_NAME)(spacy_doc)
        assert spacy_doc._.trf_data is not None, f"Missing trf_data"  # noqa
        return spacy_doc, tuple(tok.text for tok in spacy_doc)


def _get_trf_embedding_of_lex_tokens(
        lexical_tokens: tuple[str, ...], lex_tok_idxes: tuple[int, ...], trf_data: TransformerData
) -> Union[Tensor, None]:
    trf_tok_idxes: tuple[int, ...] = _get_trf_tok_idxes(len(lexical_tokens), lex_tok_idxes, trf_data.align)

    lex_tokens: list[str] = [lexical_tokens[tok_idx] for tok_idx in lex_tok_idxes]
    trf_tokens: list[str] = [trf_data.wordpieces.strings[0][tok_idx] for tok_idx in trf_tok_idxes]
    logger.debug(f"Lexical tokens: {lex_tokens}, Transformer tokens: {trf_tokens}")

    trf_embeddings: Tensor = Tensor(trf_data.model_output.last_hidden_state[:, trf_tok_idxes, :])
    return _average_pool(trf_embeddings, Tensor(trf_data.wordpieces.attention_mask[:, trf_tok_idxes]))


def _get_trf_tok_idxes(
        n_lex_tokens: int, lex_tok_idxes: tuple[int, ...], alignment_data: Ragged
) -> tuple[int, ...]:
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
        lex2trf_idx[lex_idx] = [int(i) for i in alignment_data.dataXd[trf_idx:trf_idx + trf_token_length]]

        trf_idx += trf_token_length
    return lex2trf_idx


# Adapted from: https://huggingface.co/intfloat/e5-base
def _average_pool(last_hidden_states: Tensor, attention_mask: Tensor, normalize: bool = False) -> Tensor:
    last_hidden: Tensor = last_hidden_states.masked_fill(~attention_mask[..., None].bool(), 0.0)
    embeddings: Tensor = last_hidden.sum(dim=1) / attention_mask.sum(dim=1)[..., None]
    if normalize:
        embeddings = F.normalize(embeddings, p=2)
    return embeddings


def _get_embedding_prefix_tokens(prefix: str) -> list[str]:
    if prefix is None:
        return []

    # Create a Tokenizer with the default settings for English
    # including punctuation rules and exceptions
    nlp = English()
    tokenizer = nlp.tokenizer
    prefix_tokens: list[str] = [str(tok) for tok in tokenizer(prefix)]
    logger.debug(f"Prefix: {prefix}, Prefix tokens: {prefix_tokens}")
    return prefix_tokens


def _validate_spacy_tokenization(tokens: tuple[str, ...], spacy_tokens: tuple[str, ...]) -> bool:
    try:
        assert spacy_tokens == tokens
    except AssertionError:
        logger.warning(f"Spacy lexical tokenization does not equal given tokens: {spacy_tokens=} != {tokens=}")
        return False
    return True


def _get_and_validate_tokens(edge: Hyperedge, hg: Hypergraph) -> Union[tuple[str, ...], None]:
    tokens_str: str = hg.get_str_attribute(edge, 'tokens')
    if not tokens_str:
        return None
    try:
        tokens: list[str] = ast.literal_eval(tokens_str)
    except ValueError:
        logger.error(f"Edge has invalid 'tokens' attribute: {tokens_str}")
        return None
    return tuple(tokens)


def _validate_tok_idxes_for_tokens(tok_idxes: tuple[int, ...], tokens: tuple[str, ...]) -> bool:
    out_of_range_tok_idxes: list[int] = [tok_idx for tok_idx in tok_idxes if tok_idx >= len(tokens)]
    try:
        assert not out_of_range_tok_idxes
    except AssertionError:
        logger.warning(
            f"Token idxes are greater then length of tokens: {out_of_range_tok_idxes=}, {tokens=} (len={len(tokens)})"
        )
        return False
    return True


def _get_and_validate_tok_idxes(tok_pos: Hyperedge, tokens: tuple[str, ...], use_all_tokens: bool) -> tuple[int, ...] | None:
    if use_all_tokens:
        tok_idxes = tuple(range(len(tokens)))
    else:
        tok_idxes = _get_tok_idxes_from_tok_pos(tok_pos)

    if not _validate_tok_idxes_for_tokens(tok_idxes, tokens):
        return None
    return tok_idxes


def _get_tok_idxes_from_tok_pos(tok_pos: Hyperedge) -> tuple[int, ...]:
    # list of atoms in tok_pos (possibly with duplicates)
    tok_pos_atoms: list[Atom] = tok_pos.all_atoms()

    tok_idxes: list[int] = []
    for tok_pos_atom in tok_pos_atoms:
        tok_idx: Union[int, None] = _get_and_validate_tok_idx(tok_pos_atom)
        if tok_idx is not None:
            tok_idxes.append(tok_idx)

    # return tok_idxes sorted
    return tuple(sorted(tok_idxes))


def _get_and_validate_tok_idx(tok_pos: Hyperedge) -> Union[int, None]:
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
