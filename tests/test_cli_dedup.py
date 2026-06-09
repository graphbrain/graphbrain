import argparse
import json

import pytest

from hyperbase.builders import hedge
from hyperbase.cli.dedup import run_dedup
from hyperbase.parsers.result import ParseResult


def _line(text: str, failed: bool = False) -> str:
    tokens = text.split()
    pr = ParseResult(
        edge=hedge("(is/P a/C b/C)"),
        text=text,
        tokens=tokens,
        tok_pos=hedge("(is/P a/C b/C)"),
        failed=failed,
        errors=["no valid parse"] if failed else [],
    )
    return pr.to_json()


def _write(path, lines: list[str]) -> str:
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return str(path)


def _ns(file, output=None, in_place=False) -> argparse.Namespace:
    return argparse.Namespace(file=file, output=output, in_place=in_place)


def test_removes_duplicates_preserving_first_occurrence_and_order(tmp_path, capsys):
    src = _write(
        tmp_path / "in.jsonl",
        [
            _line("the cat sat"),
            _line("hello world"),
            _line("the cat sat"),
            _line("a third one"),
            _line("hello world"),
        ],
    )
    out = tmp_path / "out.jsonl"
    run_dedup(_ns(src, output=str(out)))

    texts = [
        json.loads(ln)["text"] for ln in out.read_text(encoding="utf-8").splitlines()
    ]
    assert texts == ["the cat sat", "hello world", "a third one"]

    err = capsys.readouterr().err
    assert "Total lines:   5" in err
    assert "Kept (unique): 3" in err
    assert "Duplicates:    2" in err


def test_in_place_rewrites_input(tmp_path, capsys):
    src = _write(
        tmp_path / "data.jsonl",
        [_line("one"), _line("one"), _line("two")],
    )
    run_dedup(_ns(src, in_place=True))

    lines = (tmp_path / "data.jsonl").read_text(encoding="utf-8").splitlines()
    assert len(lines) == 2
    err = capsys.readouterr().err
    assert "Kept (unique): 2" in err
    assert "Duplicates:    1" in err


def test_already_unique_file_is_byte_identical(tmp_path):
    src_path = tmp_path / "uniq.jsonl"
    _write(src_path, [_line("alpha"), _line("beta"), _line("gamma")])
    original = src_path.read_text(encoding="utf-8")

    out = tmp_path / "out.jsonl"
    run_dedup(_ns(str(src_path), output=str(out)))
    assert out.read_text(encoding="utf-8") == original


def test_malformed_line_dropped_and_reported(tmp_path, capsys):
    src_path = tmp_path / "in.jsonl"
    src_path.write_text(
        _line("good one") + "\n" + "{bad json\n" + _line("good two") + "\n",
        encoding="utf-8",
    )
    out = tmp_path / "out.jsonl"
    run_dedup(_ns(str(src_path), output=str(out)))

    lines = out.read_text(encoding="utf-8").splitlines()
    assert len(lines) == 2
    err = capsys.readouterr().err
    assert "Malformed:     1 (dropped)" in err


def test_failed_parse_duplicates_collapse(tmp_path, capsys):
    src = _write(
        tmp_path / "in.jsonl",
        [_line("garble", failed=True), _line("garble", failed=True)],
    )
    out = tmp_path / "out.jsonl"
    run_dedup(_ns(src, output=str(out)))
    assert len(out.read_text(encoding="utf-8").splitlines()) == 1
    assert "Duplicates:    1" in capsys.readouterr().err


def test_neither_output_nor_in_place_exits(tmp_path, capsys):
    src = _write(tmp_path / "in.jsonl", [_line("x")])
    with pytest.raises(SystemExit) as exc:
        run_dedup(_ns(src))
    assert exc.value.code == 1
    assert "specify -o/--output" in capsys.readouterr().err


def test_both_output_and_in_place_exits(tmp_path, capsys):
    src = _write(tmp_path / "in.jsonl", [_line("x")])
    with pytest.raises(SystemExit) as exc:
        run_dedup(_ns(src, output=str(tmp_path / "out.jsonl"), in_place=True))
    assert exc.value.code == 1
    assert "not both" in capsys.readouterr().err


def test_missing_file_exits(tmp_path, capsys):
    missing = tmp_path / "nope.jsonl"
    with pytest.raises(SystemExit) as exc:
        run_dedup(_ns(str(missing), output=str(tmp_path / "out.jsonl")))
    assert exc.value.code == 1
    assert "file not found" in capsys.readouterr().err
