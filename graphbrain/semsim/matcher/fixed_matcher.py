from __future__ import annotations

import logging
from dataclasses import dataclass
from pathlib import Path
from typing import Union

import gensim.downloader
from gensim.models import KeyedVectors

from graphbrain.semsim.matcher.matcher import SemSimMatcher, SemSimConfig

logger: logging.Logger = logging.getLogger(__name__)


@dataclass
class GensimModelConfig:
    w2v_binary: bool
    key_prefix: str = None


_GENSIM_MODEL_CONFIGS: dict[str, GensimModelConfig] = {
    "word2vec-google-news-300": GensimModelConfig(w2v_binary=True),
    "conceptnet-numberbatch-17-06-300": GensimModelConfig(w2v_binary=False, key_prefix="/c/en/")
}


class FixedEmbeddingMatcher(SemSimMatcher):
    def __init__(self, config: SemSimConfig):
        super().__init__(config=config)
        self._model_dir: Path = self._create_sub_dir("gensim-data", base_dir=self._base_model_dir)
        self._model: KeyedVectors = self._load_model(config.model_name)
        self._model_key_prefix: Union[str, None] = _get_model_key_prefix(config.model_name)

    def _in_vocab(self, words: list[str], return_filtered: bool = False) -> Union[bool | list[str]]:
        oov_words = [w for w in words if w not in self._model]
        if oov_words:
            logger.debug(f"Queried word(s) out of vocabulary: {oov_words}")
        if return_filtered:
            return [w for w in words if w not in oov_words]
        if not oov_words:
            return True
        return False

    def filter_oov(self, words: list[str]) -> list[str]:
        filtered_words = self._in_vocab(words, return_filtered=True)
        logger.debug(f"Words left after filtering OOV words: {filtered_words}")
        return filtered_words

    def _similarities(
            self,
            cand_word: str = None,
            ref_words: list[str] = None,
            **kwargs
    ) -> Union[dict[str, float], None]:
        if self._model_key_prefix:
            cand_word = f"{self._model_key_prefix}{cand_word}"
            ref_words = [f"{self._model_key_prefix}{ref_word}" for ref_word in ref_words]

        assert cand_word is not None and ref_words is not None, (
            f"Candidate and references must be specified! {cand_word=} | {ref_words=}"
        )
        logger.debug(f"Candidate string: {cand_word} | References: {ref_words}")

        if not (filtered_references := self._in_vocab(ref_words, return_filtered=True)):
            logger.warning(f"All reference word(s) out of vocabulary: {ref_words}")
            return None

        if len(filtered_references) < len(ref_words):
            logger.info(f"Some reference words out of vocabulary: "
                        f"{[r for r in ref_words if r not in filtered_references]}")

        if not self._in_vocab([cand_word]):
            return None

        # similarities: dict[str, float] = {ref: self._model.similarity(candidate, ref) for ref in filtered_references}
        return {ref: self._model.similarity(cand_word, ref) for ref in filtered_references}

    def _load_model(self, model_name: str) -> KeyedVectors:
        model_path: Path = Path(gensim.downloader.BASE_DIR) / model_name / f"{model_name}.gz"
        model_path_w2v: Path = self._model_dir / f"{model_name}_w2v" / model_name

        # download specified model if it does not exist
        if not model_path_w2v.exists() and not model_path.exists():
            download_path: Path = Path(gensim.downloader.load(model_name, return_path=True))
            assert download_path == model_path, f"Model was downloaded incorrectly! {download_path=} != {model_path=}"

        # convert the model to word2vec format if necessary
        # this allows for faster loading, since the model does not have be decompressed at load time
        model_w2v: KeyedVectors | None = None
        if not model_path_w2v.exists() and model_name in _GENSIM_MODEL_CONFIGS:
            model_w2v = _model_to_w2v(model_path, model_path_w2v, binary=_GENSIM_MODEL_CONFIGS[model_name].w2v_binary)

        if model_w2v:
            return model_w2v
        if model_path_w2v.exists():
            return KeyedVectors.load(str(model_path_w2v))  # noqa

        # if the w2v format of the model is not configured, load the model directly
        logger.info(
            f"No word2vec format configured for model '{model_name}'," 
            f"always going to decompress the model at load time!"
        )
        return KeyedVectors.load(str(model_path))  # noqa


def _get_model_key_prefix(model_name) -> Union[str, None]:
    return _GENSIM_MODEL_CONFIGS[model_name].key_prefix if model_name in _GENSIM_MODEL_CONFIGS else None


def _model_to_w2v(model_path: Path, model_path_w2v: Path, binary: bool) -> KeyedVectors:
    # load and save the model in word2vec format to speed up loading
    model_w2v = KeyedVectors.load_word2vec_format(str(model_path), binary=binary)
    model_path_w2v.parent.mkdir(exist_ok=True)
    model_w2v.save(str(model_path_w2v))
    return model_w2v
