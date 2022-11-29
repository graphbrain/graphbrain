import logging
from pathlib import Path

import gensim.downloader
from gensim.models import KeyedVectors

from graphbrain.semsim import gensim_data_path

logger = logging.getLogger(__name__)


class SemSimMatcher:
    def __init__(self, model_name: str, similarity_threshold: float):
        self._similarity_threshold: float = similarity_threshold
        self._w2v_model: KeyedVectors = _load_model(model_name)

    def _in_vocab(self, words: list[str]):
        oov_words = []
        for w in words:
            if w not in self._w2v_model:
                oov_words.append(w)

        if oov_words:
            logger.warning(f"Queried word(s) out of vocabulary: {oov_words}")
            return False

        return True

    def similar(self, w1: str, w2: str) -> bool | None:
        if not self._in_vocab([w1, w2]):
            return None

        word_distance = self._w2v_model.distance(w1, w2)
        if word_distance < self._similarity_threshold:
            logger.debug(f"Word distance for ('{w1}, '{w2}') = {word_distance} is smaller than threshold!")
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
    return KeyedVectors.load(str(model_path_bin), mmap='r')  # noqa


def _model_to_bin(model_path: Path, model_path_bin: Path):
    model_bin = KeyedVectors.load_word2vec_format(str(model_path), binary=True)
    model_path_bin.parent.mkdir(parents=False, exist_ok=True)
    model_bin.save(str(model_path_bin))




