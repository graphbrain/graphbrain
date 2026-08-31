"""Parser-agnostic parse-correctness checking for parsed edges.

Where :func:`hyperbase.correctness.check_correctness` validates a hyperedge in
isolation, this module checks a whole *parse*: the edge plus how its atoms map
onto the original tokens. :func:`check_parse_correctness` combines the hard
grammar errors, the soft structural-quality errors, and token-matching
validation so any parser plugin can score the output of a parse against the
original tokens. The third value in each error tuple is a severity (lower is
worse): ``0`` for hard correctness failures, ``1`` for token-mismatch issues,
``2`` for argrole problems, ``3`` for junction issues.

When ``strict`` is ``True``, the underlying :func:`check_correctness` also
enforces that every predicate specification-role (``x``) argument is a
specifier (``S``), emitting a ``spec-arg-not-specifier`` failure otherwise.
Default (``strict=False``) behaviour is unchanged.

:func:`parse_coverage` exposes the same token↔atom matching as structured
data — which original tokens went unused and which atoms no token accounts for —
so callers (e.g. a correctness-guided search) can attribute coverage failures
to specific tokens instead of only reading the error messages.

Matching is by exact, case-insensitive surface identity: an atom's root must
*be* one of the tokens. It used to be fuzzy — punctuation stripped from both
sides, then a cascade of concatenation fallbacks so a parse built on one
tokenizer could be scored against another's tokens (spaCy's ``U.S.`` against
``u`` + ``s``, an atom pair ``1`` + ``m`` against the token ``1m``). Every
parser in the ecosystem now shares one tokenizer, and that tolerance quietly
accepted parses no per-token model can represent: ``c'`` cleaned to ``c`` and
matched the token ``c``, so nothing flagged an atom that no token can carry.
"""

from hyperbase.constants import atom_decode
from hyperbase.correctness import check_structural_quality
from hyperbase.hyperedge import Hyperedge
from hyperbase.parsers.utils import clean_alphanumeric, is_structural_atom


def _surface(text: str) -> str:
    """Comparable surface form of a token or an atom root.

    Atom roots are percent-encoded (``str_to_atom`` maps ``%`` to ``%25``, ``.``
    to ``%2e``, ...), and a gold corpus written by hand may spell either form,
    so both sides are decoded before they meet. Case is folded because atom
    roots are lowercased where tokens keep their casing.
    """
    return atom_decode(text).lower()


def parse_coverage(
    edge: Hyperedge, tokens: list[str]
) -> tuple[list[int], list[Hyperedge]]:
    """Attribute a parse's token-coverage failures to their sources.

    Runs the same token↔atom matching as :func:`check_parse_correctness` and
    returns ``(unused_tokens, unaligned_atoms)``: the indices (into ``tokens``)
    of tokens no atom claims, and the non-structural atoms left without one --
    either because the root is nowhere in the sentence or because earlier atoms
    used up every instance of it. Returns ``([], [])`` when matching cannot run
    (e.g. a malformed edge).
    """
    try:
        # Every token instance, by surface form: an atom claims one of them, and
        # a second atom with the same root needs a second instance.
        available: dict[str, list[int]] = {}
        for i, tok in enumerate(tokens):
            available.setdefault(_surface(tok), []).append(i)

        claimed: set[int] = set()
        unaligned: list[Hyperedge] = []
        for atom in edge.all_atoms():
            if is_structural_atom(atom):
                continue  # stands for a connector the text does not spell out
            instances = available.get(_surface(atom.root()))
            if instances:
                claimed.add(instances.pop(0))
            else:
                unaligned.append(atom)

        # A token no atom claimed is only a failure if it carries content:
        # punctuation is expected to go unused.
        unused = [
            i
            for i in range(len(tokens))
            if i not in claimed and clean_alphanumeric(tokens[i])
        ]
        return unused, unaligned
    except Exception:
        return [], []


def check_parse_correctness(
    edge: Hyperedge,
    tokens: list[str],
    strict: bool = False,
) -> dict[str | Hyperedge, list[tuple[str, str, int]]]:

    # Hard grammar failures (severity 0), keyed by subedge.
    errors: dict[str | Hyperedge, list[tuple[str, str, int]]] = {
        k: list(v) for k, v in edge.check_correctness(strict=strict).items()
    }

    structural_errors = check_structural_quality(edge)
    for k, v in structural_errors.items():
        if k in errors:
            errors[k].extend(v)
        else:
            errors[k] = v

    # Only check token matching if we have a valid edge
    if edge:
        try:
            unused, unaligned = parse_coverage(edge, tokens)
            present = {_surface(token) for token in tokens}

            token_matching_errors: list[tuple[str, str, int]] = []
            for atom in unaligned:
                root = atom.root()
                if _surface(root) in present:
                    token_matching_errors.append(
                        (
                            "root-without-token",
                            f"Atom root '{root}' in the parse is used more times than "
                            "it appears in the source sentence.",
                            1,
                        )
                    )
                else:
                    token_matching_errors.append(
                        (
                            "atom-not-a-token",
                            f"Atom root '{root}' is not one of the source tokens; an "
                            "atom must carry exactly one token's surface form.",
                            1,
                        )
                    )

            for token_idx in unused:
                token_matching_errors.append(
                    (
                        "token-unused",
                        f"Token '{tokens[token_idx]}' from the source sentence is not "
                        "used by any atom in the parse.",
                        1,
                    )
                )

            if len(token_matching_errors) > 0:
                errors["token-matching"] = token_matching_errors

        except (AttributeError, Exception):
            # If token counting fails (e.g., edge is invalid), skip it
            pass

    return errors
