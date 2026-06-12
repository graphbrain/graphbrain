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

:func:`parse_coverage` exposes the same token↔root matching as structured
data — which original tokens went unused and which atoms over-use their root —
so callers (e.g. a correctness-guided search) can attribute coverage failures
to specific tokens instead of only reading the error messages.
"""

from hyperbase.correctness import check_structural_quality
from hyperbase.hyperedge import Hyperedge
from hyperbase.parsers.utils import clean_alphanumeric, filter_alphanumeric_strings


def _match_tokens_to_roots(
    tokens: list[str], roots: list[str]
) -> tuple[set[int], set[int]]:
    """Greedily match source tokens against parse atom roots.

    Both lists must already be cleaned (see ``filter_alphanumeric_strings``):
    lowercased, alphanumeric-only, empties dropped. Matching tries, in order:
    exact match, several consecutive roots concatenating to one token, one root
    spanning several consecutive tokens, positional multi-token↔multi-root
    concatenation, and non-positional two-token contractions. Returns the sets
    of matched token indices and matched root indices (into the given lists).
    """
    # Track which tokens and roots have been matched
    matched_tokens: set[int] = set()
    matched_roots: set[int] = set()

    # Count remaining unmatched instances of each root
    def count_unmatched_roots(root_value: str) -> int:
        count = 0
        for root_idx, root in enumerate(roots):
            if root == root_value and root_idx not in matched_roots:
                count += 1
        return count

    # Go through each token and try to find matching roots
    for token_idx, token in enumerate(tokens):
        if token_idx in matched_tokens:
            continue  # Already matched this token

        # Try exact match first
        unmatched_root_count = count_unmatched_roots(token)
        if unmatched_root_count > 0:
            matched_tokens.add(token_idx)
            # Find an unmatched instance of this root
            for root_idx, root in enumerate(roots):
                if root == token and root_idx not in matched_roots:
                    matched_roots.add(root_idx)
                    break

        else:
            # Try to find a root that matches this token exactly (case (a))
            for root_idx, root in enumerate(roots):
                if root_idx in matched_roots:
                    continue  # Already matched this root

                if root == token:
                    matched_tokens.add(token_idx)
                    matched_roots.add(root_idx)
                    break

            # If no exact match, try to find combination of roots
            # that form this token (case (b))
            if token_idx not in matched_tokens:
                # Look for sequence of consecutive roots that concatenate
                # to form the token
                for root_start_idx in range(len(roots)):
                    if root_start_idx in matched_roots:
                        continue  # This root is already matched

                    concatenated = ""
                    root_sequence: list[int] = []

                    for root_idx in range(root_start_idx, len(roots)):
                        if root_idx in matched_roots:
                            # Can't use matched roots in sequence
                            break

                        root = roots[root_idx]
                        concatenated += root
                        root_sequence.append(root_idx)

                        if concatenated == token:
                            # Found a matching sequence
                            matched_tokens.add(token_idx)
                            for idx in root_sequence:
                                matched_roots.add(idx)
                            break

                        if len(concatenated) >= len(token):
                            # Gone too far or exact match found
                            break

                    if token_idx in matched_tokens:
                        break  # Found a match, no need to try other
                        # starting positions

            # If still no match, try case (c): root that matches this token
            # and subsequent tokens
            if token_idx not in matched_tokens:
                # Look for a root that can match this token plus some
                # following tokens
                for root_idx, root in enumerate(roots):
                    if root_idx in matched_roots:
                        continue  # Already matched

                    concatenated = ""
                    token_sequence: list[int] = []

                    for next_token_idx in range(token_idx, len(tokens)):
                        if next_token_idx in matched_tokens:
                            continue  # Already matched

                        concatenated += tokens[next_token_idx]
                        token_sequence.append(next_token_idx)

                        if concatenated == root:
                            # Found a root that matches multiple tokens
                            matched_roots.add(root_idx)
                            for idx in token_sequence:
                                matched_tokens.add(idx)
                            break

                        if len(concatenated) >= len(root):
                            break

            # If still no match, try case (d): multi-token to multi-root
            # concatenation matching
            if token_idx not in matched_tokens:
                # First, try positional matching (existing logic)
                for root_start_idx in range(len(roots)):
                    if root_start_idx in matched_roots:
                        continue  # This root is already matched

                    tokens_concatenated = ""
                    roots_concatenated = ""
                    token_sequence_d: list[int] = []
                    root_sequence_d: list[int] = []

                    max_tokens = min(
                        len(tokens) - token_idx, len(roots) - root_start_idx
                    )

                    for i in range(max_tokens):
                        current_token_idx = token_idx + i
                        current_root_idx = root_start_idx + i

                        if (
                            current_token_idx in matched_tokens
                            or current_root_idx in matched_roots
                        ):
                            break  # Can't use already matched items

                        tokens_concatenated += tokens[current_token_idx]
                        roots_concatenated += roots[current_root_idx]
                        token_sequence_d.append(current_token_idx)
                        root_sequence_d.append(current_root_idx)

                        # Check if concatenations match
                        if (
                            tokens_concatenated == roots_concatenated
                            and tokens_concatenated
                        ):
                            # Found a match - mark all as matched
                            for idx in token_sequence_d:
                                matched_tokens.add(idx)
                            for idx in root_sequence_d:
                                matched_roots.add(idx)
                            break

                        # Stop if we've gone too far
                        # (tokens longer than reasonable)
                        if (
                            len(tokens_concatenated) > 10
                            or len(roots_concatenated) > 10
                        ):
                            break

                    if token_idx in matched_tokens:
                        break  # Found a match, no need to try
                        # other root positions

                # If still no match, try non-positional contraction matching
                if (
                    token_idx not in matched_tokens
                    # Look for contractions by trying to combine this token
                    # with the next one and matching against any two available
                    # roots in the roots list (not necessarily consecutive)
                    and (
                        token_idx + 1 < len(tokens)
                        and token_idx + 1 not in matched_tokens
                    )
                ):
                    token_concat = tokens[token_idx] + tokens[token_idx + 1]

                    # Try to find any two available roots
                    # (not necessarily consecutive) that concatenate
                    # to the same value
                    for root_idx1 in range(len(roots)):
                        if root_idx1 in matched_roots:
                            continue  # Can't use already matched roots

                        for root_idx2 in range(len(roots)):
                            if root_idx2 in matched_roots or root_idx2 == root_idx1:
                                continue  # Can't use already matched roots
                                # or same root

                            root_concat = roots[root_idx1] + roots[root_idx2]

                            if token_concat == root_concat:
                                # Found a contraction match!
                                matched_tokens.add(token_idx)
                                matched_tokens.add(token_idx + 1)
                                matched_roots.add(root_idx1)
                                matched_roots.add(root_idx2)
                                break

                        if token_idx in matched_tokens:
                            break  # Found a match, no need to try
                            # other combinations

    return matched_tokens, matched_roots


def parse_coverage(
    edge: Hyperedge, tokens: list[str]
) -> tuple[list[int], list[Hyperedge]]:
    """Attribute a parse's token-coverage failures to their sources.

    Runs the same token↔root matching as :func:`check_parse_correctness` and
    returns ``(unused_tokens, overused_atoms)``: the indices (into the
    *original* ``tokens`` list) of tokens no atom accounts for, and the atoms
    whose root is used more times than it appears in the sentence. Returns
    ``([], [])`` when matching cannot run (e.g. a malformed edge).
    """
    try:
        cleaned_tokens: list[str] = []
        original_idx: list[int] = []
        for i, tok in enumerate(tokens):
            cleaned = clean_alphanumeric(tok)
            if cleaned:
                cleaned_tokens.append(cleaned)
                original_idx.append(i)
        atoms: list[Hyperedge] = []
        roots: list[str] = []
        for atom in edge.all_atoms():
            cleaned = clean_alphanumeric(atom.label())
            if cleaned:
                atoms.append(atom)
                roots.append(cleaned)
        matched_tokens, matched_roots = _match_tokens_to_roots(cleaned_tokens, roots)
        unused = [
            original_idx[i]
            for i in range(len(cleaned_tokens))
            if i not in matched_tokens
        ]
        overused = [atoms[i] for i in range(len(roots)) if i not in matched_roots]
        return unused, overused
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
            tokens = filter_alphanumeric_strings(tokens)
            roots: list[str] = filter_alphanumeric_strings(
                [atom.label() for atom in edge.all_atoms()]
            )

            matched_tokens, matched_roots = _match_tokens_to_roots(tokens, roots)

            token_matching_errors: list[tuple[str, str, int]] = []
            # Report unmatched roots
            for root_idx, root in enumerate(roots):
                if root_idx not in matched_roots:
                    token_matching_errors.append(
                        (
                            "root-without-token",
                            f"Atom root '{root}' in the parse is used more times than "
                            "it appears in the source sentence.",
                            1,
                        )
                    )

            # Report unmatched tokens
            for token_idx, token in enumerate(tokens):
                if token_idx not in matched_tokens:
                    token_matching_errors.append(
                        (
                            "token-unused",
                            f"Token '{token}' from the source sentence is not used by "
                            "any atom in the parse.",
                            1,
                        )
                    )

            if len(token_matching_errors) > 0:
                errors["token-matching"] = token_matching_errors

        except (AttributeError, Exception):
            # If token counting fails (e.g., edge is invalid), skip it
            pass

    return errors
