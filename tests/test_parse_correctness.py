from hyperbase import hedge
from hyperbase.parsers.correctness import check_parse_correctness, parse_coverage
from hyperbase.parsers.utils import clean_alphanumeric


class TestParseCoverage:
    """Tests for parse_coverage: coverage failures attributed to their sources."""

    def test_full_coverage(self):
        edge = hedge("(plays/Pv.so maria/Cp chess/Cc)")
        unused, overused = parse_coverage(edge, ["Maria", "plays", "chess"])
        assert unused == []
        assert overused == []

    def test_unused_token_reported_with_original_index(self):
        edge = hedge("(plays/Pv.s maria/Cp)")
        unused, overused = parse_coverage(edge, ["Maria", "plays", "chess"])
        assert unused == [2]
        assert overused == []

    def test_punctuation_tokens_never_unused(self):
        # ',' cleans to empty and is excluded from matching, exactly as in
        # check_parse_correctness; indices still refer to the original list.
        edge = hedge("(plays/Pv.s maria/Cp)")
        unused, overused = parse_coverage(edge, ["Maria", ",", "plays", "chess"])
        assert unused == [3]
        assert overused == []

    def test_overused_root_returns_the_atom(self):
        edge = hedge("(plays/Pv.so maria/Cp maria/Cp)")
        unused, overused = parse_coverage(edge, ["Maria", "plays", "chess"])
        assert unused == [2]
        assert [str(a) for a in overused] == ["maria/Cp"]

    def test_agrees_with_check_parse_correctness(self):
        edge = hedge("(plays/Pv.s maria/Cp)")
        tokens = ["Maria", "plays", "chess"]
        unused, overused = parse_coverage(edge, tokens)
        issues = check_parse_correctness(edge, tokens).get("token-matching", [])
        assert len([c for c, _, _ in issues if c == "token-unused"]) == len(unused)
        assert len([c for c, _, _ in issues if c == "root-without-token"]) == len(
            overused
        )


class TestCleanAlphanumeric:
    """The helper that decides whether an unclaimed token is worth reporting."""

    def test_lowercases_and_strips_punctuation(self):
        assert clean_alphanumeric("Test/C") == "testc"
        assert clean_alphanumeric("WORD-123") == "word123"

    def test_punctuation_only_cleans_to_empty(self):
        # This is what exempts '.' and ',' from the token-unused report.
        assert clean_alphanumeric("...") == ""
        assert clean_alphanumeric("(") == ""

    def test_keeps_non_ascii_letters(self):
        assert clean_alphanumeric("Ῥωμαϊκά") == "ῥωμαϊκά"


class TestCheckParseCorrectness:
    """Tests for check_parse_correctness function"""

    def test_valid_parse_matching_tokens(self):
        """Test with valid parse and matching tokens"""
        valid_parse = "(is/P.so (the/M sky/C) blue/C)"
        tokens = ["the", "sky", "is", "blue"]
        edge = hedge(valid_parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        assert len(errors) == 0

    def test_with_original_text(self):
        """Test that passing original text orders roots correctly"""
        valid_parse = "(is/P.so (the/M sky/C) blue/C)"
        tokens = ["the", "sky", "is", "blue"]
        edge = hedge(valid_parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        assert len(errors) == 0

    def test_atom_spanning_several_tokens_is_reported(self):
        """An atom must carry one token: 'newyork' over ['new', 'york'] cannot."""
        parse = "(is/P.s newyork/C)"
        tokens = ["new", "york", "is"]
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        found = [e[0] for e in errors["token-matching"]]
        assert "atom-not-a-token" in found  # newyork
        assert found.count("token-unused") == 2  # new, york

    def test_valid_parse_missing_token(self):
        """Test when parse doesn't use all tokens"""
        parse = "(is/P.so blue/C)"
        tokens = ["sky", "is", "blue"]
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        # Should have error: "sky" not used
        assert len(errors) > 0

    def test_valid_parse_extra_root(self):
        """Test when parse uses root not in tokens"""
        parse = "(is/P.so (the/M sky/C) blue/C)"
        tokens = ["sky", "is", "blue"]  # Missing "the"
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        # Should have error: "the" used but not in tokens
        assert len(errors) > 0

    def test_token_count_mismatch(self):
        """Test when token appears multiple times"""
        parse = "(is/P.s blue/C)"
        tokens = ["blue", "blue", "is"]  # "blue" appears twice
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        # Should have error: "blue" used less than it appears
        assert len(errors) > 0

    def test_special_characters_filtered(self):
        """Test that special characters in tokens are filtered"""
        parse = "(is/P.s blue/C)"
        tokens = ["(", "is", "blue", ")"]
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        assert len(errors) == 0

    def test_empty_tokens_list(self):
        """Test with empty tokens list"""
        parse = "(is/P.s blue/C)"
        tokens = []
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        # Should have errors: roots used but no tokens
        assert len(errors) > 0

    # -- parses tokenized differently from the token list --------------------- #
    # These four came from the spaCy-based Alpha-Beta parser, whose atoms were
    # scored against another tokenizer's tokens; matching used to strip
    # punctuation and try concatenations so they passed. Every parser in the
    # ecosystem now shares one tokenizer, and an atom that carries no single
    # token is a parse no per-token model can produce -- so they are reported.

    def test_atom_joining_tokens_the_tokenizer_split(self):
        """'U.S.' as one atom over the tokens ['u', 's']."""
        # From: Russia regrets U.S. not pressing charges over boy's death
        parse = "(regrets/P.so russia/C (pressing/P.so us/C (over/B.ma charges/C (s/B.am boy/C death/C))))"
        tokens = [
            "russia",
            "regrets",
            "u",
            "s",
            "pressing",
            "charges",
            "over",
            "boy",
            "s",
            "death",
        ]
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        found = [e[0] for e in errors["token-matching"]]
        assert "atom-not-a-token" in found  # us
        assert "token-unused" in found  # the 'u' it was built from

    def test_tokenization_mismatch_us_case_error1(self):
        """Same parse with a missing token: still an error, as it always was."""
        parse = "(regrets/P.sr russia/C (pressing/P.so us/C (over/B.ma charges/C (s/B.am boy/C death/C))))"
        tokens = [
            "russia",
            "regrets",
            "u",
            "pressing",
            "charges",
            "over",
            "boy",
            "s",
            "death",
        ]
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        assert len(errors) > 0

    def test_tokenization_mismatch_us_case_error2(self):
        """Same parse with a bad argrole: still an error, as it always was."""
        parse = "(regrets/P.sr russia/C (pressing/P us/C (over/B.ma charges/C (s/B.am boy/C death/C))))"
        tokens = [
            "russia",
            "regrets",
            "u",
            "s",
            "pressing",
            "charges",
            "over",
            "boy",
            "s",
            "death",
        ]
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        assert len(errors) > 0

    def test_atoms_splitting_a_token_the_tokenizer_kept_whole(self):
        """Atoms '1' + 'm' over the single token '1m'."""
        # From: RAF flies 1m euros to Cyprus
        parse = "(flies/P.sxox raf/C (1/M m/C) euros/C (to/T cyprus/C))"
        tokens = ["raf", "flies", "1m", "euros", "to", "cyprus"]
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        found = [e[0] for e in errors["token-matching"]]
        assert found.count("atom-not-a-token") == 2  # 1, m
        assert "token-unused" in found  # 1m

    def test_atom_never_matches_a_substring_of_a_token(self):
        """'m' must not be satisfied by the 'm' inside 'malawi'."""
        # From: Malawi gets 37m in UK health aid
        parse = "(gets/P.sox malawi/C (37/M m/C) (in/T (uk/M (health/M aid/C))))"
        tokens = ["malawi", "gets", "37m", "in", "uk", "health", "aid"]
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        found = [e[0] for e in errors["token-matching"]]
        assert found.count("atom-not-a-token") == 2  # 37, m -- not 'malawi'
        assert "token-unused" in found  # 37m

    def test_contraction_split_differently_from_the_tokens(self):
        """Atoms 'do' + \"n't\" over the tokens ['don', 't']."""
        parse = (
            "((off/Ml/en (do/Mv.-i-----/en (n't/Mn/en rip/P!.o.-i-----/en))) me/Ci/en)"
        )
        tokens = ["don", "t", "rip", "me", "off"]
        edge = hedge(parse)
        assert edge
        errors = check_parse_correctness(edge, tokens)
        found = [e[0] for e in errors["token-matching"]]
        assert found.count("atom-not-a-token") == 2  # do, n't
        assert found.count("token-unused") == 2  # don, t

    def test_contraction_matching_the_tokens_passes(self):
        """The same contraction is clean once the atoms carry the real tokens."""
        parse = "((doesn/Mv.-i-----/en ('t/Mn/en is/P.o)) blue/C)"
        tokens = ["doesn", "'t", "is", "blue"]
        edge = hedge(parse)
        assert edge
        assert "token-matching" not in check_parse_correctness(edge, tokens)

    def test_valid_edge(self):
        edge = hedge("(is/P.s bob/C)")
        assert edge
        errors = check_parse_correctness(edge, [])

        # Filter out token matching errors
        structural_errors = {k: v for k, v in errors.items() if k != "token-matching"}
        assert not structural_errors

    def test_invalid_argrole(self):
        # 'z' is not in mspaoixtjr
        edge = hedge("(is/P.z bob/C)")
        assert edge
        errors = check_parse_correctness(edge, [])
        # errors is a dict {edge: list of errors} or {string: list of errors}
        # We look for 'bad-argrole' in the errors

        found = False
        for _k, v in errors.items():
            if isinstance(v, list):
                for err in v:
                    if isinstance(err, tuple) and err[0] == "bad-argrole":
                        assert len(err) == 3
                        assert err[2] == 2
                        found = True
                        break
        assert found, "Should detect bad argrole 'z' with severity 2"

    def test_valid_junction(self):
        # All C
        edge = hedge("(and/J bob/C alice/C)")
        assert edge
        errors = check_parse_correctness(edge, [])

        # Ignore token matching errors if any (tokens is empty so maybe some?)
        # But structural errors should be absent.
        structural_errors = []
        for _k, v in errors.items():
            if isinstance(v, list):
                for err in v:
                    if isinstance(err, tuple) and err[0] in [
                        "bad-argrole",
                        "bad-junction-types",
                    ]:
                        structural_errors.append(err)
                    if isinstance(err, tuple) and str(err[0]).startswith(
                        "duplicate-argrole"
                    ):
                        structural_errors.append(err)

        assert not structural_errors, "Should be valid junction"

    def test_token_matching_severity(self):
        edge = hedge("(is/P.s blue/C)")
        tokens = ["is"]  # blue/C is not a token at all
        assert edge
        errors = check_parse_correctness(edge, tokens)
        assert [(e[0], e[2]) for e in errors["token-matching"]] == [
            ("atom-not-a-token", 1)
        ]

    def test_percent_encoded_root_matches_its_token(self):
        # The assembler encodes an atom root ('%' -> '%25'); the token does not.
        edge = hedge("(is/P.so (el/M (25/M %25/C)) mujeres/C)")
        tokens = ["el", "25", "%", "is", "mujeres"]
        assert "token-matching" not in check_parse_correctness(edge, tokens)

    def test_overused_root_is_distinguished_from_a_missing_one(self):
        # 'blue' IS a token, but two atoms want it and there is only one.
        edge = hedge("(is/P.so blue/C blue/C)")
        errors = check_parse_correctness(edge, ["blue", "is"])
        assert [(e[0], e[2]) for e in errors["token-matching"]] == [
            ("root-without-token", 1)
        ]

    def test_check_correctness_severity(self):
        # builders can only have two arguments
        edge = hedge("(+/B a/C b/C c/C)")
        assert edge
        errors = check_parse_correctness(edge, [])

        found = False
        for _k, v in errors.items():
            for err in v:
                if err[0] == "build-2-args":
                    assert err[2] == 0
                    found = True
        assert found, "Should have build-2-args with severity 0"


def _codes(errors):
    return {code for v in errors.values() for code, _msg, _sev in v}


class TestStrictMode:
    """Strict mode enforces that x-role arguments are specifiers (S)."""

    def test_strict_flags_bare_specification_argument(self):
        # "peter" fills the x slot but is a bare concept, not a specifier
        edge = hedge("(gave/Pv.sox maria/Cp book/Cc peter/Cp)")
        assert edge
        errors = check_parse_correctness(edge, [], strict=True)
        assert "spec-arg-not-specifier" in _codes(errors)

    def test_strict_error_has_severity_zero(self):
        edge = hedge("(gave/Pv.sox maria/Cp book/Cc peter/Cp)")
        assert edge
        errors = check_parse_correctness(edge, [], strict=True)
        severities = [
            sev
            for v in errors.values()
            for code, _msg, sev in v
            if code == "spec-arg-not-specifier"
        ]
        assert severities == [0]

    def test_default_mode_does_not_flag_bare_specification(self):
        # Same edge, default (non-strict) mode: behaviour unchanged.
        edge = hedge("(gave/Pv.sox maria/Cp book/Cc peter/Cp)")
        assert edge
        errors = check_parse_correctness(edge, [])
        assert "spec-arg-not-specifier" not in _codes(errors)

    def test_strict_passes_wrapped_specification(self):
        # Wrapping the recipient in a special trigger atom makes it an S.
        edge = hedge("(gave/Pv.sox maria/Cp book/Cc (_/Ti/. peter/Cp))")
        assert edge
        errors = check_parse_correctness(edge, [], strict=True)
        assert "spec-arg-not-specifier" not in _codes(errors)

    def test_strict_allows_relation_specification(self):
        # A trigger applied to a relation is an S and is accepted in the x slot.
        edge = hedge("(plays/Pv.sox maria/Cp chess/Cc (when/Tt (rains/Pv.s it/Ci)))")
        assert edge
        errors = check_parse_correctness(edge, [], strict=True)
        assert "spec-arg-not-specifier" not in _codes(errors)


class TestNewSubtypeModifierRules:
    """Soft modifier-target checks cover the newly added modifier subtypes."""

    def test_interrogative_determiner_on_predicate_flagged(self):
        edge = hedge("(which/Mw (ran/Pv.sox he/Ci))")
        assert edge
        assert "bad-mw-target" in _codes(check_parse_correctness(edge, []))

    def test_demonstrative_determiner_on_concept_ok(self):
        edge = hedge("(this/Me book/Cc)")
        assert edge
        assert "bad-me-target" not in _codes(check_parse_correctness(edge, []))

    def test_manner_adverb_on_concept_flagged(self):
        edge = hedge("(quickly/Mb sky/Cc)")
        assert edge
        assert "bad-mb-target" in _codes(check_parse_correctness(edge, []))
