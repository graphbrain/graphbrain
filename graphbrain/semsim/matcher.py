import dataclasses
import logging
from dataclasses import dataclass
from pathlib import Path
from statistics import mean

import gensim.downloader
from gensim.models import KeyedVectors

from graphbrain.semsim import gensim_data_path

logger = logging.getLogger(__name__)


@dataclass
class SemSimConfig:
    model_name: str
    similarity_threshold: float


class SemSimMatcher:
    def __init__(self, config: SemSimConfig):
        self._similarity_threshold: float = config.similarity_threshold
        self._w2v_model: KeyedVectors = _load_model(config.model_name)

    def _in_vocab(self, words: list[str], return_filtered: bool = False) -> bool | list[str]:
        oov_words = [w for w in words if w not in self._w2v_model]
        if oov_words:
            logger.debug(f"Queried word(s) out of vocabulary: {oov_words}")
        if return_filtered:
            return [w for w in words if w not in oov_words]
        if not oov_words:
            return True
        return False

    def filter_oov(self, words: list[str]):
        filtered_words = self._in_vocab(words, return_filtered=True)
        logger.info(f"Words left after filtering OOV words: {filtered_words}")
        return filtered_words

    def similar(self, candidate: str, references: list[str], threshold: float = None) -> bool | None:
        if not references:
            logger.error("No reference word(s) given for semantic similarity matching!")
            return None

        if not (filtered_references := self._in_vocab(references, return_filtered=True)):
            logger.warning(f"All reference word(s) out of vocabulary: {references}")
            return None

        if len(filtered_references) < len(references):
            logger.info(f"Some reference words out of vocabulary: "
                        f"{[r for r in references if r not in filtered_references]}")

        if not self._in_vocab([candidate]):
            return None

        # reference_vectors = [self._w2v_model[reference_word] for reference_word in references]
        word_distance = mean(self._w2v_model.distances(word_or_vector=candidate, other_words=filtered_references))

        similarity_threshold: float = threshold if threshold is not None else self._similarity_threshold
        if word_distance < similarity_threshold:
            logger.debug(f"Word distance for ('{candidate}, {filtered_references}) = {word_distance:.2f} "
                         f"is smaller than threshold!")
            return True

        return False


def _load_model(model_name: str) -> KeyedVectors:
    model_path: Path = gensim_data_path / model_name / f"{model_name}.gz"
    model_path_bin: Path = gensim_data_path / f"{model_name}_bin" / model_name

    # download specified model if it does not exist
    if not model_path_bin.exists() and not model_path.exists():
        download_path: Path = Path(gensim.downloader.load(model_name, return_path=True))
        assert download_path == model_path, f"Model was downloaded incorrectly!"

    # convert the model in binary format if necessary
    if not model_path_bin.exists():
        _model_to_bin(model_path, model_path_bin)

    # load the binary model memory mapped (mmap = 'r')
    # this speeds up loading times massively but slows down computations
    # this trade-off is good in this case, since we only compare two vectors at once
    # return KeyedVectors.load(str(model_path_bin), mmap='r')  # noqa
    return KeyedVectors.load(str(model_path_bin))  # noqa


def _model_to_bin(model_path: Path, model_path_bin: Path):
    model_bin = KeyedVectors.load_word2vec_format(str(model_path), binary=True)
    model_path_bin.parent.mkdir(parents=False, exist_ok=True)
    model_bin.save(str(model_path_bin))




