import argparse
import json
import math
import statistics
import sys
from collections import Counter
from pathlib import Path

from rich.console import Console
from rich.table import Table

from hyperbase.constants import EdgeType
from hyperbase.parsers.result import ParseResult

# Single-character main type code -> human-readable name (e.g. "C" -> "Concept").
_TYPE_NAMES = {t.value: t.name.title() for t in EdgeType}

# Width (in characters) of the tallest histogram bar.
_BAR_WIDTH = 40
# How many distinct integer values we'll render as one-bin-each before
# switching to fixed-width binning.
_MAX_INT_BINS = 30
_DEFAULT_BINS = 20
# How many distinct error messages to list in the failure breakdown.
_TOP_ERRORS = 10


def run_stats(args: argparse.Namespace) -> None:
    """Read a JSONL parse-results file and print summary statistics."""
    path = Path(args.file).expanduser()
    if not path.is_file():
        print(f"Error: file not found: {path}", file=sys.stderr)
        sys.exit(1)

    total = 0
    failed = 0
    malformed = 0
    sizes: list[int] = []
    depths: list[int] = []
    token_counts: list[int] = []
    type_counter: Counter[str] = Counter()
    error_counter: Counter[str] = Counter()

    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            total += 1
            try:
                pr = ParseResult.from_dict(json.loads(line))
            except Exception:
                malformed += 1
                continue
            if pr.failed:
                failed += 1
                error_counter.update(pr.errors or ["(no error message)"])
                continue
            edge = pr.edge
            sizes.append(edge.size())
            depths.append(edge.depth())
            token_counts.append(len(pr.tokens))
            try:
                type_counter.update([edge.mtype()])
            except Exception:
                type_counter.update(["?"])

    console = Console()

    if total == 0:
        console.print("[yellow]No parse results found.[/yellow]")
        return

    successful = len(sizes)

    console.print(_overview_table(path, total, successful, failed, malformed))

    if successful:
        console.print()
        console.print(_summary_table(sizes, depths, token_counts))
        console.print()
        console.print(_histogram_table("Edge size distribution", sizes, args.bins))
        console.print()
        console.print(_histogram_table("Edge depth distribution", depths, args.bins))
        console.print()
        console.print(_type_table(type_counter, successful))

    if failed:
        console.print()
        console.print(_failure_table(error_counter, failed))


def _pct(n: int, d: int) -> str:
    return f"{100 * n / d:.1f}%" if d else "0.0%"


def _fmt(x: float) -> str:
    """Format a stat: whole numbers as ints, everything else to 2 decimals."""
    if isinstance(x, int) or float(x).is_integer():
        return str(int(x))
    return f"{x:.2f}"


def _overview_table(
    path: Path, total: int, successful: int, failed: int, malformed: int
) -> Table:
    table = Table(title="Parse statistics", show_header=False)
    table.add_column("Metric", style="bold")
    table.add_column("Value")
    table.add_row("File", str(path))
    table.add_row("Total parses", str(total))
    table.add_row("Successful", f"{successful} ({_pct(successful, total)})")
    table.add_row("Failed", f"{failed} ({_pct(failed, total)})")
    if malformed:
        table.add_row("Malformed lines", f"{malformed} ({_pct(malformed, total)})")
    return table


def _summary_table(
    sizes: list[int], depths: list[int], token_counts: list[int]
) -> Table:
    table = Table(title="Summary statistics")
    table.add_column("Statistic", style="bold")
    table.add_column("Edge size", justify="right")
    table.add_column("Depth", justify="right")
    table.add_column("Tokens", justify="right")

    columns = (sizes, depths, token_counts)
    for label, fn in (
        ("Mean", statistics.mean),
        ("Median", statistics.median),
        ("Min", min),
        ("Max", max),
        ("Std dev", statistics.pstdev),
    ):
        table.add_row(label, *(_fmt(fn(col)) for col in columns))

    total_atoms = sum(sizes)
    total_tokens = sum(token_counts)
    ratios = [s / t for s, t in zip(sizes, token_counts, strict=True) if t]
    mean_ratio = statistics.mean(ratios) if ratios else 0.0
    table.add_section()
    table.add_row(
        "Total",
        f"{total_atoms} atoms",
        "",
        f"{total_tokens} tokens",
    )
    table.add_row("Size / tokens", f"{mean_ratio:.2f}", "", "")
    return table


def _histogram_buckets(
    values: list[int], bins: int | None
) -> list[tuple[int, int, int]]:
    """Bucket integer *values* into ``(low, high_inclusive, count)`` tuples."""
    lo, hi = min(values), max(values)
    span = hi - lo + 1
    if bins is None:
        bins = span if span <= _MAX_INT_BINS else _DEFAULT_BINS
    bins = max(1, bins)
    width = max(1, math.ceil(span / bins))

    buckets: list[tuple[int, int, int]] = []
    start = lo
    while start <= hi:
        end = min(start + width - 1, hi)
        count = sum(1 for v in values if start <= v <= end)
        buckets.append((start, end, count))
        start += width
    return buckets


def _histogram_table(title: str, values: list[int], bins: int | None) -> Table:
    buckets = _histogram_buckets(values, bins)
    max_count = max((c for _, _, c in buckets), default=0)
    n = sum(c for _, _, c in buckets)

    table = Table(title=title)
    table.add_column("Range", justify="right")
    table.add_column("Count", justify="right")
    table.add_column("%", justify="right")
    table.add_column("Distribution")
    for start, end, count in buckets:
        label = str(start) if start == end else f"{start}-{end}"
        bar_len = round(count / max_count * _BAR_WIDTH) if max_count else 0
        table.add_row(label, str(count), _pct(count, n), "█" * bar_len)
    return table


def _type_table(type_counter: Counter[str], successful: int) -> Table:
    table = Table(title="Edge types")
    table.add_column("Type", style="bold")
    table.add_column("Count", justify="right")
    table.add_column("%", justify="right")
    for code, count in type_counter.most_common():
        name = _TYPE_NAMES.get(code, "Unknown" if code == "?" else code)
        table.add_row(f"{name} ({code})", str(count), _pct(count, successful))
    return table


def _failure_table(error_counter: Counter[str], failed: int) -> Table:
    table = Table(title=f"Failures ({failed})")
    table.add_column("Error message")
    table.add_column("Count", justify="right")
    for message, count in error_counter.most_common(_TOP_ERRORS):
        table.add_row(message, str(count))
    remaining = len(error_counter) - _TOP_ERRORS
    if remaining > 0:
        table.add_row(f"… and {remaining} more distinct messages", "")
    return table
