## Context

The template syntax design impacts developer experience, Java string literal ergonomics, and parser implementation complexity. This change affects the core parsing subsystem that will be used throughout the project lifecycle.

### Stakeholders
- Developers using the library (syntax usability)
- Maintainers (parser complexity and extensibility)

### Constraints
- Must be valid Java string literal content (no escaping headaches)
- Must support future expression syntax (`${user.getName()}`, `${a + b}`)
- Parser must handle nested constructs correctly (e.g., `${map.get("}")}`)

## Goals / Non-Goals

### Goals
- Eliminate Java string escaping issues with template syntax
- Use industry-standard syntax that developers already know
- Design parser architecture that supports future expression features
- Single unified parser for both template structure and expressions

### Non-Goals
- Implementing expression support in this change (deferred to future)
- Supporting alternative syntax options (only `${var}` chosen)
- IDE plugin support (separate concern)

## Decisions

### Decision 1: Use `${var}` Syntax

**What**: Change from `\{var}` to `${var}` for variable interpolation.

**Why**:
- `\{` requires `\\{` in Java strings (backslash is escape character)
- `${var}` requires no escaping - write exactly what you mean
- Industry standard: Spring, Velocity, shell, Gradle, many others
- Developers already know this syntax

**Alternatives considered**:
| Syntax | Pros | Cons |
|--------|------|------|
| `\{var}` (current) | Unique | Requires `\\{` in Java, escaping nightmare |
| `${var}` (chosen) | Standard, no escaping | Conflicts with shell in templates containing shell scripts |
| `{{var}}` | Mustache standard | More verbose |
| `#{var}` | Thymeleaf/Ruby | Less common |

### Decision 2: Escape with `$${}`

**What**: To include literal `${` in output, write `$${`.

**Why**:
- Consistent with shell escaping pattern
- Single character doubling is intuitive
- `$${name}` → outputs `${name}` literally

### Decision 3: Single ANTLR Grammar with Lexer Modes

**What**: Use one unified ANTLR grammar that handles both template structure (text vs expressions) and expression parsing.

**Why**:
- ANTLR lexer modes are designed exactly for this use case
- Handles edge cases correctly (nested braces, escapes, strings containing `}`)
- Better error messages with accurate source positions
- Single parser pass, no offset mapping needed
- Industry standard approach (used by StringTemplate, Velocity, etc.)

**Alternatives considered**:
| Approach | Pros | Cons |
|----------|------|------|
| Regex + manual split | Simple for MVP | Breaks on nested braces, poor errors |
| Two-pass (split then parse) | Separation of concerns | Offset mapping, duplicated escape logic |
| Single ANTLR grammar (chosen) | Correct, extensible, good errors | ANTLR learning curve, generated code |

### Decision 4: Two-Phase Implementation

**What**: Implement in two phases - simple variables first, expressions later.

**Why**:
- Phase 1 (MVP): `${var}` only - can use simplified grammar subset
- Phase 2 (Future): Add `${var.field}`, `${a + b}`, `${method()}` - extend grammar
- Allows shipping useful functionality sooner
- Grammar is designed to support both from the start

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| ANTLR adds build complexity | Medium | Generated sources committed to repo, or maven plugin |
| Grammar bugs cause parsing failures | High | Comprehensive test suite with edge cases |
| `$` conflicts with shell scripts in templates | Low | Rare use case; `$${` escape available |
| Learning curve for maintainers | Low | ANTLR is well-documented, grammar is small |

## Grammar Sketch

```antlr
// Lexer with modes
lexer grammar TemplateLexer;

// Default mode - template text
TEXT          : ~[$]+ ;
DOLLAR_TEXT   : '$' ~[${] ;
ESCAPE        : '$$' ;
EXPR_START    : '${' -> pushMode(EXPRESSION) ;

mode EXPRESSION;
EXPR_END      : '}' -> popMode ;
ID            : [a-zA-Z_][a-zA-Z0-9_]* ;
DOT           : '.' ;
LPAREN        : '(' ;
RPAREN        : ')' ;
COMMA         : ',' ;
STRING        : '"' (~["])* '"' ;
NUMBER        : [0-9]+ ;
WS            : [ \t]+ -> skip ;

// Parser
parser grammar TemplateParser;
options { tokenVocab=TemplateLexer; }

template    : part* EOF ;
part        : TEXT | DOLLAR_TEXT | escape | expression ;
escape      : ESCAPE ;  // $$ -> literal $
expression  : EXPR_START expr EXPR_END ;

// Phase 1: Simple variables only
expr        : ID ;

// Phase 2 (future): Full expressions
// expr     : primary (DOT ID (LPAREN args? RPAREN)?)*
//          | expr ('+' | '-' | '*' | '/') expr
//          ;
// primary  : ID | NUMBER | STRING | '(' expr ')' ;
// args     : expr (',' expr)* ;
```

## Open Questions

None - design decisions are settled based on earlier discussion.
