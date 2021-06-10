# Changelog

## [0.5.0] - ongoing work
### Added
- SQLite3 hypergraph database backend.
- Hypergraph.add_with_attributes().

### Changed
- Main hypergraph database backend is now SQLite3.
- LevelDB backend becomes optional. (disabled by default)
- Neuralcoref becomes optional. (disabled by default)

### Removed

## [0.4.3] - 22-04-2021
### Changed
- Fixed AlphaBeta bug related to temporary atoms being removed too soon from atom2tokens.
- Hypergraph.add_sequence() converts sequence name directly to atom.
- Parser level coreference resolution (neuralcoref) disabled by default, requires dedicated build.

## [0.4.2] - 12-04-2021
### Changed
- Solving wheel compilation issue.

## [0.4.1] - 07-04-2021
### Changed
- Solving issue with inclusion of auxiliary data file in non-binary distributions.

## [0.4.0] - 07-04-2021
### Added
- Agents system.
- Conjunctions resolution agent.
- Number agent (singular/plural relations) and related meaning.number module.
- Wikipedia agent.
- Coreference resolution in the parser through local fork of neuralcoref.
- atom_gender(), atom_number(), atom_person(), atom_animacy() in parsers.
- Coreference inference: gender, number and animacy.
- Coreference inference:'X is Y' relationships.
- dir_parser agent (looks for text files in directory tree).
- Function meaning.concepts.has_common_or_proper_concept(edge).
- Function Hypergraph.add() now has optional 'count' parameter.
- Function Hypergraph.sequences().
- Function Hyperedge.replace_argroles().
- Function Hyperedge.insert_argrole().
- Function Hyperedge.insert_edge_with_argrole().
- Function Hyperedge.all_atoms().
- Function Hyperedge.size().
- Function Hyperedge.predicate().
- Function Hyperedge.predicate_atom().
- Function Hyperedge.simplify().
- Function Hyperedge.connector_atom().
- Utility functions to show colored edges in the terminal.

### Changed
- Special characters in atoms are now percent-encoded.
- parse() now returns a dictionary that includes inferred edges.
- parse() now returns a dictionary of edges to text.
- New parser framework, improved English parser + scripts to help train and test new parsers.
- Hyperedge main type character codes become upercase (apple/C instead of apple/c).
- Conjunction hyperedge type, character code: "J" (e.g.: and/J).
- Modifier hyperedge type can now be applied to anything.
- Searching patterns can now include types, argument roles and other atom parts.
- Hypergraph.search_count() renamed to Hypergraph.count().
- corefs_names agent is simplified.
- corefs_onto agent is simplified.
- Tests now run with pytest instead of the deprecated './setup.py test'.
- Hyperedge.replace_atom() optional unique argument.

### Removed
- Meta-modifier hyperedge type.
- Auxiliary, subpredicate and dependency hyperedge types.
- Obsolete Hyperedge.nest() method.

## [0.3.2] - 10-02-2020
### Added
- simplify_role() on Atom objects produces an atom with only its simple type as role.

### Changed
- Lemmas are now based on atoms with simplified roles.
- Improved actors agent (more accurate identification of actors, English only for now).

## [0.3.1] - 03-02-2020
### Added
- German parser (experimental and incomplete).
- Documentation.
- Hyperedge sequences.

### Changed
- Improved hyperedge visualization in notebooks.
- Agents receive language and sequence.
- txt_parser agent creates a sequence.
- Refactored parsers code.
- Catching and logging exceptions during parse.
- Improved conflict agent.

## [0.3.0] - 28-09-2019
### Added
- Tests.
- Documentation.

### Changed
- Graphbrain is now beta (main APIs considered stable).
- LevelDB edge attributes encoded in JSON.
- Renamed hypergraph() to hgraph() and moved function to __jnit__.
- Fixed notebook examples.
- Moved Hypergraph.lemma_degrees() to meaning.lemmas.
- Argroles location is normalized (first position in both predicates and builders).
- In the parsers package, create_parser() was moved to __init__ and the factory module was removed.
- is_proper_concept() renamed to has_proper_concept() (meaning.concepts module).
- find_concept() renamed to strip_concept() (meaning.concepts module).
- Improved notebooks visualizations (show(), blocks(), vblocks()).

### Removed
- graphbrain.funs module.

## [0.2.2] - 13-09-2019

### Added
- txt_parser agent.
- MANIFEST.in to include VERSION file in distribution.

### Changed
- Fixing 'pip install graphbrain' on Linux/Windows.

## [0.2.1] - 04-09-2019
### Added
- claim_actors and corefs_dets agents.
- meaning.concepts module.

### Changed
- Fixed example.
- hypergraph.sum_degree() and .sum_deep_degree().
- Parser improvements.
- Agent corefs_atoms is now corefs_unidecode.
- Improved docs.

### Removed
- Obsolete 'work-in-progress' code.
- hg2json command.

## [0.2.0] - 04-08-2019
### Added
- Primary entities and deep degrees.
- Hyperedges have their own class, deriving from tuple.
- Atoms have a special class, deriving from Hyperedge.
- Restructuring of code files tree.
- Created agent system + first agents.

### Changed
- Parsers now have own package.

### Removed
- Old experimental code.

## [0.1.0] - 14-06-2019
### Added
- First release.