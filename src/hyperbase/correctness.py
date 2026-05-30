from __future__ import annotations

from collections import Counter
from typing import TYPE_CHECKING

import hyperbase.constants as const
from hyperbase.constants import EdgeType

if TYPE_CHECKING:
    from hyperbase.hyperedge import Atom, Hyperedge


def check_correctness(edge: Hyperedge) -> dict[Hyperedge, list[tuple[str, str, int]]]:
    """Check correctness of a hyperedge, returning errors keyed by subedge.

    Each error is ``(code, message, severity)``. Correctness failures are hard
    grammar violations, so they all carry severity ``0`` (the most serious).
    """
    if edge.atom:
        return _check_atom(edge)  # type: ignore[arg-type]
    return _check_edge(edge)


def _check_atom(atom: Atom) -> dict[Hyperedge, list[tuple[str, str, int]]]:
    output: dict[Hyperedge, list[tuple[str, str, int]]] = {}
    errors: list[tuple[str, str]] = []

    at = atom.mtype()
    if at not in {
        EdgeType.CONCEPT,
        EdgeType.PREDICATE,
        EdgeType.MODIFIER,
        EdgeType.BUILDER,
        EdgeType.TRIGGER,
        EdgeType.CONJUNCTION,
    }:
        errors.append(("bad-atom-type", f"atom '{atom}' has invalid type {at}"))

    if len(errors) > 0:
        output[atom] = [(code, msg, 0) for code, msg in errors]

    return output


def _check_edge(edge: Hyperedge) -> dict[Hyperedge, list[tuple[str, str, int]]]:
    output: dict[Hyperedge, list[tuple[str, str, int]]] = {}
    errors: list[tuple[str, str]] = []

    ct = edge[0].mtype()
    # check if connector has valid type
    if ct not in {
        EdgeType.PREDICATE,
        EdgeType.MODIFIER,
        EdgeType.BUILDER,
        EdgeType.TRIGGER,
        EdgeType.CONJUNCTION,
    }:
        errors.append(
            (
                "conn-bad-type",
                f"connector '{edge[0]}' of '{edge}' has incorrect type: {ct}",
            )
        )
    # check if modifier structure is correct
    if ct == EdgeType.MODIFIER:
        if len(edge) != 2:
            errors.append(
                ("mod-1-arg", f"modifier edge '{edge}' can only have one argument")
            )
    # check if builder structure is correct
    elif ct == EdgeType.BUILDER:
        if len(edge) != 3:
            errors.append(
                ("build-2-args", f"builder edge '{edge}' can only have two arguments")
            )
        for arg in edge[1:]:
            at = arg.mtype()
            if at != EdgeType.CONCEPT:
                e = f"builder argument '{arg}' of '{edge}' has incorrect type: {at}"
                errors.append(("build-arg-bad-type", e))
    # check if trigger structure is correct
    elif ct == EdgeType.TRIGGER:
        if len(edge) != 2:
            errors.append(
                ("trig-1-arg", f"trigger edge '{edge}' can only have one argument")
            )
        for arg in edge[1:]:
            at = arg.mtype()
            if at not in {EdgeType.CONCEPT, EdgeType.RELATION}:
                e = f"trigger argument '{arg}' of '{edge}' has incorrect type: {at}"
                errors.append(("trig-bad-arg-type", e))
    # check if predicate structure is correct
    elif ct == EdgeType.PREDICATE:
        for arg in edge[1:]:
            at = arg.mtype()
            if at not in {EdgeType.CONCEPT, EdgeType.RELATION, EdgeType.SPECIFIER}:
                e = f"predicate argument '{arg}' of '{edge}' has incorrect type: {at}"
                errors.append(("pred-arg-bad-type", e))
    # check if conjunction structure is correct
    elif ct == EdgeType.CONJUNCTION and len(edge) < 3:
        errors.append(
            (
                "conj-2-args-min",
                f"conjunction edge '{edge}' must have at least two arguments",
            )
        )

    # check argrole counts
    if ct in {EdgeType.PREDICATE, EdgeType.BUILDER}:
        try:
            ars = edge.argroles()
            if len(ars) > 0:
                if ct == EdgeType.PREDICATE:
                    for ar in ars:
                        if ar not in const.valid_p_argroles:
                            errors.append(
                                (
                                    "pred-bad-arg-role",
                                    f"'{ar}' is not a valid argument role for "
                                    f"predicate connector '{edge[0]}' in '{edge}'",
                                )
                            )
                elif ct == EdgeType.BUILDER:
                    for ar in ars:
                        if ar not in const.valid_b_argroles:
                            errors.append(
                                (
                                    "build-bad-arg-role",
                                    f"'{ar}' is not a valid argument role for "
                                    f"builder connector '{edge[0]}' in '{edge}'",
                                )
                            )

                if len(ars) != len(edge) - 1:
                    errors.append(
                        (
                            "bad-num-argroles",
                            f"number of argroles on connector '{edge[0]}' must match "
                            f"number of arguments in '{edge}'",
                        )
                    )

                ars_counts = Counter(ars)
                for role in "soam":
                    if ars_counts[role] > 1:
                        errors.append(
                            (
                                f"argrole-{role}-1-max",
                                f"argrole '{role}' can only be used once in '{edge}'",
                            )
                        )
            else:
                errors.append(
                    (
                        "no-argroles",
                        f"connector '{edge[0]}' of '{edge}' (type P or B) must have "
                        "argument roles",
                    )
                )
        except RuntimeError:
            # malformed edges are detected elsewhere
            pass

    if len(errors) > 0:
        output[edge] = [(code, msg, 0) for code, msg in errors]

    for subedge in edge:
        output.update(check_correctness(subedge))

    return output


def check_structural_quality(
    edge: Hyperedge,
) -> dict[Hyperedge, list[tuple[str, str, int]]]:
    """Soft, edge-only quality checks on argroles, modifiers and junctions.

    Unlike :func:`check_correctness` (hard grammar rules), these flag
    structures that are valid but unidiomatic. The third tuple value is a
    severity (lower is worse): ``2`` for argrole problems, ``3`` for modifier
    and junction issues.
    """
    errors: dict[Hyperedge, list[tuple[str, str, int]]] = {}

    def _visit(current_edge: Hyperedge) -> None:
        if not current_edge or current_edge.atom:
            return

        current_errors: list[tuple[str, str, int]] = []

        # Argrole checks
        try:
            ars = current_edge.argroles()
            ar_counts: Counter[str] = Counter()
            for ar in ars:
                if ar not in "masox":
                    current_errors.append(
                        (
                            "bad-argrole",
                            f"Bad argument role '{ar}' on connector "
                            f"'{current_edge[0]}' in '{current_edge}'. "
                            "Should be one of 'masox'.",
                            2,
                        )
                    )
                ar_counts[ar] += 1
        except Exception:
            pass

        # Modifier checks
        try:
            connector_type = current_edge[0].type()
            if len(current_edge) >= 2:
                target_mt = current_edge[1].mt
                if connector_type in {"Ma", "Md", "Mq", "Mp"} and target_mt != "C":
                    current_errors.append(
                        (
                            f"bad-{connector_type.lower()}-target",
                            f"Modifier '{current_edge}' of type '{connector_type}' "
                            "should only be applied to concepts (type 'C'), but its "
                            f"target '{current_edge[1]}' has type '{target_mt}'.",
                            3,
                        )
                    )
                elif connector_type == "Mm" and target_mt != "P":
                    current_errors.append(
                        (
                            "bad-mm-target",
                            f"Modifier '{current_edge}' of type 'Mm' should only be "
                            "applied to predicates (type 'P'), but its target "
                            f"'{current_edge[1]}' has type '{target_mt}'.",
                            3,
                        )
                    )
                elif connector_type == "Mn" and target_mt not in {"C", "P"}:
                    current_errors.append(
                        (
                            "bad-mn-target",
                            f"Modifier '{current_edge}' of type 'Mn' should only be "
                            "applied to concepts (type 'C') or predicates (type 'P'), "
                            f"but its target '{current_edge[1]}' has type "
                            f"'{target_mt}'.",
                            3,
                        )
                    )
        except Exception:
            pass

        # Junction checks
        try:
            if current_edge[0].mt == "J":
                types = {child.mt for child in current_edge[1:]}
                if str(current_edge[0]) == ":/J/.":
                    if types != {"R"} and types != {"C"} and types != {"C", "R"}:
                        current_errors.append(
                            (
                                "bad-colon-junction-types",
                                f"Arguments of junction '{current_edge}' should "
                                "ideally be of either type 'R' or 'C'.",
                                3,
                            )
                        )
                else:
                    if len(types) > 1 and types != {"C", "R"}:
                        current_errors.append(
                            (
                                "bad-junction-types",
                                f"Arguments of junction '{current_edge}' should "
                                "ideally be all of the same type or a combination of "
                                "'R' and 'C' types.",
                                3,
                            )
                        )
        except Exception:
            pass

        if current_errors:
            errors[current_edge] = current_errors

        for child in current_edge:
            _visit(child)

    if edge:
        _visit(edge)
    return errors
