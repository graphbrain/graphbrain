import argparse
import random
import sys

from hyperbase.cli._io import atomic_write, resolve_io


def run_shuffle(args: argparse.Namespace) -> None:
    """Randomly shuffle the lines of a JSONL parse-results file.

    Lines are treated as opaque text -- nothing is parsed or dropped except
    blank lines -- so the original ordering is the only thing changed. Writes
    either to ``-o/--output`` or back over the input with ``--in-place``. Pass
    ``--seed`` for a reproducible ordering.
    """
    src, dest = resolve_io(args)

    lines: list[str] = []
    with open(src, encoding="utf-8") as fin:
        for line in fin:
            stripped = line.strip()
            if stripped:
                lines.append(stripped)

    # Random(None) seeds from OS entropy (random each run); Random(int) is
    # reproducible -- so no conditional is needed here.
    random.Random(getattr(args, "seed", None)).shuffle(lines)

    with atomic_write(dest) as fout:
        for line in lines:
            fout.write(line + "\n")

    print(f"Lines shuffled: {len(lines)}", file=sys.stderr)
    if getattr(args, "seed", None) is not None:
        print(f"Seed:           {args.seed}", file=sys.stderr)
    print(f"Output:         {dest}", file=sys.stderr)
