from __future__ import annotations

import logging
from abc import ABC
from dataclasses import dataclass
from enum import Enum
from pathlib import Path
from statistics import mean

logger = logging.getLogger(__name__)


class SemSimType(str, Enum):
    FIX = "FIX"
    CTX = "CTX"


@dataclass
class SemSimConfig:
    model_name: str
    similarity_threshold: float


class SemSimMatcher(ABC):
    def __init__(self, config: SemSimConfig):
        self._base_model_dir: Path = self._create_sub_dir('models')
        self._similarity_threshold: float = config.similarity_threshold

    def similar(
            self,
            threshold: float = None,
            **kwargs
    ) -> bool:
        similarities: dict[str, float] = self._similarities(**kwargs)
        logger.debug(f"Similarities: {similarities}")
        if not similarities:
            return False

        similarity_threshold: float = threshold if threshold is not None else self._similarity_threshold
        if (similarity := mean(similarities.values())) < similarity_threshold:
            logger.debug(f"Mean similarity is smaller than threshold: {similarity:.2f} < {similarity_threshold}")
            return False

        logger.debug(f"Mean similarity is greater or equal threshold: {similarity:.2f} >= {similarity_threshold}")
        return True

    def _similarities(self, **kwargs) -> dict[str, float] | None:
        raise NotImplementedError

    @staticmethod
    def _create_sub_dir(dir_name: str, base_dir: Path = Path.home() / ".graphbrain-data") -> Path:
        base_dir.mkdir(exist_ok=True)
        sub_dir: Path = base_dir / dir_name
        sub_dir.mkdir(exist_ok=True)
        return sub_dir
