import logging
import json
from pathlib import Path

logger = logging.getLogger(__name__)


def make_any_func_pattern(words_and_vars: list[str],
                          inner_funcs: list[str] | None = None,
                          arg_roles: list[str] | None = None):

    inner_patterns: list[str] = []
    for wav in words_and_vars:
        inner_patterns_ar: list[str] = []
        if arg_roles:
            for arg_role in arg_roles:
                inner_pattern_ar = f"{wav}/{arg_role}"
                inner_patterns_ar.append(inner_pattern_ar)
        else:
            inner_patterns_ar.append(wav)

        for inner_pattern in inner_patterns_ar:
            if inner_funcs:
                for func in reversed(inner_funcs):
                    inner_pattern = f"({func} {inner_pattern})"
            inner_patterns.append(inner_pattern)

    inner_patterns_joined = " ".join(inner_patterns)
    return f"(any {inner_patterns_joined})"


def save_search_results(result_dict: dict, result_file_path: Path):
    with open(result_file_path, "w") as f:
        json.dump(result_dict, f)
    logger.info(f"Saved results to '{result_file_path}'")

