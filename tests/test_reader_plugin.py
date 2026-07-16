"""Tests for the reader plugin system.

Covers reader registration, auto-detection, specificity ranking,
split_blocks edge cases, and error paths.
"""

import os
import tempfile

import pytest

from hyperbase.readers import reader as reader_module
from hyperbase.readers.reader import (
    _REGISTRY,
    Reader,
    get_reader,
    list_readers,
    register_reader,
    split_blocks,
)
from hyperbase.readers.txt import TxtReader


class TestSplitBlocks:
    """Edge cases for split_blocks()."""

    def test_empty_string(self):
        assert split_blocks("") == []

    def test_whitespace_only(self):
        assert split_blocks("   \n\n   ") == []

    def test_single_line(self):
        assert split_blocks("hello world") == ["hello world"]

    def test_blank_line_separation(self):
        text = "First paragraph.\n\nSecond paragraph."
        blocks = split_blocks(text)
        assert len(blocks) == 2
        assert blocks[0] == "First paragraph."
        assert blocks[1] == "Second paragraph."

    def test_typewriter_line_wrapping(self):
        """Lines within a paragraph (no blank lines) should be joined."""
        text = "This is a long\nline that wraps\nat 40 columns.\n\nNew paragraph here."
        blocks = split_blocks(text)
        assert len(blocks) == 2
        assert blocks[0] == "This is a long line that wraps at 40 columns."
        assert blocks[1] == "New paragraph here."

    def test_no_blank_lines_each_line_is_block(self):
        """Without blank lines, each line becomes its own block."""
        text = "line one\nline two\nline three"
        blocks = split_blocks(text)
        assert len(blocks) == 3

    def test_crlf_normalized(self):
        text = "first\r\n\r\nsecond"
        blocks = split_blocks(text)
        assert len(blocks) == 2

    def test_cr_only_normalized(self):
        text = "first\r\rsecond"
        blocks = split_blocks(text)
        assert len(blocks) == 2

    def test_multiple_blank_lines(self):
        text = "first\n\n\n\nsecond"
        blocks = split_blocks(text)
        assert len(blocks) == 2

    def test_tabs_in_blank_lines(self):
        """Blank lines with only tabs should still be separators."""
        text = "first\n\t\nsecond"
        blocks = split_blocks(text)
        assert len(blocks) == 2

    def test_leading_trailing_whitespace_stripped(self):
        text = "  hello  \n\n  world  "
        blocks = split_blocks(text)
        assert blocks == ["hello", "world"]


class TestReaderRegistry:
    """Tests for register_reader, list_readers, get_reader."""

    def setup_method(self):
        """Save registry state to restore after each test."""
        self._saved_registry = dict(_REGISTRY)
        self._saved_loaded = reader_module._plugins_loaded

    def teardown_method(self):
        """Restore registry state.

        ``_plugins_loaded`` is restored alongside the registry: a plugin scan
        triggered inside a test would otherwise leave the flag set while this
        rolls the plugins back out of the registry, so no later test could get
        them back.
        """
        _REGISTRY.clear()
        _REGISTRY.update(self._saved_registry)
        reader_module._plugins_loaded = self._saved_loaded

    def test_register_and_list(self):
        class DummyReader(Reader):
            @staticmethod
            def accepts(source):
                return False

            def read(self, source):
                yield "dummy"

        register_reader("dummy", DummyReader)
        readers = list_readers()
        assert "dummy" in readers
        assert readers["dummy"] is DummyReader

    def test_register_overwrites(self):
        class R1(Reader):
            @staticmethod
            def accepts(source):
                return False

            def read(self, source):
                yield "r1"

        class R2(Reader):
            @staticmethod
            def accepts(source):
                return False

            def read(self, source):
                yield "r2"

        register_reader("test", R1)
        register_reader("test", R2)
        assert list_readers()["test"] is R2

    def test_get_reader_by_name(self):
        class Named(Reader):
            @staticmethod
            def accepts(source):
                return True

            def read(self, source):
                yield "named"

        register_reader("named_reader", Named)
        reader = get_reader(reader="named_reader")
        assert isinstance(reader, Named)

    def test_get_reader_unknown_name(self):
        with pytest.raises(ValueError, match="not registered"):
            get_reader(reader="nonexistent_reader_xyz")

    def test_get_reader_auto_no_source(self):
        with pytest.raises(ValueError, match="Either 'source' or"):
            get_reader(reader="auto")

    def test_get_reader_auto_no_match(self):
        """Auto-detection with a source no reader accepts."""
        # Clear all readers so nothing matches
        _REGISTRY.clear()
        with pytest.raises(ValueError, match="No reader found"):
            get_reader(source="/nonexistent/path/to/nothing.xyz")

    def test_get_reader_auto_selects_accepting(self):
        class AlwaysAccepts(Reader):
            @staticmethod
            def accepts(source):
                return True

            def read(self, source):
                yield source

        register_reader("always", AlwaysAccepts)
        reader = get_reader(source="anything")
        assert isinstance(reader, AlwaysAccepts)

    def test_specificity_ranking(self):
        """More-specific reader should be preferred over more-general one."""

        class GeneralReader(Reader):
            @staticmethod
            def accepts(source):
                return True

            def read(self, source):
                yield "general"

        class SpecificReader(Reader):
            more_general = ("general_r",)

            @staticmethod
            def accepts(source):
                return True

            def read(self, source):
                yield "specific"

        register_reader("general_r", GeneralReader)
        register_reader("specific_r", SpecificReader)

        reader = get_reader(source="test")
        # specific_r declares general_r as more_general, so specific_r wins
        assert isinstance(reader, SpecificReader)


class TestPluginDiscovery:
    """Readers installed via the 'hyperbase.readers' entry-point group."""

    def setup_method(self):
        self._saved_registry = dict(_REGISTRY)
        self._saved_loaded = reader_module._plugins_loaded

    def teardown_method(self):
        _REGISTRY.clear()
        _REGISTRY.update(self._saved_registry)
        reader_module._plugins_loaded = self._saved_loaded

    def _install_fake_plugin(self, monkeypatch, name, reader_cls):
        """Make a fake entry point the only member of the reader group."""

        class FakeEntryPoint:
            def __init__(self):
                self.name = name

            def load(self):
                return reader_cls

        def fake_entry_points(group):
            assert group == "hyperbase.readers"
            return [FakeEntryPoint()]

        monkeypatch.setattr(reader_module, "entry_points", fake_entry_points)
        reader_module._plugins_loaded = False

    def test_plugin_is_registered_on_first_query(self, monkeypatch):
        class PluginReader(Reader):
            @staticmethod
            def accepts(source):
                return source == "plugin://x"

            def read(self, source):
                yield "from plugin"

        self._install_fake_plugin(monkeypatch, "fake", PluginReader)

        assert list_readers()["fake"] is PluginReader
        assert isinstance(get_reader(source="plugin://x"), PluginReader)

    def test_plugin_does_not_override_explicit_registration(self, monkeypatch):
        """register_reader() wins over an entry point of the same name."""

        class PluginReader(Reader):
            @staticmethod
            def accepts(source):
                return False

            def read(self, source):
                yield "plugin"

        class MineReader(Reader):
            @staticmethod
            def accepts(source):
                return False

            def read(self, source):
                yield "mine"

        self._install_fake_plugin(monkeypatch, "clash", PluginReader)
        register_reader("clash", MineReader)

        assert list_readers()["clash"] is MineReader

    def test_discovery_runs_only_once(self, monkeypatch):
        class PluginReader(Reader):
            @staticmethod
            def accepts(source):
                return False

            def read(self, source):
                yield "plugin"

        calls = []

        class FakeEntryPoint:
            name = "counted"

            def load(self):
                return PluginReader

        def fake_entry_points(group):
            calls.append(group)
            return [FakeEntryPoint()]

        monkeypatch.setattr(reader_module, "entry_points", fake_entry_points)
        reader_module._plugins_loaded = False

        list_readers()
        list_readers()
        get_reader(reader="counted")

        assert len(calls) == 1


class TestTxtReader:
    """Integration tests for the built-in TxtReader."""

    def test_accepts_text_file(self):
        with tempfile.NamedTemporaryFile(suffix=".txt", delete=False) as f:
            path = f.name
        try:
            assert TxtReader.accepts(path) is True
        finally:
            os.unlink(path)

    def test_rejects_binary_extensions(self):
        """Binary formats are left to reader plugins, not read as text.

        Asserted on accepts() rather than through get_reader(): a pdf reader
        plugin may well be installed alongside, in which case get_reader()
        legitimately returns it.
        """
        for suffix in (".pdf", ".docx", ".epub", ".PDF"):
            with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as f:
                path = f.name
            try:
                assert TxtReader.accepts(path) is False
            finally:
                os.unlink(path)

    def test_reads_file(self):
        with tempfile.NamedTemporaryFile(mode="w", suffix=".txt", delete=False) as f:
            f.write("Hello world.\n\nSecond paragraph.")
            f.flush()
            path = f.name

        try:
            reader = get_reader(source=path)
            blocks = list(reader.read(path))
            assert len(blocks) == 2
            assert blocks[0] == "Hello world."
        finally:
            os.unlink(path)

    def test_block_count(self):
        with tempfile.NamedTemporaryFile(mode="w", suffix=".txt", delete=False) as f:
            f.write("One.\n\nTwo.\n\nThree.")
            f.flush()
            path = f.name

        try:
            reader = get_reader(source=path)
            assert reader.block_count(path) == 3
        finally:
            os.unlink(path)

    def test_read_to_text(self):
        with tempfile.NamedTemporaryFile(mode="w", suffix=".txt", delete=False) as f:
            f.write("Alpha.\n\nBeta.")
            f.flush()
            src = f.name

        out = src + ".out"
        try:
            reader = get_reader(source=src)
            reader.read_to_text(src, out)
            with open(out) as f:
                content = f.read()
            assert "Alpha." in content
            assert "Beta." in content
        finally:
            os.unlink(src)
            if os.path.exists(out):
                os.unlink(out)


class TestReaderBaseClass:
    """Tests for the Reader base class defaults."""

    def test_accepts_not_implemented(self):
        with pytest.raises(NotImplementedError):
            Reader.accepts("anything")

    def test_read_not_implemented(self):
        reader = Reader()
        with pytest.raises(NotImplementedError):
            list(reader.read("anything"))

    def test_block_count_default_none(self):
        reader = Reader()
        assert reader.block_count("anything") is None

    def test_more_general_default_empty(self):
        reader = Reader()
        assert reader.more_general == ()
