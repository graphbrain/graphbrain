import argparse
import contextlib
import json
import os
import sys
import tempfile
from pathlib import Path


def run_dedup(args: argparse.Namespace) -> None:
    """Remove duplicate sentences from a JSONL parse-results file.

    Keeps the first occurrence of each distinct ``text`` (exact match, the same
    key used by ``stats`` duplicate detection) and drops the rest. Writes either
    to ``-o/--output`` or back over the input with ``--in-place``.
    """
    path = Path(args.file).expanduser()
    if not path.is_file():
        print(f"Error: file not found: {path}", file=sys.stderr)
        sys.exit(1)

    in_place = bool(getattr(args, "in_place", False))
    output = getattr(args, "output", None)
    if in_place and output:
        print("Error: use either -o/--output or --in-place, not both", file=sys.stderr)
        sys.exit(1)
    if not in_place and not output:
        print("Error: specify -o/--output <path> or --in-place", file=sys.stderr)
        sys.exit(1)

    dest = path if in_place else Path(output).expanduser()
    dest.parent.mkdir(parents=True, exist_ok=True)

    total = kept = duplicates = malformed = 0
    seen: set[str] = set()

    # Stream to a temp file in the destination directory, then atomically
    # replace — so --in-place never reads and overwrites the same file at once,
    # and an interrupted run never leaves a half-written output.
    fd, tmp_name = tempfile.mkstemp(dir=dest.parent, suffix=".tmp")
    try:
        with (
            open(path, encoding="utf-8") as fin,
            os.fdopen(fd, "w", encoding="utf-8") as fout,
        ):
            for line in fin:
                stripped = line.strip()
                if not stripped:
                    continue
                total += 1
                # Key on the raw ``text`` field only. We deliberately do NOT
                # reconstruct a ParseResult: that would run hedge() on the edge
                # string and drop an otherwise-valid line whose edge fails to
                # parse. Kept lines are written verbatim (byte-lossless).
                try:
                    text = json.loads(stripped)["text"]
                except Exception:
                    malformed += 1
                    continue
                if text in seen:
                    duplicates += 1
                    continue
                seen.add(text)
                kept += 1
                fout.write(stripped + "\n")
        os.replace(tmp_name, dest)
    except BaseException:
        with contextlib.suppress(OSError):
            os.unlink(tmp_name)
        raise

    print(f"Total lines:   {total}", file=sys.stderr)
    print(f"Kept (unique): {kept}", file=sys.stderr)
    print(f"Duplicates:    {duplicates}", file=sys.stderr)
    if malformed:
        print(f"Malformed:     {malformed} (dropped)", file=sys.stderr)
    print(f"Output:        {dest}", file=sys.stderr)
