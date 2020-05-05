# Changelog

## [0.4.0] - work in progress
### Added
- Coreference resolution in the parser.
- atom_gender(), atom_number(), atom_person(), atom_animacy() in parsers.
- Coreference inference: gender, number and animacy.
- Coreference inference:'X is Y' relationships.
- Function meaning.concepts.has_common_or_proper_concept(edge).
- Function hypergraph.add() now has optional 'count' parameter.
- Function Hyperedge.replace_argroles().
- Function Hyperedge.insert_argrole().
- Function Hyperedge.insert_edge_with_argrole().
- Function Hyperedge.all_atoms().
- Argument role pattern matching.
- HG.match().
- Hyperedge.atom().
- Logic engine, HG.eval().

### Changed
- parse() now returns a dictionary that includes inferred edges.
- parse() now returns a dictionary of edges to text.
- English parser improved.
- Hyperedge main type character codes become upercase (apple/C instead of apple/c).
- Conjunction hyperedge type, character code: "J" (e.g.: and/J).
- Modifier hyperedge type can now be applied to predicates.
- Searching patterns can now include types, argument roles and other atom parts.

### Removed
- Meta-modifier hyperedge type.
- Auxiliary, subpredicate and dependency hyperedge types.

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
- is_proper_concept() renamed to has_proper_concept() (meaning.concepts module)
- find_concept() renamed to strip_concept() (meaning.concepts module)

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
- Hypergraph semantic layer.

### Changed
- Parser is now deterministic.

### Removed
- Misc. obsolete code.

## [0.0.7] - 05-01-2019

Experimental release.

## [0.0.6] - 01-01-2019

Experimental release.

## [0.0.5] - 31-12-2018

Experimental release.

## [0.0.4] - 29-12-2018

Experimental release.

## [0.0.3] - 28-12-2018

Experimental release.

## [0.0.2] - 27-12-2018

Experimental release.

## [0.0.1] - 25-12-2018

Experimental release.
