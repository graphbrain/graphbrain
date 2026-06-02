# Semantic Hypergraph Notation

SH notation is based on these principles:

- The simplest SH structure is the atom, which typically corresponds to a single word and is also considered an hyperedge (e.g. `sky/C`).
- Atoms belong to one of six basic types: concept (`C`), predicate (`P`), modifier (`M`), builder (`B`), trigger (`T`) and conjunction (`J`).
- Atoms can be combined to form more complex structures, called hyperedges, for example: `(blue/M sky/C)`
- Non-atomic hyperedges can also be relations (`R`) and specifications (`S`).
- The first element of a non-atomic hyperedge is a connector, followed by arguments that can be atomic or non-atomic hyperedges. Only five types can be connectors: predicates (define relations), modifiers (modify anything), builders (combine two concepts), triggers (define specifiers) and conjunctions (combine anything).
- Predicates and builders have argument roles, which indicate the role of each argument following. Argument roles are encoded as characters following a dot, for example: `is/P.so`.
- Predicates have three possible argument roles: `s` (subject), `o` (object) and `x` (specification).
- Builders have two possible argument roles: `m` (main) and `a` (auxiliary).

This is enough to represent any sentence in natural language, for example "The sky is blue":

```
(is/P.so (the/M sky/C) blue/C)
```

SH notation offers two optional features: subtypes and namespaces.

**Subtypes** provide additional information about an atome type, for exemple `maria/Cp` indicates that this conceot is a proper noun. Unlike the main types, subtypes are meant to be arbitrarily extendable and can have any length, for example, we could define the subtype math and use it in a predicate such as: `union/Pmath`. SH natural language parsers are expected to produce single-character subtypes according to the tables provided below,

**Namespaces** allow us to distinguish atoms with the same root and type but that correspond to different things, for example: `paris/Cp/1` and `paris/Cp/2` to distinguish Paris (France) and Paris (Texas). They can also be used to distinguish the language from which an atom was extracted, for example: `ciel/Cc/fr`, `céu/Cc/pt` and `sky/Cc/en`. Like subtypes, namespaces are strings of lowercase alphanumeric characters of arbitrary length.

Then, the additional notational devices can be employed when useful. Of course, it is always possible to utilize full notation for machine tasks while presented a simplified version for human-friendliness.

## Hyperedge types

All valid semantic hyperedges are of one of the eight types shown in the table below. The first six types can be explicit (directly annotating an atomic hyperedge) or implicit (inferred from the types of the elements of the hyperedge). The last two types are always implicit.

| Code | Type | Purpose | Example |
|------|------|---------|---------|
| **Atomic or non-atomic** | | | |
| C | concept | Define atomic concepts | **apple/C** |
| P | predicate | Build relations | (**is/P.so** berlin/C nice/C) |
| M | modifier | Modify any other hyperedge type, including itself | (**red/M** shoes/C) |
| B | builder | Build concepts from concepts | (**of/B.ma** capital/C germany/C) |
| T | trigger | Build specifications | (**in/T** 1994/C) |
| J | conjunction | Define sequences of hyperedges | (**and/J** meat/C potatoes/C) |
| **Non-atomic only** | | | |
| R | relation | Express facts, statements, questions, orders, ... | **(is/P.so berlin/C nice/C)** |
| S | specifier | Relation specification (e.g. condition, time, ...) | **(in/T 1976/C)** |

## Type inference rules

The table below shows how implicit hyperedge types are inferred.

| Element types | Resulting type |
|---------------|----------------|
| (M x) | x |
| (B C C+) | C |
| (T [CR]) | S |
| (P [CRS]+) | P |
| (J x y+) | x |

We use the notation of regular expressions: the symbol `+` is used to denote one or more entities with the type that precedes it, while square brackets indicate several possibilities (for instance, `[CRS]+` means "at least one of any of C, R or S" types). `x` means any type: `(M x)` is of type `x`.

### Predicate argument roles

Predicates accept the three following argument roles:

- **s**: subject (at most one)
- **o**: object (at most one)
- **x**: specification (any number)

In the simplest terms: the subject is who or what does something, and the object is who or what it is done to. For example:

```
(plays/P.so maria/C chess/C)
```

In the previous case of:

(is/P.so (the/M sky/C) blue/C)

"the sky" is the subject, being assigned the property of "blue". These s/o pairs map to the typical dyadic relationships in knolwedge graphs, with the predicate playing the role of connection label:

maria (subject) ---[plays]---> chess (object)

the sky (subject) ---[is]---> blue (object)

Specifications then allow for further constraints and context in a relation, for example in "Maria plays chess in the afternoon":

```
(plays/P.sox maria/C chess/C (in/T (the/M afternoon/C)))
```

Specifications can be other relations, for example in "Maria plays chess when it rains":

```
(plays/P.sox maria/C chess/C (when/T (rains/P.o it/C)))
```

**Every argument filling the specification role (`x`) must be of type specifier (`S`).** Specifiers are produced by triggers (`(T [CR])` → `S`), so a specification is normally introduced by a trigger word, as in `(in/Tt ...)` or `(when/Tt ...)` above. When a specification has no natural trigger word in the surface text — for example a bare indirect object — the argument must still be turned into an `S` by enclosing it in the appropriate special trigger atom `_/T*/.` (see [Special atoms](#special-atoms)). For instance, "Maria gave Peter a book" has Peter as a recipient with no preposition:

```
(gave/P.sox maria/Cp book/Cc (_/Ti/. peter/Cp))
```

Here `(_/Ti/. peter/Cp)` is a specifier of indirect-object subtype, satisfying the requirement that the `x` argument be of type `S`.

### Builders

In builders, the two argument roles are used to identify the main concept and the auxiliary concept

- **m**: main concept (exactly once)
- **a**: auxiliary concept (exactly once)

These codes are used to build strings, where each character corresponds to the parameter of the builder in the equivalent position. For example, consider the hyperedge:

```
(of/B.ma founder/C psychoanalysis/C)
```

The *ma* subpart indicates that the first concept following the builder should be considered a main concept, and the next one auxiliary. This defines an implicit taxonomical relation, in this case the "founder of psychoanalysis" is a type of "founder". In other words, a concept defined with a builder is a more specific type of the main concept.

Other examples: `(of/B.ma capital/C france/C)` is a type of `capital/C` and `(for/B.ma data/C asia/C)` is a type of `data/C`.

In the general case: `(x/B.ma y/C z/C)` is a subtype of `y/C`.

## Subtypes

The following tables present the subtypes that SH semantic parsers are expected to produce.

### Concept

| Code | Subtype | Example |
|------|---------|---------|
| Cc | common | apple/Cc |
| Cp | proper | mary/Cp |
| Ci | pronoun | she/Ci |
| Cq | quantitative | 27/Cn |
| Ca | adjective | 27/Ca |
| Cd | determinant | some/Cd |
| Cw | interrogative / wh (nominal) | who/Cw, what/Cw, which/Cw |
| Ce | demonstrative pronoun | this/Ce, that/Ce |
| Cg | nominalized verb / gerund | swimming/Cg |
| Cx | unclassified | |

### Predicate

`Pv` is the default declarative verbal predicate. The mood/kind subtypes below take precedence when they apply; a predicate carries exactly one single-character subtype.

| Code | Subtype | Example |
|------|---------|---------|
| Pv | verbal (declarative, default) | is/Pv |
| Pi | interrogative | is/Pi (Is the sky blue?) |
| Pj | imperative / jussive | close/Pj (Close the door) |
| Pn | nominal / copular | non-verbal / copula-drop predicate |
| Pe | existential | there is/are |
| Px | unclassified | |

### Builder

| Code | Subtype | Example |
|------|---------|---------|
| Bp | genitive / relational | of/Bp.ma (capital of France) |
| Bm | partitive / measure | cup of coffee, slice of bread |
| Bx | unclassified | |

Appositives ("Obama, the president") are *not* builders: express them with the generic conjunction `:/J/.`, e.g. `(:/J/. obama/Cp (the/Md president/Cc))`.

### Modifier

| Code | Subtype | Example |
|------|---------|---------|
| Md | determinant | the/Md |
| Ma | adjective | green/Ma |
| Mq | quantitative | 100/Mq |
| Mm | modal / tense / auxiliary | will/Mm, was/Mm |
| Mb | adverbial / manner | quickly/Mb, carefully/Mb |
| Mg | degree / intensifier | very/Mg, more/Mg, too/Mg |
| Mn | negation | not/Mn |
| Mp | possessive | my/Mp |
| Me | demonstrative determiner | this/Me, that/Me |
| Mw | interrogative determiner | which/Mw, whose/Mw |
| Mx | unclassified | |

### Trigger

| Code | Subtype | Description | Example (EN) | Cross-lingual notes |
|------|---------|-------------|--------------|---------------------|
| Tt | temporal | situates event in time | when/Tt, after/Tt | DE *als/wenn*, PT *quando*, FR *quand/lorsque* |
| Tl | locative | situates event in space | in/Tl, where/Tl | covers static location & path; directionality disambiguated via Ts (source) vs Tl (goal) |
| Ti | indirect object | dative / recipient | to/Ti (gave to Mary) | DE dative, FR *à*, PT *a* |
| Ta | passive actor / agent | demoted agent | by/Ta | DE *von/durch*, PT *por*, FR *par* |
| Tb | beneficiary | for whose benefit | for/Tb (for him) | distinct from Tp (purpose); PT *para* is ambiguous Tb/Tp |
| Ts | source / ablative | origin, provenance | from/Ts, out of/Ts | DE *aus/von*, PT *de*, FR *de/depuis* |
| Tn | manner / means / instrument | how an action is performed | with/Tn (with a hammer), by/Tn (by force), carefully/Tn | conflates Latin ablative roles |
| Tw | comitative | accompaniment | with/Tw (with John) | |
| Tr | reference / topic | aboutness | about/Tr, regarding/Tr | JP は, DE *über/bezüglich*, PT *sobre/acerca de* |
| Tq | quantitative / measure | extent, degree, measure | by/Tq (by 5%), for/Tq (for 3 hours) | |
| Tv | privative / negative | absence, exclusion | without/Tv, lacking/Tv | DE *ohne*, PT *sem*, FR *sans* |
| Tf | conditional | hypothesis | if/Tf, unless/Tf | DE *wenn/falls*, PT *se*, FR *si* |
| Tc | causal | cause / reason | because/Tc, since/Tc | DE *weil/da*, PT *porque*, FR *parce que* |
| Tp | purpose / final | intended goal | to/Tp (in order to), so as to/Tp | distinct from To (result): purpose is intended, result is achieved |
| To | result / consecutive | achieved outcome | so that/To, such that/To | |
| Tg | concessive | counter-expectation | although/Tg, even though/Tg | DE *obwohl*, PT *embora*, FR *bien que* |
| Te | comparative | similarity / comparison | like/Te, as/Te, than/Te | |
| Td | declarative complementizer | introduces propositional argument | that/Td (I think that X) | optional in EN/PT, obligatory in DE *dass*, FR *que* |
| Tx | unclassified | | | |

## Special atoms

Special atoms are annotated with the reserved `.` namespace.

| Atom | Purpose | Example |
|------|---------|---------|
| +/B/. | Define compound nouns | (+/B.am/. alan/Cp turing/Cp) |
| :/J/. | Generic conjunction | |

### Special trigger atoms

There is one special trigger atom per trigger subtype. Use them to turn a bare concept or relation into a specifier (`S`) when a specification argument (the `x` role of a predicate) has no natural trigger word in the surface text. The subtype is chosen to match the semantic role the specification plays.

| Atom | Subtype | Example |
|------|---------|---------|
| _/Tt/. | temporal | (_/Tt/. monday/Cc) |
| _/Tl/. | locative | (_/Tl/. berlin/Cp) |
| _/Ti/. | indirect object | (_/Ti/. peter/Cp) |
| _/Ta/. | passive actor / agent | (_/Ta/. dog/Cc) |
| _/Tb/. | beneficiary | (_/Tb/. him/Ci) |
| _/Ts/. | source / ablative | (_/Ts/. paris/Cp) |
| _/Tn/. | manner / means / instrument | (_/Tn/. hammer/Cc) |
| _/Tw/. | comitative | (_/Tw/. john/Cp) |
| _/Tr/. | reference / topic | (_/Tr/. politics/Cc) |
| _/Tq/. | quantitative / measure | (_/Tq/. (5/Mq percent/Cc)) |
| _/Tv/. | privative / negative | (_/Tv/. money/Cc) |
| _/Tf/. | conditional | (_/Tf/. (rains/Pv.s it/Ci)) |
| _/Tc/. | causal | (_/Tc/. rain/Cc) |
| _/Tp/. | purpose / final | (_/Tp/. safety/Cc) |
| _/To/. | result / consecutive | (_/To/. victory/Cc) |
| _/Tg/. | concessive | (_/Tg/. rain/Cc) |
| _/Te/. | comparative | (_/Te/. lion/Cc) |
| _/Td/. | declarative complementizer | (_/Td/. (won/Pv.s she/Ci)) |
| _/Tx/. | unclassified | (_/Tx/. thing/Cc) |
