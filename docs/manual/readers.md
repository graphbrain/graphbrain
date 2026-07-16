# Readers

The `hyperbase.readers` module provides a way to read and parse text from various sources directly into Semantic Hypergraphs. A reader handles the extraction and segmentation of text into paragraph-sized blocks, which can then be fed to a parser.

Hyperbase uses a plugin architecture for readers. The core package ships the `Reader` interface and a single built-in reader for plain text files -- readers for other formats bring their own extraction dependencies and are installed separately as Python packages that register themselves via entry points.

## Reading and parsing sources

The preferred way to read and parse a source is through the `Parser` methods `parse_source()` and `parse_source_to_jsonl()`. These handle reader selection automatically, so you only need a parser instance:

```python
from hyperbase import get_parser

parser = get_parser("generative")

# Iterate over parse results block by block
for results in parser.parse_source("article.txt"):
    for result in results:
        print(result.edge)

# Or write everything to a JSONL file in one call
parser.parse_source_to_jsonl("article.txt", "output.jsonl", progress=True)
```

Both methods accept an optional `reader` argument to force a specific reader instead of auto-detection:

```python
# Force the plain text reader on a file that another reader would also accept
for results in parser.parse_source("notes.txt", reader="plain_text"):
    ...
```

### Extracting raw text (no parsing)

To extract text blocks without parsing, use a reader directly via `get_reader()` (available from `hyperbase.readers`):

```python
from hyperbase.readers import get_reader

reader = get_reader("article.txt")

# Iterate over text blocks
for block in reader.read("article.txt"):
    print(block)

# Or write blocks to a plain text file
reader.read_to_text("article.txt", "output.txt", progress=True)
```

When a named reader is given, the source is not required to obtain the reader instance:

```python
reader = get_reader(reader="plain_text")
```

Either `source` or a named `reader` must be provided -- calling `get_reader()` with neither raises a `ValueError`.

## CLI

The `hyperbase read` command provides a convenient way to read and parse sources from the command line:

```bash
# Parse a local file to JSONL
hyperbase read article.txt -o output.jsonl

# Extract raw text blocks (no parsing)
hyperbase read article.txt -o output.txt

# Specify reader and parser explicitly
hyperbase read source.txt -o output.jsonl --reader plain_text --parser alphabeta --lang en
```

## Source information

Readers attach metadata to each `ParseResult` through the `source_info()` method. When parsing through a reader (via `parse_source()` or `parse_source_to_jsonl()`), the `source` field of each `ParseResult` is automatically populated with this metadata:

```python
for results in parser.parse_source("article.txt"):
    for result in results:
        print(result.source)
        # {"source_type": "txt", "source": "article.txt"}
```

The `source_info()` dict always includes `source_type` and `source`. Readers may add their own fields -- a reader for a source that has a title, for example, would typically add a `title` key. Custom readers override `source_info(source)` to provide their own metadata.

## Built-in reader

### `plain_text`

Reads local text files. The text is split into paragraph-sized blocks: if blank lines are found, they are used as paragraph separators; otherwise each line becomes its own block.

It accepts any local file except those with an extension of a known binary format (`.pdf`, `.doc`, `.docx`, `.epub`, `.odt`, `.rtf`). Those are left to reader plugins, so that a missing plugin raises a `ValueError` rather than silently decoding binary data as text.

Its source metadata is `source_type` and `source` (the file name).

## Auto-detection

When a reader is not explicitly specified, all registered readers are checked and those whose `accepts()` method returns `True` for the given source are collected. If more than one reader matches, the `more_general` mechanism is used to pick the most specific one.

For example, a reader for RSS feeds accepts a subset of what a generic URL reader accepts. Declaring `more_general = ("url",)` on the RSS reader means it is selected whenever both accept a source.

If no registered reader accepts the source, `get_reader()` raises a `ValueError`.

## Reader plugins

A reader plugin is a Python package that registers one or more readers via the `hyperbase.readers` entry-point group in its `pyproject.toml`:

```toml
[project.entry-points."hyperbase.readers"]
rss = "my_package.rss:RSSReader"
```

Installing the package is all that is required -- the readers it provides are discovered automatically the first time the registry is queried, and participate in auto-detection like any other reader. Nothing needs to be imported by the calling code.

Note that every reader plugin is imported when the registry is first queried, because auto-detection has to ask each reader whether it accepts a source. Keep module-level work in a reader module cheap.

## Custom readers

A custom reader must subclass `Reader` and implement two methods:

- `accepts(source)` -- a static method that returns `True` if the reader can handle the given source string.
- `read(source)` -- a generator that yields text blocks from the source.

Optionally, you can implement `block_count(source)` to return the total number of blocks (enabling progress bars), `source_info(source)` to provide metadata for parse results, and set the `more_general` class attribute to declare that this reader is more specific than others.

Here is an example:

```python
from hyperbase.readers import Reader

class RSSReader(Reader):
    more_general = ("url",)  # take priority over a generic URL reader

    @staticmethod
    def accepts(source: str) -> bool:
        return source.endswith(".rss") or source.endswith("/feed")

    def read(self, source: str):
        import feedparser
        feed = feedparser.parse(source)
        for entry in feed.entries:
            # yield the text content of each entry as a block
            yield entry.get("summary", "")

    def source_info(self, source: str):
        return {"source_type": "rss", "source": source}
```

To ship it as a plugin, declare it in the `hyperbase.readers` entry-point group as shown above. To register it directly from your own code instead -- useful for one-off readers that do not warrant a package -- call `register_reader()`:

```python
from hyperbase.readers import register_reader

register_reader("rss", RSSReader)
```

A reader registered this way takes priority over a plugin of the same name. After registration, the new reader is automatically considered during auto-detection, and can also be requested by name:

```python
parser.parse_source_to_jsonl("https://example.com/feed", "feed.jsonl", reader="rss")
```

## Listing registered readers

To see all currently registered readers, including those provided by plugins:

```python
from hyperbase.readers import list_readers

for name, cls in list_readers().items():
    print(f"{name}: {cls.__name__}")
```
