import logging
from pathlib import Path
from statistics import mean
from typing import Union

import gensim.downloader
from gensim.models import KeyedVectors

from graphbrain.semsim.matchers.matcher import SemSimMatcher, SemSimConfig

logger: logging.Logger = logging.getLogger(__name__)


class FixedEmbeddingMatcher(SemSimMatcher):
    def __init__(self, config: SemSimConfig):
        super().__init__(config)
        self._model_path: Path = self._base_model_path / 'gensim-data'
        self._model: KeyedVectors = self._load_model(config.model_name)

    def _in_vocab(self, words: list[str], return_filtered: bool = False) -> bool | list[str]:
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
        logger.info(f"Words left after filtering OOV words: {filtered_words}")
        return filtered_words

    def _similarities(
            self,
            candidate: str,
            references: list[str],
            **kwargs
    ) -> Union[dict[str, int], None]:
        if not (filtered_references := self._in_vocab(references, return_filtered=True)):
            logger.warning(f"All reference word(s) out of vocabulary: {references}")
            return None

        if len(filtered_references) < len(references):
            logger.info(f"Some reference words out of vocabulary: "
                        f"{[r for r in references if r not in filtered_references]}")

        if not self._in_vocab([candidate]):
            return None

        return {ref: self._model.similarity(candidate, ref) for ref in filtered_references}

    def _load_model(self, model_name: str) -> KeyedVectors:
        model_path: Path = self._model_path / model_name / f"{model_name}.gz"
        model_path_bin: Path = self._model_path / f"{model_name}_bin" / model_name

        # download specified model if it does not exist
        if not model_path_bin.exists() and not model_path.exists():
            download_path: Path = Path(gensim.downloader.load(model_name, return_path=True))
            assert download_path == model_path, f"Model was downloaded incorrectly!"

        # convert the model in binary format if necessary
        # this allows for faster loading, since the model does not have be decompressed at load time
        if not model_path_bin.exists():
            _model_to_bin(model_path, model_path_bin)

        # load the binary model memory mapped (mmap = 'r')
        # this speeds up loading times massively but slows down computations (not true! see above regarding compression)
        # this trade-off is good in this case, since we only compare two vectors at once
        # return KeyedVectors.load(str(model_path_bin), mmap='r')  # noqa
        return KeyedVectors.load(str(model_path_bin))  # noqa


def _model_to_bin(model_path: Path, model_path_bin: Path):
    model_bin = KeyedVectors.load_word2vec_format(str(model_path), binary=True)
    model_path_bin.parent.mkdir(parents=False, exist_ok=True)
    model_bin.save(str(model_path_bin))
