import argparse
import contextlib
import os
import sys
import tempfile
from collections.abc import Iterator
from contextlib import contextmanager
from pathlib import Path
from typing import IO


def resolve_io(args: argparse.Namespace) -> tuple[Path, Path]:
    """Validate a transform command's args and return ``(src, dest)``.

    Shared by parses-file -> parses-file commands (``dedup``, ``shuffle``): the
    input ``file`` must exist, and exactly one of ``-o/--output`` or
    ``--in-place`` must be given. Exits with a stderr message on any violation.
    """
    src = Path(args.file).expanduser()
    if not src.is_file():
        print(f"Error: file not found: {src}", file=sys.stderr)
        sys.exit(1)

    in_place = bool(getattr(args, "in_place", False))
    output = getattr(args, "output", None)
    if in_place and output:
        print("Error: use either -o/--output or --in-place, not both", file=sys.stderr)
        sys.exit(1)
    if not in_place and not output:
        print("Error: specify -o/--output <path> or --in-place", file=sys.stderr)
        sys.exit(1)

    dest = src if in_place else Path(output).expanduser()
    dest.parent.mkdir(parents=True, exist_ok=True)
    return src, dest


@contextmanager
def atomic_write(dest: Path) -> Iterator[IO[str]]:
    """Yield a text handle that is atomically moved onto *dest* on success.

    Writes go to a temp file in *dest*'s directory; on clean exit it is
    ``os.replace``-d onto *dest* -- atomic, and safe even when *dest* is also the
    input being read (``--in-place``). On any exception the temp file is removed.
    """
    fd, tmp_name = tempfile.mkstemp(dir=dest.parent, suffix=".tmp")
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as fout:
            yield fout
        os.replace(tmp_name, dest)
    except BaseException:
        with contextlib.suppress(OSError):
            os.unlink(tmp_name)
        raise
