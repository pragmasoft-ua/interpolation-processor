# Change: Update Template Syntax from `\{var}` to `${var}`

## Why

The current `\{var}` syntax creates a poor developer experience in Java because backslash is an escape character in string literals. Writing `str("Hello \{name}")` requires `str("Hello \\{name}")` in actual code, and escaping a literal placeholder becomes `\\\\{name}` - nearly unreadable. The `${var}` syntax is industry-standard (Spring, Velocity, shell), requires no Java escaping, and developers already know it.

## What Changes

- **BREAKING**: Template variable syntax changes from `\{varName}` to `${varName}`
- Escape sequence changes from `\\{` to `$${` (double dollar for literal `${`)
- Template parser implementation changes from planned regex/manual approach to unified ANTLR grammar with lexer modes
- API method `str()` behavior unchanged - only the template string format changes

## Impact

- Affected specs: `template-syntax` (new capability spec)
- Affected code:
  - `annotation-processor/`: Template parsing logic (new ANTLR-based parser)
  - `ARCHITECTURE.md`: Syntax documentation updates
  - `README.md`: Usage examples and syntax reference table
  - `TODO.md`: Template parser tasks
  - `openspec/project.md`: Template syntax section
- Affected users: None yet (project not released)
