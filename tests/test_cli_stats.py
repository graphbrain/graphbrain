import argparse

import pytest

from hyperbase.builders import hedge
from hyperbase.cli.stats import (
    _TYPE_NAMES,
    _fmt,
    _histogram_buckets,
    _pct,
    run_stats,
)
from hyperbase.parsers.result import ParseResult


def _line(edge: str, tokens: list[str], failed: bool = False, errors=None) -> str:
    pr = ParseResult(
        edge=hedge(edge),
        text=" ".join(tokens),
        tokens=tokens,
        tok_pos=hedge(edge),
        failed=failed,
        errors=errors or [],
    )
    return pr.to_json()


def _sample_file(path) -> str:
    lines = [
        _line("(is/P hyperbase/C great/C)", ["hyperbase", "is", "great"]),
        _line("(is/P (super/M great/C) it/C)", ["super", "great", "it"]),
        _line("(of/B capital/C france/C)", ["capital", "of", "france"]),
        _line("(failed/C)", ["garble"], failed=True, errors=["no valid parse"]),
    ]
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return str(path)


# --- pure helpers ---------------------------------------------------------


def test_pct():
    assert _pct(1, 2) == "50.0%"
    assert _pct(1, 0) == "0.0%"


def test_fmt_trims_whole_floats():
    assert _fmt(4.0) == "4"
    assert _fmt(3) == "3"
    assert _fmt(3.5) == "3.50"


def test_type_names_cover_edge_types():
    assert _TYPE_NAMES["C"] == "Concept"
    assert _TYPE_NAMES["R"] == "Relation"


def test_histogram_one_bin_per_integer_for_small_range():
    buckets = _histogram_buckets([3, 4, 4, 6], bins=None)
    assert buckets == [(3, 3, 1), (4, 4, 2), (5, 5, 0), (6, 6, 1)]


def test_histogram_respects_explicit_bins():
    buckets = _histogram_buckets([3, 4, 4, 6], bins=2)
    assert buckets == [(3, 4, 3), (5, 6, 1)]
    # every value is accounted for exactly once
    assert sum(c for _, _, c in buckets) == 4


def test_histogram_caps_bins_for_wide_range():
    values = list(range(0, 101))
    buckets = _histogram_buckets(values, bins=None)
    assert len(buckets) <= 20
    assert sum(c for _, _, c in buckets) == len(values)


# --- run_stats end to end -------------------------------------------------


def test_run_stats_reports_counts(tmp_path, capsys):
    path = _sample_file(tmp_path / "sample.jsonl")
    run_stats(argparse.Namespace(file=path, bins=None))
    out = capsys.readouterr().out
    assert "Total parses" in out
    assert "Successful" in out
    assert "Failures" in out


def test_run_stats_missing_file_exits(tmp_path, capsys):
    missing = tmp_path / "nope.jsonl"
    with pytest.raises(SystemExit) as exc:
        run_stats(argparse.Namespace(file=str(missing), bins=None))
    assert exc.value.code == 1
    assert "file not found" in capsys.readouterr().err


def test_run_stats_empty_file(tmp_path, capsys):
    path = tmp_path / "empty.jsonl"
    path.write_text("", encoding="utf-8")
    run_stats(argparse.Namespace(file=str(path), bins=None))
    assert "No parse results found" in capsys.readouterr().out
