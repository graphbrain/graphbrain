import logging
from abc import ABC
from dataclasses import dataclass
from enum import Enum, auto
from pathlib import Path
from statistics import mean
from typing import Union

import graphbrain.semsim

logger = logging.getLogger(__name__)


class SemSimModelType(Enum):
    FIXED_EMBEDDING = auto()
    CONTEXT_EMBEDDING = auto()


@dataclass
class SemSimConfig:
    model_type: SemSimModelType
    model_name: str
    similarity_threshold: float


class SemSimMatcher(ABC):
    def __init__(self, config: SemSimConfig):
        self.type: SemSimModelType = config.model_type
        self._base_model_dir: Path = _create_sub_dir('models')
        self._base_cache_dir: Path = _create_sub_dir('.cache')
        self._similarity_threshold: float = config.similarity_threshold

    def similar(
            self,
            candidate: str,
            references: list[str],
            threshold: float = None,
            **kwargs
    ) -> bool:
        logger.debug(f"Candidate string: {candidate} | References: {references} | Threshold: {threshold}")

        if not references:
            logger.error("No reference word(s) or sentence(s) given for semantic similarity matching!")
            return False

        similarities: dict[str, float] = self._similarities(candidate, references, **kwargs)
        logger.debug(f"Similarities ('{candidate}'): {similarities}")
        if not similarities:
            return False

        similarity_threshold: float = threshold if threshold is not None else self._similarity_threshold
        if (similarity := mean(similarities.values())) < similarity_threshold:
            logger.debug(f"Similarity is smaller than threshold: "
                         f"semsim('{candidate}, {similarities.keys()}) = {similarity:.2f} < {similarity_threshold}")
            return False

        logger.debug(f"Similarity is greater or equal threshold: "
                     f"semsim('{candidate}, {similarities.keys()}) = {similarity:.2f} >= {similarity_threshold}")
        return True

    def _similarities(
            self,
            *args,
            **kwargs
    ) -> Union[dict[str, float], None]:
        raise NotImplementedError

    def filter_oov(self, words: list[str]) -> list[str]:
        raise NotImplementedError


def _create_sub_dir(dir_name: str) -> Path:
    sub_dir: Path = Path(graphbrain.semsim.__file__).parent / dir_name
    sub_dir.mkdir(exist_ok=True)
    return sub_dir
