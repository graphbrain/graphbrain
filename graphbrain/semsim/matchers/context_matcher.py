import ast
import dataclasses
import logging
from dataclasses import dataclass
from typing import Union, Any, Callable, Optional

from spacy.language import Language
from spacy.lang.en import English
from spacy.tokens import Doc
from spacy_transformers import TransformerData
from spacy_transformers.pipeline_component import Transformer
from thinc.types import Ragged
from torch import Tensor
from torch.nn import CosineSimilarity
import torch.nn.functional as F

import graphbrain.semsim.semsim
from graphbrain import hedge
from graphbrain.hyperedge import Hyperedge
from graphbrain.hypergraph import Hypergraph
from graphbrain.semsim.matchers.matcher import SemSimMatcher, SemSimConfig

logger: logging.Logger = logging.getLogger(__name__)


# TODO: Caching! (only for references?)
# TODO: pass candidate edge token positions (gets rid of recursive edge search)
# TODO: pass references: sentence, tokenization, token idx

CONTEXT_REFERENCES = [
    {'text': 'Obama says America will not be intimidated by violence of ISIS', 'tokens': ['Obama', 'says', 'America', 'will', 'not', 'be', 'intimidated', 'by', 'violence', 'of', 'ISIS'], 'tok_idx': 1},
    {'text': 'Obama vows to defend Japan with U.S. nuclear umbrella', 'tokens': ['Obama', 'vows', 'to', 'defend', 'Japan', 'with', 'U.S.', 'nuclear', 'umbrella'], 'tok_idx': 1},
    {'text': 'Obama urges against fresh Iran sanctions', 'tokens': ['Obama', 'urges', 'against', 'fresh', 'Iran', 'sanctions'], 'tok_idx': 1}
]


@dataclass()
class TokenContext:
    text: str
    tokens: list[str]
    tok_idx: Optional[int] = None
    tok_pos: Optional[Hyperedge] = None


def contex_references_util(reference_sentences: list[str], config: SemSimConfig):
    matcher = graphbrain.semsim.semsim.matcher
    if not matcher:
        matcher = ContextEmbeddingMatcher(config)
    matcher.contex_references_util(reference_sentences)


class ContextEmbeddingMatcher(SemSimMatcher):
    def __init__(self, config: SemSimConfig):
        super().__init__(config)
        self._spacy_trf_pipe: Language = _create_spacy_pipeline(config.model_name)
        self._cos_sim: CosineSimilarity = CosineSimilarity(dim=1)

    # utility function
    def contex_references_util(self, reference_sentences: list[str]):
        context_refs: list[dict] = []
        for sent in reference_sentences:
            tokens: list[str] = _spacy_doc2str_list(self._spacy_trf_pipe(sent))
            print(f"Sentence: {sent}, Tokens: {[(idx, tok) for idx, tok in enumerate(tokens)]}")
            context_refs.append(dataclasses.asdict(TokenContext(text=sent, tokens=tokens, tok_idx=None)))
        for ref in context_refs:
            print(ref)

    def filter_oov(self, words: list[str]) -> list[str]:
        pass

    def _similarities(
            self,
            candidate: str,
            references: list[str],
            candidate_edge: Hyperedge = None,
            root_edge: Hyperedge = None,
            hg: Hypergraph = None,
            **kwargs
    ) -> Union[dict[str, float], None]:
        if not candidate_edge or not root_edge or not hg:
            logger.error(f"Missing one of these arguments: {candidate_edge=}, {root_edge=}, {hg=}")

        candidate_trf_embedding: Tensor = self._get_candidate_trf_embedding(
            candidate, candidate_edge, root_edge, hg
        )
        reference_trf_embeddings: list[Tensor] = self._get_reference_trf_embedding(references)

        if candidate_trf_embedding is not None and reference_trf_embeddings is not None:
            return {ref: float(self._cos_sim(candidate_trf_embedding, reference_trf_embedding))
                    for ref, reference_trf_embedding in zip(references, reference_trf_embeddings)}

        return None

    def _get_candidate_trf_embedding(
            self,
            candidate: str,
            candidate_edge: Hyperedge,
            root_edge: Hyperedge,
            hg: Hypergraph,
    ) -> Tensor | None:
        root_edge_token_context: TokenContext | None = _get_and_validate_root_edge_token_context(root_edge, hg)
        if not root_edge_token_context:
            return None

        # find out to which token idx in the root edge the candidate refers to
        candidate_tok_idx: int | None = _get_and_validate_candidate_tok_idx(
            candidate, candidate_edge, root_edge, root_edge_token_context
        )
        if not candidate_tok_idx:
            return None

        root_edge_token_context.tok_idx = candidate_tok_idx
        return self._get_trf_embedding(root_edge_token_context)

    def _get_reference_trf_embedding(self, references: list[str]) -> list[Tensor] | None:
        references = CONTEXT_REFERENCES  # TODO: Hotfix, remove!
        try:
            reference_token_contexts: list[TokenContext] = [TokenContext(**ref) for ref in references]
        except Exception as e:
            logger.warning(f"Invalid reference token context given: {references=}, exception: {e}")
            return None

        return [self._get_trf_embedding(token_context) for token_context in reference_token_contexts]

    def _get_trf_embedding(self, token_context: TokenContext) -> Tensor | None:
        spacy_doc: Doc = self._spacy_trf_pipe(token_context.text)
        lexical_tokens: list[str] = _spacy_doc2str_list(spacy_doc)

        # TODO: Hotfix, remove! (trailing punctuation chars)
        if lexical_tokens != token_context.tokens and lexical_tokens == token_context.tokens[:-1]:
            token_context.tokens = lexical_tokens

        try:
            assert lexical_tokens == token_context.tokens
        except AssertionError:
            logger.warning(f"Spacy lexical tokenization does not given tokens: "
                           f"{lexical_tokens=} != {token_context.tokens=}")
            return None

        trf_data: TransformerData = spacy_doc._.trf_data  # noqa
        tok_trf_idxes: list[int] = _get_lex2trf_idx(lexical_tokens, trf_data.align)[token_context.tok_idx]
        trf_tokens: list[str] = [trf_data.wordpieces.strings[0][tok_idx] for tok_idx in tok_trf_idxes]

        logger.debug(f"Lexical token: {lexical_tokens[token_context.tok_idx]}, Transformer tokens: {trf_tokens}")

        trf_embeddings: Tensor = Tensor(trf_data.model_output.last_hidden_state[:, tok_trf_idxes, :])
        return _average_pool(trf_embeddings, Tensor(trf_data.wordpieces.attention_mask[:, tok_trf_idxes]))


def _get_and_validate_root_edge_token_context(root_edge: Hyperedge, hg: Hypergraph) -> TokenContext | None:
    root_edge_text: str = hg.text(root_edge)
    root_edge_tokens: list[str] | None = _get_and_validate_root_edge_str_attribute(
        'tokens', root_edge, hg, constructor=ast.literal_eval
    )
    root_edge_tok_pos: Hyperedge | None = _get_and_validate_root_edge_str_attribute(
        'tok_pos', root_edge, hg, constructor=hedge
    )

    # root edge is not a full sentence (sequence)
    if not root_edge_tokens or not root_edge_tok_pos:
        return None

    return TokenContext(text=root_edge_text, tokens=root_edge_tokens, tok_pos=root_edge_tok_pos)


def _get_and_validate_root_edge_str_attribute(
        attribute_name: str, root_edge: Hyperedge, hg: Hypergraph, constructor: Callable
) -> Any:
    attribute_str_val: str = hg.get_str_attribute(root_edge, attribute_name)
    if not attribute_str_val:
        return None
    try:
        attribute_val: Any = constructor(attribute_str_val)
    except ValueError:
        logger.error(f"Root edge has invalid '{attribute_name}' attribute: {attribute_str_val}")
        return None
    return attribute_val


def _get_and_validate_candidate_tok_idx(
        candidate: str,
        candidate_edge: Hyperedge,
        root_edge: Hyperedge,
        root_edge_token_context: TokenContext
) -> int | None:
    candidate_tok_idx: int | None = _get_candidate_tok_idx(
        candidate, candidate_edge, root_edge, root_edge_token_context.tok_pos
    )
    if candidate_tok_idx is None:
        logger.debug(f"No token idx found for candidate: "
                     f"'{candidate}' (edge: '{candidate_edge}', root edge text: '{root_edge_token_context.text}')")
        return None

    try:
        assert candidate_tok_idx < len(root_edge_token_context.tokens)
    except AssertionError:
        logger.warning(f"Found token idx is greater then length of root edge tokens: "
                       f"{candidate_tok_idx=}, {root_edge_token_context.tokens=} "
                       f"(len={len(root_edge_token_context.tokens)})")
        return None

    # check if corresponding token equals the given candidate str
    candidate_token: str = root_edge_token_context.tokens[candidate_tok_idx]
    try:
        assert candidate.lower() == candidate_token.lower()
    except AssertionError:
        logger.warning(f"Token in root edge does not equal candidate: "
                       f"'{candidate_token}' (idx: {candidate_tok_idx}) != '{candidate}'. "
                       f"Root edge tokens: {root_edge_token_context.tokens}")
        return None

    return candidate_tok_idx


def _get_candidate_tok_idx(
        candidate: str, candidate_edge: Hyperedge, root_edge: Hyperedge, root_edge_tok_pos: Hyperedge
) -> int | None:
    # candidate_edge = graphbrain.hyperedge.unique(candidate_edge)
    # root_edge = graphbrain.hyperedge.unique(root_edge)

    candidate_edge_location: list[int] | None = _recursive_edge_search(root_edge, candidate_edge)
    if not candidate_edge_location:
        return None

    candidate_edge_tok_pos: Hyperedge = hedge(root_edge_tok_pos)
    for idx in candidate_edge_location:
        try:
            candidate_edge_tok_pos: Hyperedge = candidate_edge_tok_pos[idx]
        except IndexError:
            logger.warning(f"Candidate edge location invalid for candidate edge tok pos: "
                           f"{candidate_edge_location=}, {candidate_edge_tok_pos=}")
            return None

    candidate_tok_idx: int = int(candidate_edge_tok_pos[0])
    if candidate_tok_idx < 0:
        logger.debug(f"Candidate '{candidate}' has no corresponding token")
        return None

    return int(candidate_edge_tok_pos[0])


# based on atom string equality, DOES NOT WORK IN CASES WITH IDENTICAL SUB-EDGES! TODO: fix it!
def _recursive_edge_search(
        current_edge: Hyperedge,
        candidate_edge: Hyperedge,
        edge_location: list[int] = None
) -> list[int] | None:
    if not edge_location:
        edge_location = []
    if current_edge.atom:
        # if current_edge[0] == candidate_edge[0]:
        if current_edge == candidate_edge:
            return edge_location
        return None
    for sub_edge_idx, sub_edge in enumerate(current_edge):
        if sub_edge_location := _recursive_edge_search(sub_edge, candidate_edge, [sub_edge_idx]):
            return edge_location + sub_edge_location
    return None


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


def _spacy_doc2str_list(spacy_doc: Doc):
    return [tok.text for tok in spacy_doc]


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
