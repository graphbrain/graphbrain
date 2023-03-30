from __future__ import annotations

import ast
import logging

from diskcache import Cache
from spacy.language import Language
from spacy.lang.en import English
from spacy.tokens import Doc
from spacy_transformers import TransformerData
from spacy_transformers.pipeline_component import Transformer
from thinc.types import Ragged
from torch import Tensor
from torch.nn import CosineSimilarity
import torch.nn.functional as F

from graphbrain.hyperedge import Hyperedge
from graphbrain.hypergraph import Hypergraph
from graphbrain.semsim.matchers.matcher import SemSimMatcher, SemSimConfig

logger: logging.Logger = logging.getLogger(__name__)


# TODO: Caching! (only for references?)


class ContextEmbeddingMatcher(SemSimMatcher):
    def __init__(self, config: SemSimConfig):
        super().__init__(config)
        self._spacy_pipe: Language = _create_spacy_pipeline(config.model_name)
        self._spacy_cache: Cache = Cache(str(self._base_cache_dir / 'spacy'))
        self._cos_sim: CosineSimilarity = CosineSimilarity(dim=1)

    def filter_oov(self, words: list[str]) -> list[str]:
        pass

    def _similarities(
            self,
            candidate: str,
            references: list[str],
            root_edge: Hyperedge = None,
            tok_pos: Hyperedge = None,
            hg: Hypergraph = None,
            **kwargs
    ) -> dict[str, float] | None:
        assert root_edge and hg, f"Missing root_edge and or hg: {root_edge=}, {hg=}"

        tok_idx: int | None = _get_and_validate_tok_idx(tok_pos)
        if tok_idx is None:
            return None

        candidate_trf_embedding: Tensor = self._get_candidate_trf_embedding(candidate, tok_idx, root_edge, hg)
        reference_trf_embeddings: list[Tensor] = self._get_reference_trf_embeddings(references, tok_idx)

        if candidate_trf_embedding is not None and reference_trf_embeddings is not None:
            return {ref: float(self._cos_sim(candidate_trf_embedding, reference_trf_embedding))
                    for ref, reference_trf_embedding in zip(references, reference_trf_embeddings)}

        return None

    def _get_candidate_trf_embedding(
            self,
            candidate: str,
            tok_idx: int,
            root_edge: Hyperedge,
            hg: Hypergraph,
    ) -> Tensor | None:
        root_edge_text: str = hg.text(root_edge)
        root_edge_tokens: list[str] | None = _get_and_validate_root_edge_tokens(root_edge, hg)
        if not root_edge_tokens or not _validate_candidate_tok_idx(candidate, tok_idx, root_edge_tokens):
            return None

        root_edge_spacy_doc, root_edge_spacy_tokens = self._get_spacy_doc_and_tokens(root_edge_text)

        # TODO: Hotfix, remove! (trailing punctuation chars)
        if root_edge_spacy_tokens != root_edge_tokens and root_edge_spacy_tokens == root_edge_tokens[:-1]:
            root_edge_tokens = root_edge_spacy_tokens

        if not _validate_spacy_tokenization(root_edge_tokens, root_edge_spacy_tokens, root_edge_text):
            return None

        return _get_trf_embedding_(root_edge_tokens, tok_idx, root_edge_spacy_doc._.trf_data)  # noqa

    def _get_reference_trf_embeddings(self, references: list[str], tok_idx: int) -> list[Tensor] | None:
        references_trf_data: list[TransformerData] = []
        references_tokens: list[list[str]] = []

        for ref in references:
            reference_spacy_doc, reference_tokens = self._get_spacy_doc_and_tokens(ref)

            if not _validate_tok_idx_for_tokens(tok_idx, reference_tokens):
                logger.warning(f"Reference sentence does not match tok_idx")
                return None

            references_trf_data.append(reference_spacy_doc._.trf_data)  # noqa
            references_tokens.append(reference_tokens)

        return [_get_trf_embedding_(ref_tokens, tok_idx, ref_trf_data)
                for ref_tokens, ref_trf_data in zip(references_tokens, references_trf_data)]

    def _get_spacy_doc_and_tokens(self, text: str) -> tuple[Doc, list[str]]:
        spacy_doc: Doc = self._spacy_pipe(text)
        tokens: list[str] = [tok.text for tok in spacy_doc]
        return spacy_doc, tokens


def _get_trf_embedding_(lexical_tokens: list[str], lex_tok_idx: int, trf_data: TransformerData):
    tok_trf_idxes: list[int] = _get_lex2trf_idx(lexical_tokens, trf_data.align)[lex_tok_idx]
    trf_tokens: list[str] = [trf_data.wordpieces.strings[0][tok_idx] for tok_idx in tok_trf_idxes]

    logger.debug(f"Lexical token: {lexical_tokens[lex_tok_idx]}, Transformer tokens: {trf_tokens}")

    trf_embeddings: Tensor = Tensor(trf_data.model_output.last_hidden_state[:, tok_trf_idxes, :])
    return _average_pool(trf_embeddings, Tensor(trf_data.wordpieces.attention_mask[:, tok_trf_idxes]))


def _validate_spacy_tokenization(tokens: list[str], spacy_tokens: list[str], text: str):
    try:
        assert spacy_tokens == tokens
    except AssertionError:
        logger.warning(f"Spacy lexical tokenization does not equal given tokens: "
                       f"{spacy_tokens=} != {tokens=}, {text=}")
        return False
    return True


def _get_and_validate_root_edge_tokens(root_edge: Hyperedge, hg: Hypergraph) -> list[str] | None:
    root_edge_tokens_str: str = hg.get_str_attribute(root_edge, 'tokens')
    if not root_edge_tokens_str:
        return None
    try:
        root_edge_tokens: list[str] = ast.literal_eval(root_edge_tokens_str)
    except ValueError:
        logger.error(f"Root edge has invalid 'tokens' attribute: {root_edge_tokens_str}")
        return None
    return root_edge_tokens


def _validate_candidate_tok_idx(candidate: str, tok_idx: int, root_edge_tokens: list[str]) -> bool:
    if not _validate_tok_idx_for_tokens(tok_idx, root_edge_tokens):
        return False

    # check if corresponding token equals the given candidate str
    candidate_token: str = root_edge_tokens[tok_idx]
    try:
        assert candidate.lower() == candidate_token.lower()
    except AssertionError:
        logger.warning(f"Token in root edge does not equal candidate: "
                       f"'{candidate_token}' (idx: {tok_idx}) != '{candidate}'. Root edge tokens: {root_edge_tokens}")
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
        tok_idx_str: str = tok_pos[0]
    except IndexError:
        logger.warning(f"Cannot access tok_pos string value: {tok_pos}")
        return None

    try:
        tok_idx: int = int(tok_idx_str)
    except ValueError:
        logger.warning(f"Cannot convert tok_pos to int value: {tok_pos}")
        return None

    if tok_idx < 0:
        logger.debug(f"Candidate has no corresponding token")
        return None

    return tok_idx


# # based on atom string equality, DOES NOT WORK IN CASES WITH IDENTICAL SUB-EDGES! TODO: fix it!
# def _recursive_edge_search(
#         current_edge: Hyperedge,
#         candidate_edge: Hyperedge,
#         edge_location: list[int] = None
# ) -> list[int] | None:
#     if not edge_location:
#         edge_location = []
#     if current_edge.atom:
#         # if current_edge[0] == candidate_edge[0]:
#         if current_edge == candidate_edge:
#             return edge_location
#         return None
#     for sub_edge_idx, sub_edge in enumerate(current_edge):
#         if sub_edge_location := _recursive_edge_search(sub_edge, candidate_edge, [sub_edge_idx]):
#             return edge_location + sub_edge_location
#     return None


# Make alignment between lexical tokens and transformer (sentencepiece, wordpiece, ...) tokens
def _get_lex2trf_idx(lexical_tokens: list[str], alignment_data: Ragged) -> dict[int, list[int]]:
    lex2trf_idx: dict[int, list[int]] = {}
    trf_idx: int = 0
    for lex_idx in range(len(lexical_tokens)):
        trf_token_length: int = alignment_data.lengths[lex_idx]
        lex2trf_idx[lex_idx] = list(alignment_data.dataXd[trf_idx:trf_idx + trf_token_length])
        trf_idx += trf_token_length
    return lex2trf_idx


def _create_spacy_pipeline(model_name: str) -> Language:
    logger.info("Creating SpaCy transformer pipeline...")
    nlp: Language = English()
    config = {
        "model": {
            "@architectures": "spacy-transformers.TransformerModel.v3",
            "name": model_name,
            "tokenizer_config": {"use_fast": True},
        }
    }
    trf: Transformer = nlp.add_pipe("transformer", config=config)  # noqa
    trf.model.initialize()
    logger.info("Done Creating SpaCy transformer pipeline!")
    return nlp


# Adapted from: https://huggingface.co/intfloat/e5-base
def _average_pool(last_hidden_states: Tensor, attention_mask: Tensor, normalize: bool = False) -> Tensor:
    last_hidden: Tensor = last_hidden_states.masked_fill(~attention_mask[..., None].bool(), 0.0)
    embeddings: Tensor = last_hidden.sum(dim=1) / attention_mask.sum(dim=1)[..., None]
    if normalize:
        embeddings = F.normalize(embeddings, p=2, dim=1)
    return embeddings


# def get_lex2trf_idx(lexical_tokens: list[str], alignment_data: Ragged) -> dict[int, list[int]]:
#     return {lex_idx: get_trf_token_idxes(lex_idx, alignment_data) for lex_idx in range(len(lexical_tokens))}

# def get_trf_token_idxes(lex_idx_: int, alignment_data: Ragged):
#     start_idx: int = int(np.sum(alignment_data.lengths[:lex_idx_]))
#     end_idx: int = start_idx + alignment_data.lengths[lex_idx_]
#     return alignment_data.dataXd[start_idx:end_idx]
