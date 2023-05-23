import logging
from abc import ABC
from dataclasses import dataclass
from enum import Enum
from pathlib import Path
from statistics import mean
from typing import Union

import graphbrain.semsim
from graphbrain.hyperedge import Hyperedge

logger = logging.getLogger(__name__)


class SemSimType(str, Enum):
    FIXED = "FIXED"
    CONTEXT = "CONTEXT"


@dataclass
class SemSimConfig:
    model_name: str
    similarity_threshold: float


class SemSimMatcher(ABC):
    _TYPE: SemSimType

    def __init__(self, config: SemSimConfig):
        self._type: SemSimType = self._TYPE
        self._base_model_dir: Path = self._create_sub_dir('models')
        self._similarity_threshold: float = config.similarity_threshold

    def similar(
            self,
            candidate: str = None,
            threshold: float = None,
            ref_words: list[str] = None,
            ref_edges: list[Hyperedge] = None,
            **kwargs
    ) -> bool:
        references = None
        if self._type == SemSimType.FIXED:
            references = ref_words
        if self._type == SemSimType.CONTEXT:
            references = ref_edges
        assert references is not None, f"Neither ref_words nor ref_edges given for model type '{self._type}'!"

        logger.debug(f"Candidate string: {candidate} | References: {references} | Threshold: {threshold}")

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

    @staticmethod
    def _create_sub_dir(dir_name: str, base_dir: Path = Path.home() / ".graphbrain-data") -> Path:
        base_dir.mkdir(exist_ok=True)
        sub_dir: Path = base_dir / dir_name
        sub_dir.mkdir(exist_ok=True)
        return sub_dir
