import argparse
import json
import sys

from hyperbase.cli._io import atomic_write, resolve_io


def run_dedup(args: argparse.Namespace) -> None:
    """Remove duplicate sentences from a JSONL parse-results file.

    Keeps the first occurrence of each distinct ``text`` (exact match, the same
    key used by ``stats`` duplicate detection) and drops the rest. Writes either
    to ``-o/--output`` or back over the input with ``--in-place``.
    """
    src, dest = resolve_io(args)

    total = kept = duplicates = malformed = 0
    seen: set[str] = set()

    # ``atomic_write`` is the outer context so the input is closed before the
    # final os.replace (matters for --in-place, where dest is the input).
    with atomic_write(dest) as fout, open(src, encoding="utf-8") as fin:
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

    print(f"Total lines:   {total}", file=sys.stderr)
    print(f"Kept (unique): {kept}", file=sys.stderr)
    print(f"Duplicates:    {duplicates}", file=sys.stderr)
    if malformed:
        print(f"Malformed:     {malformed} (dropped)", file=sys.stderr)
    print(f"Output:        {dest}", file=sys.stderr)
