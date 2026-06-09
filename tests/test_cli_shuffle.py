import argparse

import pytest

from hyperbase.builders import hedge
from hyperbase.cli.shuffle import run_shuffle
from hyperbase.parsers.result import ParseResult


def _line(text: str) -> str:
    tokens = text.split()
    pr = ParseResult(
        edge=hedge("(is/P a/C b/C)"),
        text=text,
        tokens=tokens,
        tok_pos=hedge("(is/P a/C b/C)"),
    )
    return pr.to_json()


def _write(path, lines: list[str]) -> str:
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return str(path)


def _ns(file, output=None, in_place=False, seed=None) -> argparse.Namespace:
    return argparse.Namespace(file=file, output=output, in_place=in_place, seed=seed)


def _read(path) -> list[str]:
    return path.read_text(encoding="utf-8").splitlines()


def test_shuffle_is_a_permutation(tmp_path, capsys):
    lines = [_line(f"sentence number {i}") for i in range(20)]
    src = _write(tmp_path / "in.jsonl", lines)
    out = tmp_path / "out.jsonl"
    run_shuffle(_ns(src, output=str(out), seed=42))

    result = _read(out)
    # Same lines, same count — only the order changed.
    assert sorted(result) == sorted(lines)
    assert len(result) == len(lines)
    # A fixed seed over 20 distinct lines reorders them.
    assert result != lines
    assert "Lines shuffled: 20" in capsys.readouterr().err


def test_seed_is_reproducible(tmp_path):
    lines = [_line(f"s {i}") for i in range(15)]
    src = _write(tmp_path / "in.jsonl", lines)
    out_a = tmp_path / "a.jsonl"
    out_b = tmp_path / "b.jsonl"
    run_shuffle(_ns(src, output=str(out_a), seed=7))
    run_shuffle(_ns(src, output=str(out_b), seed=7))
    assert _read(out_a) == _read(out_b)


def test_in_place_shuffles_input(tmp_path, capsys):
    lines = [_line(f"x {i}") for i in range(12)]
    src_path = tmp_path / "data.jsonl"
    _write(src_path, lines)
    run_shuffle(_ns(str(src_path), in_place=True, seed=3))

    result = _read(src_path)
    assert sorted(result) == sorted(lines)
    err = capsys.readouterr().err
    assert "Lines shuffled: 12" in err
    assert "Seed:           3" in err


def test_blank_lines_dropped(tmp_path, capsys):
    src_path = tmp_path / "in.jsonl"
    src_path.write_text(
        _line("one") + "\n\n" + _line("two") + "\n   \n" + _line("three") + "\n",
        encoding="utf-8",
    )
    out = tmp_path / "out.jsonl"
    run_shuffle(_ns(str(src_path), output=str(out), seed=1))

    result = _read(out)
    assert len(result) == 3
    assert "Lines shuffled: 3" in capsys.readouterr().err


def test_no_seed_omits_seed_line(tmp_path, capsys):
    src = _write(tmp_path / "in.jsonl", [_line("a"), _line("b")])
    run_shuffle(_ns(src, output=str(tmp_path / "out.jsonl")))
    assert "Seed:" not in capsys.readouterr().err


def test_neither_output_nor_in_place_exits(tmp_path, capsys):
    src = _write(tmp_path / "in.jsonl", [_line("x")])
    with pytest.raises(SystemExit) as exc:
        run_shuffle(_ns(src))
    assert exc.value.code == 1
    assert "specify -o/--output" in capsys.readouterr().err


def test_missing_file_exits(tmp_path, capsys):
    missing = tmp_path / "nope.jsonl"
    with pytest.raises(SystemExit) as exc:
        run_shuffle(_ns(str(missing), output=str(tmp_path / "out.jsonl")))
    assert exc.value.code == 1
    assert "file not found" in capsys.readouterr().err
