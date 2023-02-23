import logging
from abc import ABC
from dataclasses import dataclass
from pathlib import Path

from strenum import StrEnum

import graphbrain.semsim.matchers

logger = logging.getLogger(__name__)


class SemSimModelType(StrEnum):
    FIXED_VECTOR = "FIXED_VECTOR"
    SENTENCE_TRANSFORMER = "SENTENCE_TRANSFORMER"


@dataclass
class SemSimConfig:
    model_type: SemSimModelType
    model_name: str
    similarity_threshold: float


class SemSimMatcher(ABC):
    def __init__(self, config: SemSimConfig):
        self.type: SemSimModelType = config.model_type
        self._base_model_path: Path = self._create_model_dir()
        self._similarity_threshold: float = config.similarity_threshold

    @staticmethod
    def _create_model_dir() -> Path:
        model_dir_path: Path = Path(graphbrain.semsim.semsim.__file__).parent / "models"
        model_dir_path.mkdir(exist_ok=True)
        return model_dir_path

    def similar(self, candidate: str, references: list[str], threshold: float = None) -> bool | None:
        if not references:
            logger.error("No reference word(s) given for semantic similarity matching!")
            return None

        return self._similar(candidate, references, threshold)

    def _similar(self, candidate: str, references: list[str], threshold: float = None) -> bool | None:
        raise NotImplementedError

    def filter_oov(self, words: list[str]) -> list[str]:
        raise NotImplementedError




